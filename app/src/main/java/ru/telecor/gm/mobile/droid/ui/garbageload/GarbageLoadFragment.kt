package ru.telecor.gm.mobile.droid.ui.garbageload

import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.viewpager.widget.ViewPager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_garbage_load.*
import kotlinx.android.synthetic.main.fragment_garbage_load.btnNavigator
import kotlinx.android.synthetic.main.fragment_garbage_load.btnSettings
import kotlinx.android.synthetic.main.fragment_garbage_load.btnToDiscovery
import kotlinx.coroutines.launch
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import ru.telecor.gm.mobile.droid.R
import ru.telecor.gm.mobile.droid.di.Scopes
import ru.telecor.gm.mobile.droid.entities.ContainerFailureReason
import ru.telecor.gm.mobile.droid.entities.ContainerLoadLevel
import ru.telecor.gm.mobile.droid.entities.db.ProcessingPhoto
import ru.telecor.gm.mobile.droid.entities.db.TaskExtended
import ru.telecor.gm.mobile.droid.entities.photo.GarbagePhotoModel
import ru.telecor.gm.mobile.droid.entities.processing.ProcessingStatusType
import ru.telecor.gm.mobile.droid.entities.processing.StandResult
import ru.telecor.gm.mobile.droid.entities.task.StatusTaskExtended
import ru.telecor.gm.mobile.droid.extensions.visible
import ru.telecor.gm.mobile.droid.model.PhotoType
import ru.telecor.gm.mobile.droid.presentation.garbageload.GarbageLoadPresenter
import ru.telecor.gm.mobile.droid.presentation.garbageload.GarbageLoadView
import ru.telecor.gm.mobile.droid.presentation.garbageload.util.GarbageLoadScreenState
import ru.telecor.gm.mobile.droid.ui.base.BaseFragment
import ru.telecor.gm.mobile.droid.ui.garbageload.fragment.fragments.ContainerGalleryFragment
import ru.telecor.gm.mobile.droid.ui.garbageload.fragment.fragments.ProblemFragment
import ru.telecor.gm.mobile.droid.ui.garbageload.fragment.fragments.SuccessfullyFragment
import ru.telecor.gm.mobile.droid.ui.garbageload.fragment.message.MessageErrorFragment
import ru.telecor.gm.mobile.droid.ui.garbageload.fragment.message.MessageErrorMassActionFragment
import ru.telecor.gm.mobile.droid.ui.garbageload.fragment.message.RouteCompletionFragment
import ru.telecor.gm.mobile.droid.ui.garbageload.fragment.message.RouteResettingFragment
import ru.telecor.gm.mobile.droid.ui.garbageload.pager.PhotoPagerAdapter
import ru.telecor.gm.mobile.droid.ui.garbageload.pager.SelectionPagerAdapter
import ru.telecor.gm.mobile.droid.ui.garbageload.rv.AdapterPhotoContainer
import ru.telecor.gm.mobile.droid.ui.garbageload.rv.GarbageContainerAdapter
import ru.telecor.gm.mobile.droid.ui.garbageload.rv.GarbageContainerInfoAdapter
import ru.telecor.gm.mobile.droid.ui.login.fragment.setting.settingFunctionality.SettingBottomSheetFragment
import ru.telecor.gm.mobile.droid.ui.photo.PhotoActivity
import ru.telecor.gm.mobile.droid.ui.photomake.PhotoMakeGeneralActivity
import ru.telecor.gm.mobile.droid.ui.phototrouble.PhotoTroubleActivity
import ru.telecor.gm.mobile.droid.ui.utils.LocationUtils
import ru.telecor.gm.mobile.droid.ui.viewingPhoto.ViewingPhotoFragment
import ru.telecor.gm.mobile.droid.utils.Animation.slideUp
import ru.telecor.gm.mobile.droid.utils.conect
import ru.telecor.gm.mobile.droid.utils.getPositionNetwork
import ru.telecor.gm.mobile.droid.utils.setShortName
import ru.telecor.gm.mobile.droid.utils.сomponent.cookiebar.CookieBar
import toothpick.Toothpick
import kotlin.collections.ArrayList

class GarbageLoadFragment : BaseFragment(), GarbageLoadView {
    private lateinit var listStatus: List<StatusTaskExtended>
    private lateinit var listContainer: List<ContainerLoadLevel>
    private lateinit var localFailureReasonsCache: List<ContainerFailureReason>
    private lateinit var adapters: GarbageContainerAdapter
    private lateinit var adapterInfo: GarbageContainerInfoAdapter
    private lateinit var taskItem: StatusTaskExtended
    private var mTaskItem: StatusTaskExtended? = null
    private var itemMore: ArrayList<StatusTaskExtended> = arrayListOf()
    private var rule = ""
    private var mNameStatus = ""
    private var addressTxt = ""
    private var restartRecycler = true
    private var fragOpen = true
    private var repeatAnimation = false

    private var numberBar = 0
    private var sizeBarProblem = 0
    private var sizeBarSuccessfully = 0
    private var sizeBar = 3
    private var namePhotoValid = ""

    private var mHandler: Handler = Handler()

    companion object {
        fun newInstance() = GarbageLoadFragment()
    }

    override val layoutRes = R.layout.fragment_garbage_load

    @InjectPresenter
    lateinit var presenter: GarbageLoadPresenter

    @ProvidePresenter
    fun providePresenter(): GarbageLoadPresenter {
        return Toothpick.openScope(Scopes.SERVER_SCOPE)
            .getInstance(GarbageLoadPresenter::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        initClick()

        setShortName("")

        adapterInfo =
            GarbageContainerInfoAdapter(object : GarbageContainerInfoAdapter.GarbageInfoListenerState {
                override fun setOnPhotoClickListener(item: StatusTaskExtended) {
                    val bottomSheetDialogFragment = ContainerGalleryFragment(object :
                        ContainerGalleryFragment.ListenerCompleteRoute {
                        override fun getDeletePhoto(photo: ProcessingPhoto) {
                            presenter.onPhotoDeleteClicked(photo)
                        }
                    }, presenter.modelPhoto, item, listStatus)
                    bottomSheetDialogFragment.show(
                        requireActivity().supportFragmentManager,
                        bottomSheetDialogFragment.tag
                    )
                }
            })

        //RecyclerView
        adapters = GarbageContainerAdapter(object :
            GarbageContainerAdapter.GarbageListenerState {
            override fun setOnClickListener(
                item: StatusTaskExtended,
                list: ArrayList<StatusTaskExtended>
            ) {
                if (itemMore.firstOrNull { it.id == item.id } == null) {
                    itemMore.add(item)
                    presenter.detailedPhoto(itemMore, arrayListOf())
                }

                if (restartRecycler) {
                    setRecyclerData(item)
                    restartRecycler = false
                }

                if (generalCheck.isChecked) {
                    disablingCheckbox()
                    generalCheck.isChecked = false
                    itemMore.clear()
                }

                presenter.deleteTaskCon(itemMore, false)
            }

            override fun setOnClickListenerCheck(
                isCheck: Boolean,
                item: StatusTaskExtended,
                list: ArrayList<StatusTaskExtended>
            ) {
                if (itemMore.size != 0) {
                    if (itemMore.indexOfFirst { it.id == item.id } != -1) {
                        itemMore.remove(item)
                        presenter.detailedPhoto(itemMore, arrayListOf())
                    }
                    if (mNameStatus == "all") {
                        setRecyclerData(item)
                    }
                }
                generalCheck.isChecked = false
                disablingCheckbox(list)
            }
        })

        //Временная мера для получения метода.
        btnToDiscovery.setOnClickListener {
            fragOpen = true
            presenter.taskDoneButtonClicked()
        }

        getPositionNetwork(requireActivity())

        btnPhotoBefore.setOnClickListener {
            presenter.photoBeforeButtonClicked(LocationUtils.getBestLocation(requireContext()))
        }
        btnPhotoAfter.setOnClickListener {
            presenter.photoAfterButtonClicked(LocationUtils.getBestLocation(requireContext()))
        }

        btnToRoute.setOnClickListener {
            presenter.getRoutes()
        }
        btnSettings.setOnClickListener { presenter.onSettingsClicked() }
        btnNavigator.setOnClickListener { presenter.routeButtonClicked() }
        onBeckPress.setOnClickListener {
            presenter.viewState.onBeckPressed()
        }
        btnPhotoProblem.setOnClickListener { presenter.photoProblemButtonClicked() }

        //Click масового выбора
        generalCheck.setOnClickListener {
            val listStatusDel: ArrayList<StatusTaskExtended> = arrayListOf()
            if (listStatus.size > 1) {
                if (rule == "") {
                    rule = listStatus[0].rule!!
                    saveValues()
                } else {
                    val status = listStatus.firstOrNull { it.rule != rule }
                    if (status != null) {
                        val bottomSheetDialogFragment = MessageErrorMassActionFragment()
                        bottomSheetDialogFragment.show(
                            requireActivity().supportFragmentManager,
                            bottomSheetDialogFragment.tag
                        )
                    } else {
                        saveValues()
                    }
                }
            } else {
                saveValues()
            }
            slideUp(conPhotoDet)
            presenter.detailedPhoto(arrayListOf(), listStatus)
            listStatus.last { listStatusDel.add(it) }
            presenter.deleteTaskCon(listStatusDel, false, "all")
            sizeBar = 3
        }

        btnQrScanner.setOnClickListener {
            presenter.onQrCodeScannerButtonClicked()
        }

        containerMenu.setOnClickListener {
            setOptionsMenu(it)
        }
    }

    //Получает выподающий список и стутс выполения задания
    override fun setState(state: GarbageLoadScreenState) {
        listContainer = state.levelsList ?: arrayListOf()
        listStatus = state.list ?: arrayListOf()

        listStatus = listStatus.sortedByDescending { it3 ->
            it3.containerStatus?.allGroupContainersId?.toString()?.toCharArray()?.let { String(it) }
        }

        listStatus = listStatus.sortedWith(compareBy({
            it.containerStatus?.allGroupContainersId?.toString()?.toCharArray()?.let { String(it) }
        }, { it.id }))
        localFailureReasonsCache = state.localFailureReasonsCache!!

        //После полуния данных отключает все че боксы.
        generalCheck.isChecked = false
        defaultValue(generalCheck.isChecked)
    }

    //Функция обработки единичный выбор или массовый
    private fun setRecyclerData(item: StatusTaskExtended? = null) {
        if (item == null) {
            if (searchCopies(listStatus) == "") {
                getRule(listStatus[0], "all")
            }
        } else {
            getRule(item, "none")
            taskItem = item
        }
    }

    //Получение данных поле тесных отношений клёцки с тепсиком
    private fun getRule(item: StatusTaskExtended? = null, name: String) {
        viewPagerRoute(item, name)
    }

    private fun defaultValue(boolean: Boolean) {
        //Предаю в адаптер выбраные id
        adapters.listReceipts(itemMore)

        generalCheck.isChecked = boolean
        adapters.setCheck(boolean)
        setFillingAdapter()
    }

    private fun viewPagerRoute(item: StatusTaskExtended? = null, nameStatus: String) {

        namePhotoValid = ""
        mNameStatus = nameStatus
        if (item != null) {
            mTaskItem = item
        }
        orinDevice(3)
        if (nameStatus != "all") {
            if (item?.containerStatus?.containerFailureReason == null) {
                pagerStatus()
            } else {
               orinDevice(3)
            }
        } else {
            pagerStatus()
        }

        val adapter = SelectionPagerAdapter(childFragmentManager)
        adapter.addFragment(ProblemFragment(object : ProblemFragment.ProblemListener {
            override fun setOnClickListenerProgress(value: Int) {
                if (mNameStatus == "all") {
                    presenter.onTroubleReasonChosen(
                        listStatus,
                        localFailureReasonsCache[value],
                        listStatus.size
                    )
                } else if (nameStatus == "none") {
                    presenter.onTroubleReasonChosen(
                        itemMore,
                        localFailureReasonsCache[value],
                        itemMore.size
                    )
                }
                itemMore.clear()
                booleanRepeat()
                conDisplaysFalse()
                presenter.saveRoute()
            }

            override fun setClickCancellation(boolean: Boolean) {
                defaultValue(boolean)
                booleanRepeat()
                conDisplaysFalse()
                itemMore.clear()
            }

            override fun setClearPhoto(photo: ProcessingPhoto) {
                presenter.setCleatPhoto(photo)
            }

            override fun photoSize(size: Int) {
                sizeBarProblem = if (size != 0) size else 0
                if (sizeBarProblem == 0 && sizeBarSuccessfully == 0){
                    sizeBar = 3
                    orinDevice(sizeBar)
                }else{
                    sizeBar = 2
                    orinDevice(sizeBar)
                }
            }

            //меняет цвет кнопки при фиксации фото проблемы
            override fun setChangeColor(nameString: String) {
                namePhotoValid = nameString
                setBackgroundImage()
            }
        }, localFailureReasonsCache, mTaskItem), "ПРОБЛЕМА")

        adapter.addFragment(SuccessfullyFragment(object :
            SuccessfullyFragment.SuccessfullyListener {
            override fun setOnClickListenerProgress(value: Double) {
                if (mNameStatus == "all") {
                    presenter.setFillingModel(value, ArrayList(listStatus), listContainer)

                } else if (mNameStatus == "none") {
                    presenter.setFillingModel(value, itemMore, listContainer)
                }
                itemMore.clear()
                booleanRepeat()
                conDisplaysFalse()
                presenter.saveRoute()
            }

            override fun setClickCancellation(boolean: Boolean) {
                defaultValue(boolean)
                booleanRepeat()
                conDisplaysFalse()
                itemMore.clear()
            }

            override fun photoSize(size: Int) {
                sizeBarSuccessfully = if (size != 0) size else 0
                sizeBarProblem = if (size != 0) size else 0
                if (sizeBarProblem == 0 && sizeBarSuccessfully == 0){
                    sizeBar = 3
                    orinDevice(sizeBar)
                }else{
                    sizeBar = 2
                    orinDevice(sizeBar)
                }
            }
        }, item = mTaskItem), "УСПЕШНО")

        profilePager.adapter = adapter

        numButtonOne.setOnClickListener {
            profilePager.currentItem = 0
            adapter.notifyDataSetChanged()
            // TODO: Это надо вырезать
           orinDevice(sizeBar)
        }

        numButtonTwo.setOnClickListener {
            profilePager.currentItem = 1
            adapter.notifyDataSetChanged()
            // TODO: Это надо вырезать
           orinDevice(sizeBar)
        }

        profile_bar_zero.visibility = View.VISIBLE
        profile_bar_one.visibility = View.GONE
        photoDisplay(true)

        profilePager.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                numberBar = position
            }

            override fun onPageSelected(position: Int) {
                val fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
                val fadeOut = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_out)
                if (position != 1) {
                    profile_bar_zero.startAnimation(fadeIn)
                    profile_bar_one.startAnimation(fadeOut)
                    profile_bar_zero.visibility = View.VISIBLE
                    profile_bar_one.visibility = View.GONE
                    photoDisplay(true)
                } else {
                    profile_bar_one.startAnimation(fadeIn)
                    profile_bar_zero.startAnimation(fadeOut)
                    profile_bar_one.visibility = View.VISIBLE
                    profile_bar_zero.visibility = View.GONE
                    photoDisplay(false)
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        conDisplaysTrue()
    }

    //Меняю значение кнопок фиксации фото по типу
    fun photoDisplay(boolean: Boolean){
        if (boolean) {
            detailsGallery.isVisible = true
            detailsBefore.isVisible = false
            detailsAfter.isVisible = false
            detailsGallery.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.grey_button_styles_end_start)
        }else{
            detailsBefore.isVisible = true
            detailsAfter.isVisible = true
            detailsGallery.isVisible = false
            detailsBefore.background = ContextCompat.getDrawable(requireContext(), R.drawable.blue_button_styles_start)
            detailsAfter.background = ContextCompat.getDrawable(requireContext(), R.drawable.blue_button_styles_end)
        }
        setBackgroundImage()
    }

    //проверяет кнопку со сменой цвета на пустоту
    fun setBackgroundImage(){
        if (namePhotoValid != ""){
            detailsGallery.isEnabled = true
            detailsGallery.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.blue_button_styles_end_start)
        }
    }

    // TODO: Это надо вырезать
    //Функция самостоятельно проверяет оринтацию
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            pagerCon()
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            pagerCon()
        }
    }

    // TODO: Это надо вырезать
    private fun pagerCon() {
        if (profilePager.currentItem == 1) {
           orinDevice(sizeBar)
        } else {
           orinDevice(sizeBar)
        }
    }

    // TODO: Это надо вырезать
    //При повороте устонавливает размеры
    private fun orinDevice(int: Int) {
        val paramsPager = profilePager.layoutParams as LinearLayout.LayoutParams
        val params = linearLayout.layoutParams as LinearLayout.LayoutParams
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            params.height = presenter.settingsPrefs.isLayoutWhite / int - 120
            paramsPager.height = presenter.settingsPrefs.isLayoutWhite / int - 120
        } else {
            params.height = presenter.settingsPrefs.isLayoutHeight / int - 120
            paramsPager.height = presenter.settingsPrefs.isLayoutHeight / int - 120
        }
    }

    // TODO: Это надо вырезать
    private fun pagerStatus() {
        Handler().postDelayed({
            profilePager.currentItem = 1
           orinDevice(sizeBar)
        }, 20)
    }

    private fun searchCopies(list: List<StatusTaskExtended>): String {
        for (i in 0 until list.size - 1) {
            if (list[i].rule != list[i + 1].rule) {
                return list[i].rule.toString()
            }
        }
        return ""
    }

    //Закрывает оно с маршрутом
    private fun disablingCheckbox(list: ArrayList<StatusTaskExtended>) {
        if (list.size == 0) {
            defaultValue(false)
            booleanRepeat()
            conDisplaysFalse()
        }
    }

    //Закрывает оно с маршрутом
    private fun disablingCheckbox() {
        defaultValue(false)
        booleanRepeat()
        conDisplaysFalse()
    }

    private fun initClick() {
        //Всплывающее сообщение с адресом
        tvGarbageLoadAddress.setOnClickListener {
            if (addressTxt != "") {
                CookieBar.build(activity)
                    .setTitle(addressTxt)
                    .show()
            } else if (addressTxt == "") {
                showMessage("Адрес пуст")
            }
        }
        tvGarbageLoadAddress.isSelected = true

        detailsAfter.setOnClickListener {
            presenter.photoBeforeConClickedAfter(LocationUtils.getBestLocation(requireContext()))
        }
        detailsGallery.setOnClickListener {
            presenter.photoBeforeConClickedTrouble(LocationUtils.getBestLocation(requireContext()))
        }
        detailsBefore.setOnClickListener {
            presenter.photoBeforeConClicked(LocationUtils.getBestLocation(requireContext()))
        }
    }

    private fun saveValues() {
        if (generalCheck.isChecked) {
            adapters.setCheck(true)
            adapters.setTasks(listStatus)
            if (itemMore.size == 0) {
                itemMore.addAll(listStatus)
            }
            adapterInfo.setTasks(listStatus)
            if (itemMore.size == 0) {
                itemMore.addAll(listStatus)
            }
            generalCheck.isChecked = true
            setFillingAdapter()
        } else {
            adapters.setCheck(false)
            generalCheck.isChecked = false
            setFillingAdapter()
            conDisplaysFalse()
        }

        if (generalCheck.isChecked) {
            setRecyclerData()
        }
    }

    //Заполняет адаптеры данными
    private fun setFillingAdapter(){
        adapters.update(listStatus)
        adapterInfo.update(listStatus)
        rvGarbageContainersInfo.adapter = adapterInfo
        rvGarbageContainers.adapter = adapters
    }

    private fun setOptionsMenu(it: View) {
        val popupMenu = PopupMenu(requireActivity(), it)


        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.problemExportMenu -> {
                    presenter.onAddProblemButtonClicked(LocationUtils.getBestLocation(requireContext()))
                    true
                }
                R.id.bulkSiteMenu -> {
                    presenter.onAddBlockageButtonClicked(
                        LocationUtils.getBestLocation(requireContext())
                    )
                    true
                }
                else -> false
            }
        }
        popupMenu.inflate(R.menu.bottom_bar_menu)
        popupMenu.show()
    }

    //Открытие настроек
    override fun showSettingsMenu(boolean: Boolean) {
        presenter.isVisibilityNext(View.VISIBLE)
        if (boolean) {
            val bottomSheetDialogFragment = SettingBottomSheetFragment()
            bottomSheetDialogFragment.show(
                requireActivity().supportFragmentManager,
                bottomSheetDialogFragment.tag
            )
            presenter.viewState.showSettingsMenu(false)
        }
    }

    override fun onBeckPressed() {
        requireActivity().onBackPressed()
    }

    override fun setBeforePhotoButtonCompleted() {
        btnPhotoBefore.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), R.color.colorAccent)
    }

    override fun setAfterPhotoButtonCompleted() {
        btnPhotoAfter.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), R.color.colorAccent)
    }

    override fun setProblemPhotoButtonCompleted() {
        btnPhotoProblem.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), R.color.design_default_color_error)
    }

    override fun setAddButtonVisibility(value: Boolean) {
        btnAddGarbageTask.visible(value)
    }

    override fun setTaskInfo(name: String) {
        tvGarbageLoadAddress.text = name
        addressTxt = name
    }

    //Закрывает контенер и выполение задания
    override fun clearModelGroup() {
        defaultValue(false)
        booleanRepeat()
        conDisplaysFalse()
        itemMore.clear()
    }

    // При старте проверю кнопку
    // Если нет выполненых заданий то жопа иначе ты мусорщик (решай сам)
    override fun setActionCompletedBottom(boolean: Boolean?) {
        if (boolean == false) {
            btnToDiscovery.isEnabled = boolean
            btnToDiscovery.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.gray
                )
            )
            if (layoutSelectCapacity.isVisible) {
                mHandler.postDelayed({
                    layoutSelectCapacity.isVisible = false
                }, 3000)
            }

        } else {
            layoutSelectCapacity.isVisible = false
            btnToDiscovery.isEnabled = boolean == true
            btnToDiscovery.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.white_green_button_style)
        }
    }

    //Последовательность отоброжения полей при роверке фото
    private fun setActionCompletedPhoto(con: Boolean, res: Boolean) {
        layoutGalleryCon.visible(con)
        recyclerViewGarbage.visible(res)
    }

    override fun showContainerTroubleDialog(
        taskStatus: StatusTaskExtended,
        list: List<ContainerFailureReason>
    ) {
        MaterialAlertDialogBuilder(context!!)
            .setTitle(resources.getString(R.string.garbage_load_dialog_trouble_reason_label))
            .setItems(list.map { item -> item.name }.toTypedArray()) { _, position ->
                presenter.onTroubleReasonChosen(
                    arrayListOf(taskStatus),
                    list[position],
                    arrayListOf(taskStatus).size
                )
            }
            .show()
    }

    override fun showContainerLevelDialog(
        taskStatus: StatusTaskExtended,
        levelsList: List<ContainerLoadLevel>
    ) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Уровень загрузки")
            .setItems(levelsList.map { item -> item.title }.toTypedArray()) { _, position ->
                presenter.elementLoadLevelChosen(taskStatus, levelsList[position], size = 0)
            }
            .show()
    }

    override fun showPhoto(model: ArrayList<GarbagePhotoModel>) {
        var lists: ArrayList<GarbagePhotoModel> = arrayListOf()
        val adapters = AdapterPhotoContainer(deleteOption = { photo ->
            presenter.onPhotoDeleteClicked(photo)
        }, photo = {
            presenter.saveRoute()
            photoPager.isVisible = true
            viewPager(it)
        })
        model.map {
            if (presenter.sorting(it.type)) {
                lists = model
                adapters.update(model)
                adapters.notifyDataSetChanged()
            }
            recyclerViewGarbage.adapter = adapters
        }

        if (lists.size != 0) {
            setActionCompletedPhoto(false, res = true)
        } else {
            setActionCompletedPhoto(true, res = false)
        }
    }

    private fun viewPager(photo: ProcessingPhoto? = null) {
        val adapter = PhotoPagerAdapter(childFragmentManager)
        adapter.addFragment(
            ViewingPhotoFragment(
                photo,
                true,
                object : ViewingPhotoFragment.Listener {
                    override fun setOnClickClearPhoto() {
                        photoPager.isVisible = false
                    }
                })
        )
        photoPager.adapter = adapter
    }

    override fun setLoadingState(value: Boolean) {
        pbLoading.visible(value)
    }

    //Отоброжает различные сообщения
    override fun textMessageError(textMessage: String) {
        if (fragOpen) {
            val bottomSheetDialogFragment = MessageErrorFragment(textMessage)
            bottomSheetDialogFragment.show(
                requireActivity().supportFragmentManager,
                bottomSheetDialogFragment.tag
            )
            fragOpen = false
        }
    }

    //Отоброжение скрытие контенера
    override fun setHidingPanelValid(boolean: Boolean) {
        if (repeatAnimation != boolean) {
            if (!boolean) {
                repeatAnimation = boolean
            } else {
                repeatAnimation = boolean
                slideUp(conPhotoDet)
            }
        }
    }

    var openFragment = false
    //Откурывает фрагмент с коном оповещения на удаление
    override fun setOpeningFragment(status: String?) {
        val listStatusDel: ArrayList<StatusTaskExtended> = arrayListOf()
        val bottomSheetDialogFragment = RouteResettingFragment(object :
            RouteResettingFragment.ListenerCompleteRoute {
            override fun setOnClickRoute() {
                if (status != "all") {
                    presenter.deleteTaskCon(itemMore, true)
                } else {
                    listStatus.last { listStatusDel.add(it) }
                    presenter.deleteTaskCon(listStatusDel, true, status)
                }
                openFragment = false
            }

            override fun setOnClickRouteClear() {
                defaultValue(false)
                booleanRepeat()
                conDisplaysFalse()
                itemMore.clear()
                openFragment = false
            }

            override fun setOpen() {openFragment=true}
        })
        bottomSheetDialogFragment.isCancelable = false
        bottomSheetDialogFragment.show(
            requireActivity().supportFragmentManager,
            bottomSheetDialogFragment.tag
        )
    }

    //Если данные по маршруту верны едим дальше
    override fun setCompleteRoute(
        localTaskCache: TaskExtended,
        statusType: ProcessingStatusType,
        standResults: MutableList<StandResult>
    ) {
        val bottomSheetDialogFragment =
            RouteCompletionFragment(object : RouteCompletionFragment.ListenerCompleteRoute {
                override fun setOnClickRoute() {
                    presenter.launch {
                        presenter.routeInteractor.addTaskToProcessing(
                            localTaskCache,
                            statusType,
                            standResults,
                            null
                        )
                        presenter.router.exit()
                    }
                }
            }, localTaskCache.stand?.address)
        bottomSheetDialogFragment.show(
            requireActivity().supportFragmentManager,
            bottomSheetDialogFragment.tag
        )
    }

    override fun startExternalCameraForResult(path: String) {
        presenter.getStatusPhoto(true)
        getCamera(path)
    }

    //Opening the camera
    private fun getCamera(path: String) {
        withPermission(android.Manifest.permission.CAMERA, {
            if (conect(requireContext(), requireActivity())) {
                startActivityForResult(
                    PhotoMakeGeneralActivity.createIntent(
                        requireActivity(),
                        path,
                        presenter.photoType,
                        presenter.conId
                    ),
                    1
                )
            }
        },
            { showMessage(getString(R.string.error_permission_denied)) })
    }

    override fun takePhoto(routeCode: String, taskId: String, type: PhotoType) {
        startActivityForResult(
            activity?.let { it1 ->
                PhotoActivity.createIntent(it1, type, routeCode, taskId)
            }, type.ordinal
        )
    }

    override fun takeProblemPhoto(routeCode: String, taskId: String) {
        startActivityForResult(
            activity?.let { it1 ->
                PhotoTroubleActivity.createIntent(it1, routeCode, taskId)
            }, 27
        )
    }

    private fun booleanRepeat() {
        presenter.isCheck = false
        restartRecycler = true
    }

    private fun conDisplaysTrue() {
        bottomLayoutCon.isVisible = true
    }

    private fun conDisplaysFalse() {
        bottomLayoutCon.isVisible = false
    }

    override fun setContainerAction(action: String) {
        tvContainerAction.text = action
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mHandler.removeCallbacksAndMessages(null)
    }


}
