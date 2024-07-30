package ru.telecor.gm.mobile.droid.presentation.successfully


import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.telecor.gm.mobile.droid.R
import ru.telecor.gm.mobile.droid.entities.ContainerFailureReason
import ru.telecor.gm.mobile.droid.entities.ContainerLoadLevel
import ru.telecor.gm.mobile.droid.entities.RouteInfo
import ru.telecor.gm.mobile.droid.entities.StatusType
import ru.telecor.gm.mobile.droid.entities.db.PhotoProcessingForApi
import ru.telecor.gm.mobile.droid.entities.db.ProcessingPhoto
import ru.telecor.gm.mobile.droid.entities.db.TaskDraftProcessingResult
import ru.telecor.gm.mobile.droid.entities.db.TaskExtended
import ru.telecor.gm.mobile.droid.entities.photo.GarbagePhotoModel
import ru.telecor.gm.mobile.droid.entities.processing.ContainerStatus
import ru.telecor.gm.mobile.droid.entities.processing.ContainerStatusType
import ru.telecor.gm.mobile.droid.entities.processing.ProcessingStatusType
import ru.telecor.gm.mobile.droid.entities.processing.StandResult
import ru.telecor.gm.mobile.droid.entities.task.StatusTaskExtended
import ru.telecor.gm.mobile.droid.entities.task.TaskItem
import ru.telecor.gm.mobile.droid.model.PhotoType
import ru.telecor.gm.mobile.droid.model.interactors.PhotoInteractor
import ru.telecor.gm.mobile.droid.model.interactors.RouteInteractor
import ru.telecor.gm.mobile.droid.model.repository.CommonDataRepository
import ru.telecor.gm.mobile.droid.model.system.IResourceManager
import ru.telecor.gm.mobile.droid.presentation.base.BasePresenter
import ru.telecor.gm.mobile.droid.presentation.garbageload.util.GarbageLoadScreenListState
import ru.telecor.gm.mobile.droid.presentation.garbageload.util.GarbageLoadScreenPickupState
import ru.telecor.gm.mobile.droid.presentation.garbageload.util.GarbageLoadScreenState
import ru.telecor.gm.mobile.droid.utils.ConnectivityUtils
import ru.telecor.gm.mobile.droid.utils.DataStorageManager
import ru.terrakok.cicerone.Router
import java.io.File
import java.util.*
import javax.inject.Inject

class SuccessfullyPresenter @Inject constructor(
    private val routeInteractor: RouteInteractor,
    private val commonDataRepository: CommonDataRepository,
    private val rm: IResourceManager,
    private val router: Router,
    private val photoInteractor: PhotoInteractor,
    private val dataStorageManager: DataStorageManager
) : BasePresenter<SuccessfullyLoadView>() {

    private val list: ArrayList<GarbagePhotoModel> = arrayListOf()
    private lateinit var localTaskCache: TaskExtended
    private lateinit var localRouteCache: RouteInfo

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        fetchCurrentTask()
        fetchStartedRoute()

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
                            viewState.showPhoto(
                                getSortingPhotos(
                                    it as ArrayList<ProcessingPhoto>,
                                    type
                                )
                            )
                        } else {
                            if (type == PhotoType.LOAD_TROUBLE) {
                                if (it.isNotEmpty()) {
                                    viewState.showPhoto(
                                        getSortingPhotos(
                                            it as ArrayList<ProcessingPhoto>,
                                            type
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun attachView(view: SuccessfullyLoadView?) {
        super.attachView(view)

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
            localTaskCache = it.data
        }, {
            handleError(it, rm)
        })
    }

    //Удаление фотографии
    fun onPhotoDeleteClicked(photo: ProcessingPhoto) {
        launch {
            photoInteractor.deletePhoto(photo)
            val position = list.indexOfFirst { it.item == arrayListOf(photo) }
            if (position != -1) {
                if (list[position].item.size == 1) {
                    list.removeAt(position)
                } else {
                    list[position].item.removeAt(position)
                }

                viewState.showPhoto(list)
            }
        }
    }

    fun sorting(type: String): Boolean{
        when(type){
            "CONTAINER_TROUBLE" ->{
                return true
            }
            "CONTAINER_BEFORE" ->{
                return true
            }

            "CONTAINER_AFTER" ->{
                return true
            }
            "DOCUMENTARY_PHOTO" ->{
                return true
            }
        }
        return false
    }
}
