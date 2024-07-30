package ru.telecor.gm.mobile.droid.presentation.garbageload

import android.location.Location
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import moxy.InjectViewState
import ru.telecor.gm.mobile.droid.R
import ru.telecor.gm.mobile.droid.entities.*
import ru.telecor.gm.mobile.droid.entities.db.TaskExtended
import ru.telecor.gm.mobile.droid.entities.processing.*
import ru.telecor.gm.mobile.droid.entities.processing.ContainerStatus.Companion.containerStatusUi
import ru.telecor.gm.mobile.droid.entities.processing.ContainerStatusGroup.Companion.fromContainerStatusGroupAll
import ru.telecor.gm.mobile.droid.entities.processing.ContainerStatusOr.Companion.containerStatusOriginal
import ru.telecor.gm.mobile.droid.entities.task.StatusTaskExtended
import ru.telecor.gm.mobile.droid.entities.task.TaskItem
import ru.telecor.gm.mobile.droid.model.PhotoType
import ru.telecor.gm.mobile.droid.model.interactors.PhotoInteractor
import ru.telecor.gm.mobile.droid.model.interactors.RouteInteractor
import ru.telecor.gm.mobile.droid.model.repository.CommonDataRepository
import ru.telecor.gm.mobile.droid.model.system.IResourceManager
import ru.telecor.gm.mobile.droid.presentation.base.BasePresenter
import ru.telecor.gm.mobile.droid.presentation.garbageload.util.GarbageLoadScreenState
import ru.telecor.gm.mobile.droid.servise.Screens
import ru.terrakok.cicerone.Router
import ru.telecor.gm.mobile.droid.entities.db.PhotoProcessingForApi
import ru.telecor.gm.mobile.droid.entities.db.ProcessingPhoto
import ru.telecor.gm.mobile.droid.entities.db.TaskDraftProcessingResult
import ru.telecor.gm.mobile.droid.entities.photo.GarbagePhotoModel
import ru.telecor.gm.mobile.droid.model.ContainerActionType
import ru.telecor.gm.mobile.droid.model.data.storage.SettingsPrefs
import ru.telecor.gm.mobile.droid.utils.ConnectivityUtils
import ru.telecor.gm.mobile.droid.utils.DataStorageManager
import java.io.File
import java.util.*
import javax.inject.Inject


/**
 * Project Truck Crew
 * Package ru.telecor.gm.mobile.droid.presentation.garbageload
 *
 *
 *
 * Created by Emil Zamaldinov (aka piligrim) 20.07.2020
 * Copyright © 2020 TKO-Inform. All rights reserved.
 */
@InjectViewState
class GarbageLoadPresenter @Inject constructor(
    val routeInteractor: RouteInteractor,
    private val commonDataRepository: CommonDataRepository,
    private val rm: IResourceManager,
    val settingsPrefs: SettingsPrefs,
    val router: Router,
    private val photoInteractor: PhotoInteractor,
    private val dataStorageManager: DataStorageManager
) : BasePresenter<GarbageLoadView>() {

    private lateinit var localTaskCache: TaskExtended
    var isCheck: Boolean = false
    private lateinit var localTaskDraftProcessingResult: TaskDraftProcessingResult
    private var localFailureReasonsCache: List<ContainerFailureReason> = arrayListOf()
    private lateinit var localRouteCache: RouteInfo
    private lateinit var localLoadLevelsCache: List<ContainerLoadLevel>
    private val list: ArrayList<GarbagePhotoModel> = arrayListOf()
    private var mapsTask: HashMap<String, ContainerStatusGrop> = hashMapOf()

    var conId: ArrayList<Long>? = arrayListOf()

    var modelPhoto: ArrayList<GarbagePhotoModel> = arrayListOf()

    val TAG = javaClass.simpleName
    private var containerStatusesAdapter: HashMap<String, ContainerStatus> = hashMapOf()
    private var containerStatuses: MutableList<ContainerStatus> = mutableListOf()
    private var containerGropeStatuses: MutableList<ContainerStatusGroupAll> = mutableListOf()

    private var pickupTask: TaskItem? = null

    //Created in process of work
    var photoType: PhotoType = PhotoType.LOAD_TROUBLE
    var photoLocation: Location? = null
    var photoFile: File? = null

    private var idTask: Long = 0

    private var idTaskCon = 0
    private var sizeId = 0

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        refreshLocalData()
    }

    override fun attachView(view: GarbageLoadView?) {
        super.attachView(view)
        updateUI()
        if (::localTaskDraftProcessingResult.isInitialized)
            setDraftDataToClean(localTaskDraftProcessingResult)
    }

    fun getAllPhotosFlow(): List<ProcessingPhoto> {
        return photoInteractor.allPhotosStateFlow.value
    }

    private fun refreshLocalData() {
        viewState.setLoadingState(true)
        fetchCurrentTask()
        fetchStartedRoute()
        fetchContainersLevelsList()
        launch {
            fetchTroubleReasons()
            updateUI()
            viewState.setLoadingState(false)
        }

        //Заполнение адаптера фтоографиями
        for (modelType in 1..PhotoType.values().size) {
            launch {
                val type = PhotoType.values()[modelType - 1]
                if (::localTaskCache.isInitialized && ::localRouteCache.isInitialized) {
                    photoInteractor.getTaskPhotosFlow(
                        localRouteCache.id,
                        localTaskCache.id.toLong(),
                        type
                    ).collect { it ->

                        if (it.isNotEmpty()) {
                            modelPhoto = getSortingPhotos(it as ArrayList<ProcessingPhoto>, type)
                            viewState.showPhoto(getSortingPhotos(it, type))
                        } else {
                            if (type == PhotoType.LOAD_TROUBLE) {
                                if (it.isNotEmpty()) {
                                    modelPhoto =
                                        getSortingPhotos(it as ArrayList<ProcessingPhoto>, type)
                                    viewState.showPhoto(getSortingPhotos(it, type))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun isVisibilityNext(boolean: Int) {
        settingsPrefs.visibilityNext = boolean
    }

    //Сортировка и добовление фото
    private fun getSortingPhotos(
        item: ArrayList<ProcessingPhoto>,
        modelType: PhotoType? = null
    ): ArrayList<GarbagePhotoModel> {
        val getGarbagePhoto =
            list.firstOrNull { garbage -> garbage.type == modelType.toString() && item.size != 0 }
        if (getGarbagePhoto == null) {
            list.add(GarbagePhotoModel(modelType.toString(), item))
        } else {
            val type =
                list.indexOfFirst { type -> type.type == modelType.toString() && type.item.size != item.size }
            if (type != -1) {
                list.removeAt(type)
                list.addAll(type, listOf(GarbagePhotoModel(modelType.toString(), item)))
            }
        }
        return list
    }

    //Удолдяет фото из списка если удилить в окне проблема
    fun setCleatPhoto(photo: ProcessingPhoto) {
        val cleatPhoto =
            list.indexOfFirst { garbage -> garbage.type == PhotoType.LOAD_TROUBLE.toString() }
        if (cleatPhoto != -1) {
            list.removeAt(cleatPhoto)
            viewState.showPhoto(list)
        }
    }

    //Удаление фотографии
    fun onPhotoDeleteClicked(photo: ProcessingPhoto) {
        launch {
            photoInteractor.deletePhoto(photo)
            val position = list.indexOfFirst { it.item == arrayListOf(photo) }
            if (position != -1) {
                if (list[position].item.size == 1) {
                    list.removeAt(position)
                    updateUI()
                } else {
                    list[position].item.removeAt(position)
                    updateUI()
                }
                viewState.showPhoto(list)
                updateUI()
            }
        }
    }

    private suspend fun fetchTroubleReasons() {
        val result = routeInteractor.getContainerTroubleReasons()
        handleResult(result, {
            localFailureReasonsCache = it.data
        }, { handleError(it, rm) })
    }

    private fun fetchContainersLevelsList() {
        val levelsList = commonDataRepository.getContainerLevelsList()
        handleResult(levelsList, { localLoadLevelsCache = it.data }, { handleError(it, rm) })
    }

    private fun fetchStartedRoute() {
        launch {
            val route = routeInteractor.getStartedRouteInfo(
                syncAval = ConnectivityUtils.syncAvailability(
                    routeInteractor.getContext(),
                    ConnectivityUtils.DataType.SECONDARY
                )
            )
            handleResult(route, { localRouteCache = it.data }, { handleError(it, rm) })
        }
    }

    private fun fetchCurrentTask() {
        val res = routeInteractor.getCurrentTask()
        handleResult(res, {
            GlobalScope.launch {
                val taskDraftData = routeInteractor.getDraftByTaskID(it.data.id.toLong())
                handleResult(taskDraftData, { tdRes ->
                    localTaskDraftProcessingResult = tdRes.data
                    setDraftDataToClean(
                        localTaskDraftProcessingResult
                    )
                }, {})

            }
            idTask = it.data.id.toLong()
            localTaskCache = it.data
        }, {
            handleError(it, rm)
        })
    }

    fun getContainerName(taskItem: TaskItem): String? =
        localTaskCache.stand?.containerGroups?.map { cg -> cg.containerType }
            ?.find { taskItem.containerTypeId == it.id }?.name

    private fun updateUI() {
        try {
            viewState.setTaskInfo(localTaskCache.stand!!.address)
            viewState.setContainerAction(localTaskCache.containerAction.caption)

//            if (localTaskCache.containerAction.name == "PICKUP"
//                || localTaskCache.containerAction.name == "REPLACE"
//            ) {
//                pickupTask = localTaskCache.taskItems.firstOrNull()
//            }

            if (localTaskCache.containerAction.name ==
                ContainerActionType.valueOf(localTaskCache.containerAction.name).toString()
            ) {
                pickupTask = localTaskCache.taskItems.firstOrNull()
            }

            sortList()

            viewState.setState(
                GarbageLoadScreenState(
                    localLoadLevelsCache,
                    getListOfDefaultContainers(),
                    localFailureReasonsCache
                )
            )

            for (i in getListOfDefaultContainers()) {
                if (i.containerStatus != null) {
                    viewState.setActionCompletedBottom(true)
                    break
                } else {
                    viewState.setActionCompletedBottom(false)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (::localTaskCache.isInitialized) {
            val id = localTaskCache
            if (containerStatuses.isNotEmpty()) {
                routeInteractor.isEdited = true
                routeInteractor.taskId = id.id
            }
        }
    }

    // Сортирует списко
    private fun sortList() {
        containerStatusesAdapter.clear()
        val listMap: HashMap<String, ContainerStatus> = hashMapOf()
        if (containerStatuses.size != 0) {
            containerStatuses.forEach { con ->
                listMap[con.id.toString()] = con
            }
        }

        if (containerGropeStatuses.size != 0) {
            containerGropeStatuses.forEach {
                it.containerStatuses?.forEach { conG ->
                    listMap[conG.id.toString()] = ContainerStatus(
                        conG.id, conG.containerFailureReason, conG.containerTypeId, conG.contractId,
                        conG.createTime, conG.statusType, conG.volumeAct, conG.volumePercent,
                        conG.weight, conG.photos, conG.rfid, conG.staff, conG.allGroupContainersId
                    )
                }
            }
        }
        containerStatusesAdapter = listMap
    }

    //Запролняет обратеую для показа пезультата выбора
    private fun getListOfDefaultContainers(): List<StatusTaskExtended> {
        val ste: MutableList<StatusTaskExtended> = mutableListOf()

        for (taskItem in localTaskCache.taskItems) {
            ste.addAll(taskItem.statuses.map { m ->
                StatusTaskExtended(
                    m.id,
                    localTaskCache.stand!!.containerGroups.find { containerGroup ->
                        containerGroup.containerType.id == m.containerTypeId
                    }!!.containerType,
                    m.taskItemId,
                    containerStatusesAdapter[m.id.toString()],
                    rule = taskItem.rule,
                    containerAction = localTaskCache.containerAction.caption,
                    containerGroups = localTaskCache.stand!!.containerGroups.find { containerGroup ->
                        containerGroup.containerType.id == m.containerTypeId
                    }!!.garbageType,
                    privatePhotos = photoInteractor.allPhotosStateFlow.value.filter { photo -> m.id.toLong() in photo.conId }
                )
            })
        }
        return ste
    }

    private fun removeOldContainerStatus(id: Long) {
        val index = containerStatuses.indexOfFirst { it.id == id }
        if (index != -1) {
            containerStatuses.removeAt(index)
        }
    }

    private fun removeOldContainerStatusCon(id: Long) {
        val index = containerGropeStatuses.indexOfFirst { it.id?.toLong() == id }
        if (index != -1) {
            containerGropeStatuses.removeAt(index)
        }
    }

    private fun setDraftDataToClean(draftsData: TaskDraftProcessingResult?) {
        containerStatuses.clear()
        containerGropeStatuses.clear()
        draftsData?.standResults?.first()?.containerStatuses?.let {
            containerStatuses =
                containerStatusUi(it.toMutableList()) as MutableList<ContainerStatus>
        }
        draftsData?.standResults?.first()?.containerStatusGroups?.let {
            it.map { cG ->
                containerGropeStatuses.add(
                    ContainerStatusGroupAll(
                        cG.containerStatuses,
                        cG.createTime,
                        cG.id?.toLong(),
                        cG.photos,
                        cG.volume,
                        cG.weight,
                    )
                )
            }
        }
        updateUI()
    }

    fun photoBeforeConClicked(location: Location? = null) {
        photoType = PhotoType.CONTAINER_BEFORE
        openCamera(location)
    }

    fun photoBeforeConClickedTrouble(location: Location? = null) {
        photoType = PhotoType.CONTAINER_TROUBLE
        openCamera(location)
    }

    fun photoBeforeConClickedAfter(location: Location? = null) {
        photoType = PhotoType.CONTAINER_AFTER
        openCamera(location)
    }

    fun photoBeforeButtonClicked(location: Location? = null) {
        photoType = PhotoType.LOAD_BEFORE
        conId = arrayListOf()
        openCamera(location)
    }

    fun photoAfterButtonClicked(location: Location? = null) {
        photoType = PhotoType.LOAD_AFTER
        conId = arrayListOf()
        openCamera(location)
    }

    fun onAddProblemButtonClicked(location: Location? = null) {
        photoType = PhotoType.LOAD_TROUBLE
        conId = arrayListOf()
        openCamera(location)
    }

    fun onAddBlockageButtonClicked(location: Location? = null) {
        photoType = PhotoType.LOAD_TROUBLE_BLOCKAGE
        conId = arrayListOf()
        openCamera(location)
    }

    fun photoProblemButtonClicked() {
        viewState.takeProblemPhoto(
            localRouteCache.id.toString(),
            localTaskCache.id.toString(),
        )
    }

    fun getStatusPhoto(boolean: Boolean) {
        photoInteractor.getStatusPhoto(boolean)
    }

    fun onSettingsClicked() {
        viewState.showSettingsMenu(true)
    }

    fun taskDoneButtonClicked(
        volume: Double,
        idTask: Int,
        status: String,
        size: Int,
        listStatus: String
    ) {
        setPickupValue(volume, idTask, status, size, listStatus)
    }

    fun onQrCodeScannerButtonClicked() {
        router.navigateTo(Screens.QrCodeScanner)
    }

    fun routeButtonClicked() {
        router.navigateTo(Screens.RouteStands)
    }

    private fun checkCurrentTaskDone(): Boolean =
        ::localTaskCache.isInitialized && routeInteractor.getTaskResultById(localTaskCache.id.toLong()) != null

    private fun getPhotoModel() =
        photoInteractor.allPhotosStateFlow.value.filter { it.routeId == localRouteCache.id && it.taskId == localTaskCache.id.toLong() }

    fun taskDoneButtonClicked(): Boolean {
        val containerStatusesDef: MutableList<ContainerStatus> = mutableListOf()
        containerStatusesAdapter.forEach {
            containerStatusesDef.add(it.value)
        }

        if (checkCurrentTaskDone()) {
            router.exit()
            viewState.setLoadingState(false)
            return true
        }
        val statusType = ProcessingStatusType(
            "",
            statusTypeBeck(containerStatusesDef)
        )

        // list for stand results (list of container statuses)
        val standResults: MutableList<StandResult> = mutableListOf()

        val photosCount = getPhotoModel().size
        val before = getPhotoModel().filter { it.photoType == PhotoType.LOAD_BEFORE }.size
        val after = getPhotoModel().filter { it.photoType == PhotoType.LOAD_AFTER }.size

        // if photo count is zero, послать нахуй водителя
        val isAfterNeeded = routeInteractor.getCurrentRoute().requirePhotoAfter
        val isBeforeNeeded = routeInteractor.getCurrentRoute().requirePhotoBefore
        val isTroublePhotoNeeded = routeInteractor.getCurrentRoute().requireFailurePhoto

        val problems =
            containerStatusesDef.any { it.statusType?.name == ContainerStatusType.Type.FAILED }

        val allProblems =
            containerStatusesDef.all { it.statusType?.name == ContainerStatusType.Type.FAILED }

        if (problems && isTroublePhotoNeeded) {
            val pCount =
                getPhotoModel().filter { it.photoType == PhotoType.LOAD_TROUBLE || it.photoType == PhotoType.LOAD_TROUBLE_BLOCKAGE }.size
            if (pCount == 0) {
//                viewState.showMessage(rm.getString(R.string.garbage_load_fragment_trouble_photos_needed_warning))
                viewState.textMessageError(rm.getString(R.string.garbage_load_fragment_trouble_photos_needed_warning))
                viewState.setLoadingState(false)
                return true
            }
        }

        if (containerStatusesDef.size < localTaskCache.taskItems.sumOf { item -> item.statuses.size }) {
//            viewState.showMessage(rm.getString(R.string.garbage_load_fragment_fill_container_info_warning))
            viewState.textMessageError(rm.getString(R.string.garbage_load_fragment_fill_container_info_warning))
            viewState.setLoadingState(false)
            return true
        }

        if ((((before == 0) and isBeforeNeeded) or ((after == 0) and isAfterNeeded)) and !allProblems) {
            when {
                isBeforeNeeded && isAfterNeeded -> {
//                    viewState.showMessage(rm.getString(R.string.error_photo_required))
                    viewState.textMessageError(rm.getString(R.string.error_photo_required))
                    viewState.setLoadingState(false)
                    return true
                }
                isBeforeNeeded -> {
                    viewState.textMessageError(rm.getString(R.string.error_photo_required_before))
//                    viewState.showMessage(rm.getString(R.string.error_photo_required_before))
                    viewState.setLoadingState(false)
                    return true
                }
                isAfterNeeded -> {
//                    viewState.showMessage(rm.getString(R.string.error_photo_required_after))
                    viewState.textMessageError(rm.getString(R.string.error_photo_required_after))
                    viewState.setLoadingState(false)
                    return true
                }
            }
        }

        val allTasks = routeInteractor.getAllTaskInDevise()
        val currentTaskPhotosNameList = getPhotoModel().map { File(it.photoPath).name }
        val allDeliveredTasksPhotosNameList = arrayListOf<String>()


        allTasks?.forEach { task ->
            val allTaskData = routeInteractor.getDeliveredTask(taskId = task.id.toLong())
            if (allTaskData?.hasPhotos == true) {
                allTaskData.standResults?.forEach { standResult ->
                    standResult.photos?.map { allDeliveredTasksPhotosNameList += it.filename }
                }
            }
        }

        for (photoName in allDeliveredTasksPhotosNameList)
            if (photoName in currentTaskPhotosNameList) {
                Thread.sleep(500L)
                viewState.showMessage(rm.getString(R.string.error_duplicate_data))
                viewState.setLoadingState(false)
                return true
            }

        //Сортировка фотографий
        val photoModel = getPhotoModel().filter { it.photoType.status == "all" }

        //Сортировка списка удаление лищних элементов
        getSortingLists()

        // result object
        val standResult = StandResult(
            localTaskCache.id.toLong(),
            routeInteractor.processingArrivalTime ?: Date().time,
            Date().time,
            Date().time,
            containerStatusOriginal(containerStatuses),
            photosCount ?: 0,
            photos = photoModel.map {
                PhotoProcessingForApi.fromProcessingPhoto(it)
            },
            containerStatusGroups = fromContainerStatusGroupAll(containerGropeStatuses),
            tonnage = null
        )
        standResults.add(standResult)
        viewState.setLoadingState(false)
        launch {
//            routeInteractor.addTaskToProcessing(localTaskCache, statusType, standResults, null)
            viewState.setCompleteRoute(localTaskCache, statusType, standResults)
//            router.exit()
        }
        return false
    }

    //Сортировка списка удаление лищних элементов
    private fun getSortingLists() {
        if (containerGropeStatuses.size != 0) {
            containerGropeStatuses.forEach { f ->
                val index = containerStatuses.indexOfFirst { it.id == f.id }
                if (index != -1) {
                    containerStatuses.removeAt(index)
                }
            }
        }
    }

    private fun saveTaskDataDraft() {
        val statusType = ProcessingStatusType(
            "",
            statusTypeBeck(containerStatuses)
        )
        val standResults: MutableList<StandResult> = mutableListOf()
        val standResult = StandResult(
            localTaskCache.id.toLong(),
            routeInteractor.processingArrivalTime ?: Date().time,
            Date().time,
            Date().time,
            containerStatusOriginal(containerStatuses),
            0,
            photos = null,
            null,
            containerStatusGroups = fromContainerStatusGroupAll(containerGropeStatuses)
        )

        standResults.add(standResult)
        try {
            GlobalScope.launch {
                val result = routeInteractor.addDraftTaskToProcessing(
                    localTaskCache,
                    statusType,
                    standResults,
                    null,
                    idTask
                )
                handleResult(result, {
                    localTaskDraftProcessingResult = it.data
                    setDraftDataToClean(localTaskDraftProcessingResult)
                }, {})
            }
        } catch (e: java.lang.Exception) {
        }
        updateUI()
    }

    // true if at least one container is failed
    private fun statusTypeBeck(item: MutableList<ContainerStatus>): StatusType {
        var problemStatus = false
        var successfullyStatus = false
        if (item.any { it.statusType?.name == ContainerStatusType.Type.FAILED }) {
            problemStatus = true
        }
        if (item.any { it.statusType?.name == ContainerStatusType.Type.SUCCESS }) {
            successfullyStatus = true
        }
        if (problemStatus && successfullyStatus) {
            return StatusType.PARTIALLY
        } else if (problemStatus && !successfullyStatus) {
            return StatusType.FAIL
        } else if (!problemStatus && successfullyStatus) {
            return StatusType.SUCCESS
        }
        return StatusType.SUCCESS
    }

    //Толька по нажатию на кнопку сохроняет задание
    fun saveRoute() {
        if (sizeId > 1) {
            formGroupModel()
        }
        getSortingLists()
        saveTaskDataDraft()
    }

    fun getRoutes() {
        router.navigateTo(Screens.RouteStands)
    }

    fun getPhoto(photo: ProcessingPhoto) {
        router.navigateTo(Screens.PhotoViewing(photo))
    }

    //тут открывается камера
    private fun openCamera(location: Location? = null) {
        photoLocation = location
        photoFile = File.createTempFile("temp", ".jpg", dataStorageManager.getCacheDirectory())
        viewState.startExternalCameraForResult(photoFile?.absolutePath ?: return)
    }

    //Функция добовление фото
    private fun setPhotoMode(status: StatusTaskExtended? = null): List<PhotoProcessingForApi> =
        photoInteractor.allPhotosStateFlow.value.filter {
            it.routeId == localRouteCache.id &&
                    it.taskId == localTaskCache.id.toLong() &&
                    status?.id?.toLong() in it.conId
        }.map { PhotoProcessingForApi.fromProcessingPhoto(it) }

    //Масса объём
    private fun setPickupValue(
        volume: Double,
        idTask: Int,
        status: String,
        size: Int,
        listStatus: String
    ) {
        var nameStatus = 0
        if (status == "SUCCESS_FAIL_MANUAL_COUNT_WEIGHT") {
            nameStatus = volume.toInt()
        }

        val pt = pickupTask ?: return
        val containerIds = arrayListOf<Int>()
        localTaskCache.taskItems.map { it.statuses.map { s -> containerIds.add(s.id) } }
        if (containerIds.isNotEmpty()) {
            containerIds.map { cT ->
                val containerId = if (idTask != 0) idTask else cT
                getListOfDefaultContainers().find { it.id == containerId }
                    ?.let { tes ->
                        if (size <= 1) {
                            val conStatEntity = ContainerStatus(
                                idTask.toLong(),
                                null,
                                localTaskCache.stand?.containerGroups
                                    ?.find { cg -> cg.containerType.id == pt.containerTypeId }!!.containerType.id,
                                pt.contract.id,
                                Date().time,
                                ContainerStatusType("", ContainerStatusType.Type.SUCCESS),
                                volume,
                                1.0,
                                nameStatus,
                                photos = setPhotoMode(tes)
                            )
                            removeOldContainerStatus(getListOfDefaultContainers()[getListOfDefaultContainers().indexOfFirst { it.id == idTask }].id.toLong())
                            containerStatuses.add(conStatEntity)
                        } else {
                            sizeId = size
                            modelContainerGroup(
                                tes,
                                localLoadLevelsCache.find { it.value == volume },
                                groupId = listStatus, volumeAct = volume, volumeName = nameStatus
                            )
                            idTaskCon = containerId
                        }
                    }
            }
        }
        updateUI()
    }

    fun elementLoadLevelChosen(
        statusTaskExtended: StatusTaskExtended,
        containerLoadLevel: ContainerLoadLevel,
        size: Int
    ) {
        val status = ContainerStatus(
            statusTaskExtended.id.toLong(),
            null,
            statusTaskExtended.containerType.id,
            localTaskCache.taskItems.first().contract.id,
            Date().time,
            ContainerStatusType("", ContainerStatusType.Type.SUCCESS),
            0.9,
            containerLoadLevel.value,
            photos = setPhotoMode(statusTaskExtended) ?: null
        )

        if (size > 1) {
            sizeId = size
            modelContainerGroup(statusTaskExtended, containerLoadLevel, volumeAct = 0.0)
            idTaskCon = statusTaskExtended.id
        } else {
            removeOldContainerStatus(statusTaskExtended.id.toLong())
            containerStatuses.add(status)
        }
        updateUI()
    }

    fun onTroubleReasonChosen(
        taskStatus: List<StatusTaskExtended>,
        reason: ContainerFailureReason,
        size: Int
    ) {
        taskStatus.forEach {
            val status = ContainerStatus(
                it.id.toLong(), reason,
                it.containerType.id,
                localTaskCache.taskItems.first().contract.id,
                Date().time,
                ContainerStatusType("", ContainerStatusType.Type.FAILED),
                0.0,
                0.0,
                photos = setPhotoMode(it) ?: null
            )
            if (taskStatus.size > 1) {
                sizeId = size
                modelContainerGroup(it, reason = reason, volumeAct = 0.0)
                idTaskCon = it.id
            } else {
//                containerGropeStatuses = arrayListOf()
                removeOldContainerStatus(it.id.toLong())
                containerStatuses.add(status)
            }
        }
        updateUI()
    }

    //Заполнение модели контенера ContainerStatusGroup
    private fun modelContainerGroup(
        statusTaskExtended: StatusTaskExtended,
        containerLoadLevel: ContainerLoadLevel? = null,
        reason: ContainerFailureReason? = null,
        groupId: String? = null,
        volumeAct: Double,
        volumeName: Int? = 0
    ) {
        //нужна проверка
        var statusType = ContainerStatusType.Type.NEW
        statusType = if (reason == null) {
            ContainerStatusType.Type.SUCCESS
        } else {
            ContainerStatusType.Type.FAILED
        }

        val status = ContainerStatusGrop(
            statusTaskExtended.id.toLong(),
            reason,
            statusTaskExtended.containerType.id,
            localTaskCache.taskItems.first().contract.id,
            Date().time,
            ContainerStatusType("", statusType),
            volumeAct,
            containerLoadLevel?.value,
            volumeName,
            allGroupContainersId = conId?.sortedBy { it }
        )
        mapsTask[statusTaskExtended.id.toString()] = status
    }

    //Сохранение групавой модоли
    private fun formGroupModel() {
        //Заполнение групавой модели
        val containerStatusesGr: MutableList<ContainerStatusGrop> = mutableListOf()

        val ss = getListOfDefaultContainers().find { it.id == idTaskCon }

        if (mapsTask.size != 0) {
            mapsTask.forEach {
                containerStatusesGr.add(it.value)
            }

            val statusGr = ContainerStatusGroupAll(
                containerStatuses = containerStatusesGr,
                createTime = Date().time,
                photos = setPhotoMode(ss),
                id = ss!!.id.toLong()
            )
            removeOldContainerStatusCon(ss.id.toLong())
            containerGropeStatuses.add(statusGr)
            mapsTask.clear()
        }
    }

    fun detailedPhoto(
        itemMore: ArrayList<StatusTaskExtended>,
        listStatus: List<StatusTaskExtended>
    ) {
        conId?.clear()
        if (itemMore.size != 0) {
            if (itemMore.size < 2) {
                conId = arrayListOf(itemMore[0].id.toLong())
                viewState.setHidingPanelValid(true)
            } else {
                itemMore.forEach {
                    conId?.add(it.id.toLong())
                    viewState.setHidingPanelValid(false)
                }
            }
            deletePhoto(itemMore)
        } else {
            listStatus.forEach {
                conId?.add(it.id.toLong())
                viewState.setHidingPanelValid(false)
            }
            deletePhoto(listStatus)
        }
    }

    //Удаляет групавую модель
    fun deleteTaskCon(
        itemMore: ArrayList<StatusTaskExtended>,
        boolean: Boolean,
        status: String? = null
    ) {
        itemMore.map { item ->
            if (status != "all") {
                val indexCGS = containerGropeStatuses.indexOfFirst { cgs ->
                    item.id.toLong() in (cgs.containerStatuses?.map { it.id } ?: listOf())
                }
                if (indexCGS != -1) {
                    if (!boolean) {
                        viewState.setOpeningFragment()
                    } else {
                        containerGropeStatuses.removeAt(indexCGS)
                        saveTaskDataDraft()
                    }
                }
                val indexCont = containerStatuses.indexOfFirst { cs -> cs.id == item.id.toLong() }
                if (indexCont != -1) containerStatuses.removeAt(indexCont)

                deleteGrPhoto(item)
                delTaskMod(item)
            } else {
                if (!boolean) {
                    if (itemMore.firstOrNull { it.containerStatus != null} != null) {
                        viewState.setOpeningFragment("all")
                    }else{}
                }else{
                    containerGropeStatuses.clear()
                    containerStatuses.clear()
                    deleteGrPhoto(item)
                    delTaskMod(item)
                    saveTaskDataDraft()
                }
            }
        }
    }

    private fun delTaskMod(itemMore: StatusTaskExtended) =
        containerStatuses.firstOrNull { it.id == itemMore.id.toLong() }
            ?.let { containerStatuses.remove(it) }

    // Удаление фото если удаляется модель
    private fun deleteGrPhoto(itemMore: StatusTaskExtended) = launch {
        photoInteractor.allPhotosStateFlow.value.filter { itemMore.id.toLong() in it.conId }
            .map { photo -> photoInteractor.deletePhoto(photo) }
    }

    //Удаление фото
    private fun deletePhoto(listStatus: List<StatusTaskExtended>) {
        listStatus.forEach {
            getPhotoModel().forEach { photo ->
                photo.conId.forEach { id ->
                    if (id == conId?.first { fi -> fi == it.id.toLong() }) {
                        if (photo.photoType.status == "one") {
                            onPhotoDeleteClicked(photo)
                        }
                    }
                }
            }
        }
    }

    //Заполнение модели контенера
    fun setFillingModel(
        value: Double,
        itemMore: ArrayList<StatusTaskExtended>,
        listContainer: List<ContainerLoadLevel>
    ) {
        itemMore.forEach {
            if (it.rule == "SUCCESS_FAIL_MANUAL_VOLUME" ||
                it.rule == "SUCCESS_FAIL_MANUAL_COUNT_WEIGHT"
            ) {
                taskDoneButtonClicked(
                    value.toString().toDoubleOrNull() ?: 0.0,
                    it.id,
                    it.rule.toString(),
                    itemMore.size,
                    it.groupId?.toString() ?: ""
                )
            } else {
                val listPosition = listContainer.indexOfFirst { it.value == value }
                if (listPosition != -1) {
                    elementLoadLevelChosen(
                        it,
                        listContainer[listPosition], itemMore.size
                    )
                }
            }
        }
    }

    fun sorting(type: String): Boolean {
        when (type) {
            "TASK_TROUBLE" -> {
                return true
            }
            "LOAD_BEFORE" -> {
                return true
            }

            "LOAD_AFTER" -> {
                return true
            }
            "LOAD_TROUBLE" -> {
                return true
            }
        }
        return false
    }
}