package ru.telecor.gm.mobile.droid.presentation.routestands

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import moxy.InjectViewState
import ru.telecor.gm.mobile.droid.entities.RouteInfo
import ru.telecor.gm.mobile.droid.entities.StatusType
import ru.telecor.gm.mobile.droid.entities.VisitPoint
import ru.telecor.gm.mobile.droid.entities.dumping.GetListCouponsModel
import ru.telecor.gm.mobile.droid.entities.stand.Stand
import ru.telecor.gm.mobile.droid.entities.task.TaskExtended
import ru.telecor.gm.mobile.droid.entities.task.TaskRelations
import ru.telecor.gm.mobile.droid.model.data.server.GmWebSocket
import ru.telecor.gm.mobile.droid.model.data.storage.SettingsPrefs
import ru.telecor.gm.mobile.droid.model.interactors.RouteInteractor
import ru.telecor.gm.mobile.droid.model.mappers.ConverterMappers
import ru.telecor.gm.mobile.droid.model.system.IResourceManager
import ru.telecor.gm.mobile.droid.presentation.base.BasePresenter
import ru.telecor.gm.mobile.droid.servise.Screens
import ru.telecor.gm.mobile.droid.utils.ConnectivityUtils
import ru.terrakok.cicerone.Router
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Project Truck Crew
 * Package ru.telecor.gm.mobile.droid.presentation.routestands
 *
 *
 *
 * Created by Emil Zamaldinov (aka piligrim) 17.07.2020
 * Copyright © 2020 TKO-Inform. All rights reserved.
 */
@InjectViewState
class RouteStandsPresenter @Inject constructor(
    private val routeInteractor: RouteInteractor,
    private val router: Router,
    private val rm: IResourceManager,
    private var webSocket: GmWebSocket,
    private val converterMappers: ConverterMappers,
    private val settingsPrefs: SettingsPrefs
) : BasePresenter<RouteStandsView>() {

    var isNearFilterOn = false
    lateinit var localNearStandsList: List<Stand>
    private var query: String? = null
    var taskId: Int = 0

    var errorFlightTicketModel = MutableLiveData<String>()
    var getListCouponsModel = arrayListOf(GetListCouponsModel())
    var listItem = ArrayList<GetListCouponsModel>()
    private var taskItemCon: List<TaskRelations> = arrayListOf()

    private lateinit var getVisitPoint: VisitPoint

    private var relTaskList = listOf<TaskRelations>()
    private var relTaskList2 = listOf<TaskRelations>()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        loadAndSetTasksList(true)
        updateTime()

        //event when task list is updated
        routeInteractor.addOnTasksUpdatedListener {
            launch {
                var tasks = routeInteractor.getTaskWithRelations()
                val currentTask = routeInteractor.getCurrentTask()

                relTaskList = converterMappers.relationsTuTaskExtended(it)
                if (relTaskList.isNotEmpty()){
                    tasks = relTaskList
                }

                handleResult(currentTask, { ct ->
                    tasks =
                        tasks.map { t -> t.copy(task = t.task.copy(isCurrent = t.task.id == ct.data.id)) }
                }, {})
                android.os.Handler().postDelayed({ filterAndSetTasksList(tasks)
                }, 10000)
            }
        }

        routeInteractor.setErrorMes {
            viewState.showMessage(it)
        }
    }

    fun routes(): RouteInfo? {
        return routeInteractor.startedRoute
    }

    override fun attachView(view: RouteStandsView?) {
        super.attachView(view)
        taskId = routeInteractor.taskId

    }

    fun refreshTaskList() = loadAndSetTasksList()

    private fun loadAndSetTasksList(scrollToCurrent: Boolean = false) {
        viewState.showLoading(true)
        launch {
            relTaskList = converterMappers.relationsTuTaskExtended(routeInteractor.getAllTaskInDevise())
            var refresh = false
            var tasks = routeInteractor.getTaskWithRelations()
            if (relTaskList.isNotEmpty()){
                tasks = relTaskList
            }
            if (tasks.count() == tasks.filter { it.task.statusType == StatusType.NEW }.count()) {
                routeInteractor.getTaskProcessingResults()?.let { rP ->
                    if (rP.count() > 0 && ConnectivityUtils.syncAvailability(routeInteractor.getContext())) {
                        routeInteractor.startedRoute?.id?.let { rId ->
                            handleResult(
                                routeInteractor.loadTasksForCurrentRoute(
                                    rId,
                                    true
                                ), { refresh = true }, { handleError(it, rm) })
                        }
                    }
                }
            }
            if (refresh) tasks = routeInteractor.getTaskWithRelations()
            val currentTask = routeInteractor.getCurrentTask()
            handleResult(currentTask, { ct ->
                tasks =
                    tasks.map { t -> t.copy(task = t.task.copy(isCurrent = t.task.id == ct.data.id)) }
                viewState.showLoading(false)
            }, { viewState.showLoading(false) })
            filterAndSetTasksList(tasks, scrollToCurrent)
            taskItemCon = tasks
        }
    }

    private fun filterAndSetTasksList(
        tasks: List<TaskRelations>,
        scrollToCurrent: Boolean = false
    ) {
        viewState.showLoading(true)
        var finalList = tasks
        if (isNearFilterOn) {
            finalList = tasks.filter { task ->
                localNearStandsList.map { stand -> stand.id }.contains(task.task.stand?.id ?: 0)
            }
        }
        query?.let { query ->
            finalList = finalList.filter {
                (it.task.stand?.address ?: it.task.visitPoint?.name ?: "").toLowerCase()
                    .contains(query.toLowerCase())
            }
        }
        relTaskList2 = tasks
        if (relTaskList2.isEmpty()){
            viewState.setTasksList(finalList.sortedBy { it.task.statusType.order }, listItem)
        } else {
            viewState.setTasksList(tasks, listItem)
        }
        if (scrollToCurrent) {
            launch {
                val cur = routeInteractor.getCurrentTask()
                handleResult(cur, {
                    val pos = finalList.indexOf(it.data)
                    if (pos != 1) {
                        viewState.scrollRvTo(pos)
                    }
                }, { handleError(it, rm) })
            }
        }
        viewState.setEmptyListState(finalList.isEmpty())
        viewState.showLoading(false)
        viewState.setEmptyListState(finalList.isEmpty())
    }

    fun currentTaskButtonClicked() {
        router.navigateTo(Screens.Task())
    }

    fun dumpingButtonClicked() {
        viewState.showDumpingConfirmationDialog()
    }

    fun dumpingConfirmed() {
        viewState.setLoadingState(true)
        launch {
            val polygons = routeInteractor.getPolygonsList()
            handleResult(polygons,
                {
                    viewState.showPolygonSelectionDialog(it.data)
                    viewState.enableButton(true)
                }, {
                    handleError(it, rm)
                    viewState.enableButton(true)
                })

            viewState.setLoadingState(false)
        }
    }

    //При старте разгруски проверяет если вес был отправлен то отпровляется на новую разгруску
    // Если нет то повторно отпровляет вес
    fun onPolygonSelected(visitPoint: VisitPoint) {
        if (routeInteractor.isStandResult.value == null
            && routeInteractor.isLocalCurrentTaskCache.value == null
        ) {
            viewState.showPolygonEnsureDialog(visitPoint)
        } else {
            viewState.showMessage("Предыдущий вес не был выгружен. Попробуйте позднее.")
        }
    }

    @DelicateCoroutinesApi
    fun onPolygonEnsureDialogCancelled() {
        // TODO: 07.07.2022 Добавил сокет
        webSocket.startListen()
        dumpingConfirmed()
    }

    fun onSettingsClicked() {
        viewState.showSettingsMenu(true)
    }

    fun isVisibilityNext(boolean: Int) {
        settingsPrefs.visibilityNext = boolean
    }

    private fun updateTime() {
        val dateFormat = SimpleDateFormat("HH:mm")
        val time = Calendar.getInstance().time
        viewState.showCurrentTime(dateFormat.format(time))
    }

    fun updateBatteryLevel(string: String) {
        viewState.showBatteryLevel(string)
    }

    fun onPolygonEnsured(visitPoint: VisitPoint) {
        viewState.setLoadingState(true)
        launch {
            val result = routeInteractor.startToPolygon(visitPoint.id)
            handleResult(result, {
                viewState.setTasksList(converterMappers.relationsTuTaskExtended(it.data))
                router.newRootScreen(Screens.Dumping)
            }, {
                handleError(it, rm)
            })
            viewState.setLoadingState(false)
        }
    }

    fun nearStandsToggleButtonPressed(isChecked: Boolean, lat: Double, lon: Double) {
        viewState.setLoadingState(true)
        if (isChecked) {
            launch {
                val res = routeInteractor.getNearStands(lat, lon)
                handleResult(res, {
                    isNearFilterOn = true
                    localNearStandsList = it.data
                    loadAndSetTasksList()
                }, {
                    handleError(it, rm)
                })
                viewState.setLoadingState(false)
            }
        } else {
            isNearFilterOn = false
            loadAndSetTasksList()
            viewState.setLoadingState(false)
        }

    }

    fun taskFromListChosen(task: TaskRelations) {
        if (task.task.visitPoint != null) return

        when {
            task.task.isCurrent -> {
                router.replaceScreen(Screens.Task())
            }
            task.task.statusType != StatusType.NEW -> {
                router.navigateTo(Screens.TaskCompleted(task.task.id, taskItemCon))
            }
            else -> {
                router.navigateTo(Screens.Task(task.task.id))
            }
        }
    }

    fun onSearchQueryChanged(text: String?) {
        launch {
            query = text
            loadAndSetTasksList(true)
        }
    }
}
