package ru.telecor.gm.mobile.droid.ui.garbageload.fragment.fragments

import android.os.Build
import android.os.Bundle
import android.text.InputFilter
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.warkiz.widget.IndicatorSeekBar
import com.warkiz.widget.OnSeekChangeListener
import com.warkiz.widget.SeekParams
import kotlinx.android.synthetic.main.drop_down_list.view.*
import kotlinx.android.synthetic.main.fragment_garbage_load.*
import kotlinx.android.synthetic.main.fragment_successfully.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import ru.telecor.gm.mobile.droid.R
import ru.telecor.gm.mobile.droid.di.Scopes
import ru.telecor.gm.mobile.droid.entities.db.ProcessingPhoto
import ru.telecor.gm.mobile.droid.entities.photo.GarbagePhotoModel
import ru.telecor.gm.mobile.droid.entities.task.StatusTaskExtended
import ru.telecor.gm.mobile.droid.extensions.inputFilterNumberRange
import ru.telecor.gm.mobile.droid.presentation.successfully.SuccessfullyLoadView
import ru.telecor.gm.mobile.droid.presentation.successfully.SuccessfullyPresenter
import ru.telecor.gm.mobile.droid.ui.base.BaseFragment
import ru.telecor.gm.mobile.droid.ui.garbageload.rv.GalleryDetailCon
import ru.telecor.gm.mobile.droid.utils.DecimalDigitsInputFilter
import ru.telecor.gm.mobile.droid.utils.InputFilterMinMax
import toothpick.Toothpick

class SuccessfullyFragment(
    var listener: SuccessfullyListener,
    var item: StatusTaskExtended? = null,
) : BaseFragment(), SuccessfullyLoadView {

    private var progressValue = 0.0
    private var repeatedVisibility = false
    private var mapList: ArrayList<ProcessingPhoto> = arrayListOf()

    @InjectPresenter
    lateinit var presenter: SuccessfullyPresenter

    @ProvidePresenter
    fun providePresenter(): SuccessfullyPresenter {
        return Toothpick.openScope(Scopes.SERVER_SCOPE)
            .getInstance(SuccessfullyPresenter::class.java)
    }

    override val layoutRes = R.layout.fragment_successfully

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logicalChain()
//        initVisibility(EDIT)
        initClick()
        initProgress()
    }

    //Прогрес бар
    private fun initProgress() {
        indicator_seekBar.apply {
            indicator_seekBar.customTickTexts(arrayOf("Пустой"))
            setDecimalScale(2)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                tickMarksColor(resources.getColorStateList(R.color.green_text, null))
            }
            customSectionTrackColor { colorIntArr ->
                colorIntArr?.set(0, resources.getColor(R.color.empty_one))
                colorIntArr?.set(1, resources.getColor(R.color.empty_two))
                colorIntArr?.set(2, resources.getColor(R.color.empty_three))
                colorIntArr?.set(3, resources.getColor(R.color.empty_four))
                colorIntArr?.set(4, resources.getColor(R.color.empty_five))
                true
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            indicator_seekBar.onSeekChangeListener = object : OnSeekChangeListener {
                override fun onSeeking(seekParams: SeekParams?) {
                    if (seekParams?.thumbPosition == 0) {
                        indicator_seekBar.apply {
                            customTickTexts(arrayOf("Пустой"))
                            progressValue = 0.0
                            tickTextsColor(resources.getColor(R.color.gray, null))
                            thumbColor(resources.getColor(R.color.gray, null))
                            setThumbDrawable(
                                ResourcesCompat.getDrawable(
                                    resources,
                                    R.drawable.ripple_selector0,
                                    null
                                )
                            )
                        }
                    }
                    if (seekParams?.thumbPosition == 1) {
                        indicator_seekBar.apply {
                            customTickTexts(arrayOf("", "Четверть"))
                            progressValue = 0.25
                            tickTextsColor(resources.getColor(R.color.empty_one, null))
                            thumbColor(resources.getColor(R.color.empty_one, null))
                            setThumbDrawable(
                                ResourcesCompat.getDrawable(
                                    resources,
                                    R.drawable.ripple_selector1,
                                    null
                                )
                            )
                        }
                    }
                    if (seekParams?.thumbPosition == 2) {
                        indicator_seekBar.apply {
                            customTickTexts(arrayOf("", "", "Наполовину"))
                            progressValue = 0.5
                            tickTextsColor(resources.getColor(R.color.empty_two, null))
                            thumbColor(resources.getColor(R.color.empty_two, null))
                            setThumbDrawable(
                                ResourcesCompat.getDrawable(
                                    resources,
                                    R.drawable.ripple_selector,
                                    null
                                )
                            )
                        }
                    }
                    if (seekParams?.thumbPosition == 3) {
                        indicator_seekBar.apply {
                            customTickTexts(arrayOf("", "", "", "Неполный"))
                            progressValue = 0.75
                            tickTextsColor(resources.getColor(R.color.empty_three, null))
                            thumbColor(resources.getColor(R.color.empty_three, null))
                            setThumbDrawable(
                                ResourcesCompat.getDrawable(
                                    resources,
                                    R.drawable.ripple_selector2,
                                    null
                                )
                            )
                        }
                    }
                    if (seekParams?.thumbPosition == 4) {
                        indicator_seekBar.apply {
                            customTickTexts(arrayOf("", "", "", "", "Полный"))
                            progressValue = 1.0
                            tickTextsColor(resources.getColor(R.color.empty_four, null))
                            thumbColor(resources.getColor(R.color.empty_four, null))
                            setThumbDrawable(
                                ResourcesCompat.getDrawable(
                                    resources,
                                    R.drawable.ripple_selector3,
                                    null
                                )
                            )
                        }
                    }
                    if (seekParams?.thumbPosition == 5) {
                        indicator_seekBar.apply {
                            customTickTexts(arrayOf("", "", "", "", "", "Переполнен"))
                            progressValue = 1.25
                            tickTextsColor(resources.getColor(R.color.empty_five, null))
                            thumbColor(resources.getColor(R.color.empty_five, null))
                            setThumbDrawable(
                                ResourcesCompat.getDrawable(
                                    resources,
                                    R.drawable.ripple_selector4,
                                    null
                                )
                            )
                        }
                    }
                }

                override fun onStartTrackingTouch(seekBar: IndicatorSeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: IndicatorSeekBar?) {
                }
            }
        }
    }

    private fun logicalChain() {
        if (!repeatedVisibility) {
            when (item!!.rule) {
                "SUCCESS_FAIL" -> {
                    successFail()
                }
                "SUCCESS_FAIL_FILLING" -> {
                    successFailFilling()
                }
                "SUCCESS_FAIL_MANUAL_VOLUME" -> {
                    successFailManualVolume()
                }
                "SUCCESS_FAIL_MANUAL_COUNT_WEIGHT" -> {
                    successFailManualCountWeight()
                }
            }
            repeatedVisibility = true
        }
    }

    private fun successFail() {
        textTextLayout.isVisible = true
        progressValue = 1.0
    }

    private fun successFailFilling() {
        layoutSeekBar.isVisible = true
    }

    private fun successFailManualVolume() {
        layoutEdit.isVisible = true
        ilPersonnelNumber.hint = "Объём, м³"
        //Увеличил количество символов для ввода объема
        etPersonnelNumber.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(3, 2), InputFilterMinMax(0.0, 99.99))
        etPersonnelNumber.addTextChangedListener {
            if (it.toString().isNotEmpty()) {
                progressValue = it.toString().toDouble()
            }
        }
    }

    private fun successFailManualCountWeight() {
        layoutEdit.isVisible = true
        ilPersonnelNumber.hint = "Масса, кг"
        etPersonnelNumber.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(5, 2), InputFilterMinMax(0.0, 1000.99))
        etPersonnelNumber.addTextChangedListener {
            if (it.toString().isNotEmpty()) {
                progressValue = it.toString().toDouble()
            }
        }
    }

    private fun initClick() {
        btnItemOkSuccessfully.setOnClickListener {
//            if (progressValue != 0.0){
//                repeatedVisibility = false
//                listener.setOnClickListenerProgress(progressValue.toString().toDoubleOrNull() ?: 0.0)
//                progressValue = 0.0
//                bottomSheetDialogFragment.dismiss()
//            }

            repeatedVisibility = false
            listener.setOnClickListenerProgress(progressValue.toString().toDoubleOrNull() ?: 0.0)
            progressValue = 0.0
        }

        btnItemCancelSuccessfully.setOnClickListener {
            repeatedVisibility = false
            listener.setClickCancellation(repeatedVisibility)
        }
    }

    override fun showPhoto(model: ArrayList<GarbagePhotoModel>) {
        mapList.clear()
        val map: HashMap<String, ProcessingPhoto> = hashMapOf()
        val adapter = GalleryDetailCon(item) {
            presenter.onPhotoDeleteClicked(it)
            listener.photoSize(mapList.size)
        }

        model.forEach { photo ->
            if (presenter.sorting(photo.type)) {
                photo.item.map { idTs ->
                    if (idTs.conId.find { it == item?.id?.toLong() } != null) {
                        if (photo.type != "CONTAINER_TROUBLE") {
                            map[item?.id.toString()] = idTs
                            mapList.add((map[item?.id?.toString()] ?: 0) as ProcessingPhoto)
                        }
                    }
                }
            }
        }
        adapter.update(mapList)
        listener.photoSize(mapList.size)
        galleryRecyclerView.adapter = adapter
    }

    interface SuccessfullyListener {
        fun setOnClickListenerProgress(value: Double)
        fun setClickCancellation(boolean: Boolean)
        fun photoSize(size: Int)
    }
}