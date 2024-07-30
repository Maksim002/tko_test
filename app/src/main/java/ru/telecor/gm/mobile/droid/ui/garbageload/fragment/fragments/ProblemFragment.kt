package ru.telecor.gm.mobile.droid.ui.garbageload.fragment.fragments

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.fragment_problem.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import ru.telecor.gm.mobile.droid.R
import ru.telecor.gm.mobile.droid.di.Scopes
import ru.telecor.gm.mobile.droid.entities.ContainerFailureReason
import ru.telecor.gm.mobile.droid.entities.db.ProcessingPhoto
import ru.telecor.gm.mobile.droid.entities.photo.GarbagePhotoModel
import ru.telecor.gm.mobile.droid.entities.task.StatusTaskExtended
import ru.telecor.gm.mobile.droid.model.PhotoType
import ru.telecor.gm.mobile.droid.presentation.problem.ProblemLoadView
import ru.telecor.gm.mobile.droid.presentation.problem.ProblemPresenter
import ru.telecor.gm.mobile.droid.ui.base.BaseFragment
import ru.telecor.gm.mobile.droid.ui.garbageload.rv.ProblemAdapter
import ru.telecor.gm.mobile.droid.ui.garbageload.fragment.message.MessageErrorFragment
import ru.telecor.gm.mobile.droid.ui.photomake.PhotoMakeGeneralActivity
import ru.telecor.gm.mobile.droid.ui.utils.LocationUtils
import ru.telecor.gm.mobile.droid.utils.conect
import toothpick.Toothpick

class ProblemFragment(
    var listener: ProblemListener,
    var levelsList: List<ContainerFailureReason> = arrayListOf(),
    var item: StatusTaskExtended? = null,
) : BaseFragment(), ProblemLoadView {

    private var name: String = ""

    private val REQUEST_TAKE_PHOTO = 1
    private var repeatedVisibility = false
    private var mapList: ArrayList<ProcessingPhoto> = arrayListOf()

    @InjectPresenter
    lateinit var presenter: ProblemPresenter

    @ProvidePresenter
    fun providePresenter(): ProblemPresenter {
        return Toothpick.openScope(Scopes.SERVER_SCOPE)
            .getInstance(ProblemPresenter::class.java)
    }

    override val layoutRes = R.layout.fragment_problem

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logicalChain()
        initSpinner()
        initClick()
    }

    private fun initClick() {
        //Клик ксли есть позиция для завершения
        btnItemOk.setOnClickListener {
            if (name != ""){
                repeatedVisibility = false
                val listIndex = levelsList.indexOfFirst {it.name == name}
                if (listIndex != -1){
                    listener.setOnClickListenerProgress(listIndex)
                }
            }else{
                val bottomSheetDialogFragment = MessageErrorFragment()
                bottomSheetDialogFragment.show(requireActivity().supportFragmentManager,
                    bottomSheetDialogFragment.tag
                )
            }
        }

        btnTakeImage.setOnClickListener {
            presenter.photoBeforeButtonClicked(LocationUtils.getBestLocation(requireContext()))
        }

        btnItemCancel.setOnClickListener {
            repeatedVisibility = false
            listener.setClickCancellation(false)
        }
    }

    private fun logicalChain() {
        if (!repeatedVisibility){
            when(item!!.rule){
                "SUCCESS_FAIL" ->{
                    successFail()
                }
                "SUCCESS_FAIL_FILLING" ->{
                    successFailFilling()
                }
                "SUCCESS_FAIL_MANUAL_VOLUME" ->{
                    successFailManualVolume()
                }
                "SUCCESS_FAIL_MANUAL_COUNT_WEIGHT" ->{
                    successFailManualCountWeight()
                }
            }
            repeatedVisibility = true
        }
    }

    private fun successFail() {
//        problemDropDownSpinner.isVisible = true
    }

    fun successFailFilling() {

    }

    fun successFailManualVolume() {

    }

    fun successFailManualCountWeight() {

    }

    private fun initSpinner() {
        problemDropDownSpinner.setOnClickListener {
            if (levelsList.isNotEmpty()) {
                problemDropDownSpinner.setValidation()
                for (i in 1..levelsList.size) {
                    problemDropDownSpinner.setUpdate(
                        i - 1, levelsList[i - 1].name,
                        resources.getDrawable(R.drawable.ic_spinner, null)
                    )
                }
            }
        }
        //Устонавливает дефолтное значение
        item?.containerStatus?.containerFailureReason?.name?.let { nameValue ->
            problemDropDownSpinner.defaultValue(
                nameValue
            )
            name = nameValue
        }

        //Возвращает item при клике
        problemDropDownSpinner.clickItemListener.observe(viewLifecycleOwner) { res ->
            name = res.text
            listener.setChangeColor(res.text)
        }
    }

    override fun initRecyclerView(itemPhoto: ArrayList<GarbagePhotoModel>) {
        mapList.clear()
        val map: HashMap<String, ProcessingPhoto> = hashMapOf()
        val adapters = ProblemAdapter { photo ->
            presenter.onPhotoDeleteClicked(photo)
            listener.setClearPhoto(photo)
            listener.photoSize(mapList.size)
        }

        itemPhoto.forEach { photo ->
            if (presenter.sorting(photo.type)) {
                photo.item.map { idTs ->
                    if (idTs.conId.find { it == item?.id?.toLong() } != null) {
                        if (photo.type == "CONTAINER_TROUBLE"){
                            map[item?.id.toString()] = idTs
                            mapList.add((map[item?.id?.toString()]?:  0) as ProcessingPhoto)
                        }
                    }
                }
            }
        }

        adapters.update(mapList)
        listener.photoSize(mapList.size)
        itemPhotoRec.adapter = adapters
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
                        presenter.photoType, arrayListOf()
                    ),
                    REQUEST_TAKE_PHOTO
                )
            }
        },
            { showMessage(getString(R.string.error_permission_denied)) })
    }



    interface ProblemListener {
        fun setOnClickListenerProgress(value: Int)
        fun setClickCancellation(boolean: Boolean)
        fun setClearPhoto(photo: ProcessingPhoto)
        fun photoSize(size: Int)
        fun setChangeColor(nameString: String)
    }
}