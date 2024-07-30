package ru.telecor.gm.mobile.droid.ui.garbageload.rv

import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import ru.telecor.gm.mobile.droid.ui.base.rv.GenericRecyclerAdapter
import ru.telecor.gm.mobile.droid.ui.base.rv.ViewHolder
import kotlinx.android.synthetic.main.item_garbage_container.view.*
import ru.telecor.gm.mobile.droid.R
import ru.telecor.gm.mobile.droid.entities.task.StatusTaskExtended
import ru.telecor.gm.mobile.droid.utils.setShortName

class GarbageContainerAdapter(
    var listener: GarbageListenerState? = null,
    itemRoute: ArrayList<StatusTaskExtended> = arrayListOf()
) : GenericRecyclerAdapter<StatusTaskExtended>(itemRoute) {

    private var isCheck = false
    private var check: ArrayList<StatusTaskExtended> = arrayListOf()
    private var receiptsId: ArrayList<StatusTaskExtended> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return super.onCreateViewHolder(parent, R.layout.item_garbage_container)
    }

    override fun bind(item: StatusTaskExtended, holder: ViewHolder): Unit = with(holder.itemView) {

        //check.clear()

        containerCheck.isChecked = isCheck
        containerTxt.text = item.containerType.name
        garbageStatus.text = setShortName(item.containerAction.toString())
        garbageTxt.text = item.containerGroups?.name

        //Включаю выбраные чак бокчы если обновился ui
        if (receiptsId.firstOrNull { it.id == item.id} != null){
            if (!containerCheck.isChecked) {
                containerCheck.isChecked = true
            }
        }

        if (item.containerStatus != null) {
            if (item.containerStatus.containerFailureReason != null) {
                percentageFillingImageM.isVisible = true
                percentageFilling.isVisible = false
                percentageFillingImage.isVisible = false
                mark.background =
                    resources.getDrawable(R.drawable.circle_red_mark_background_layout, null)
                containerImage.setColorFilter(
                    ContextCompat.getColor(
                        context,
                        R.color.black_to_red_color
                    )
                )

            } else {
                when (item.rule) {
                    "SUCCESS_FAIL" -> {
                        percentageFillingImage.isVisible = true
                        percentageFillingImageM.isVisible = false
                        percentageFilling.isVisible = false
                    }
                    "SUCCESS_FAIL_MANUAL_VOLUME" -> {
                        percentageFillingImage.isVisible = true
                        percentageFillingImageM.isVisible = false
                        percentageFilling.isVisible = false
//                        statusDescription.isVisible = false
                    }
                    "SUCCESS_FAIL_MANUAL_COUNT_WEIGHT" -> {
                        percentageFillingImage.isVisible = true
//                        kilogramText.isVisible = true
//                        kilogramText.text = item.containerStatus.volumeAct!!.toDouble().toString() + " КГ"
                        percentageFillingImageM.isVisible = false
                        percentageFilling.isVisible = false
//                        statusDescription.isVisible = false
//                        weightText.isVisible = false
                    }
                    "SUCCESS_FAIL_FILLING" -> {
                        percentageFilling.isVisible = true
                        percentageFillingImageM.isVisible = false
                        percentageFillingImage.isVisible = false
                        when (item.containerStatus.volumePercent) {
                            0.0 -> {
                                getParameters(R.color.empty_zero, holder, "0", "ПУСТОЙ")
                            }
                            0.25 -> {
                                getParameters(R.color.empty_one, holder, "25", "ЧЕТВЕРТЬ")
                            }
                            0.5 -> {
                                getParameters(R.color.empty_two, holder, "50", "НАПОЛОВИНУ")
                            }
                            0.75 -> {
                                getParameters(R.color.empty_three, holder, "75", "НЕПОЛНЫЙ")
                            }
                            1.0 -> {
                                getParameters(R.color.empty_four, holder, "100", "ПОЛНЫЙ")
                            }
                            1.25 -> {
                                getParameters(R.color.empty_five, holder, "125", "ПЕРЕПОЛНЕН")
                            }
                        }
                    }
                }
                if (item.rule != "SUCCESS_FAIL_FILLING") {
                    containerImage.setColorFilter(
                        ContextCompat.getColor(
                            context,
                            R.color.grey_color
                        )
                    )
                }
                mark.background =
                    resources.getDrawable(R.drawable.circle_grin_mark_background_layout, null)
                garbageStatus.background =
                    ContextCompat.getDrawable(context, R.color.grey_color_alpha)
            }
            percentageFillingImage.setColorFilter(
                ContextCompat.getColor(
                    context,
                    R.color.gmm_white
                )
            );
        }

        containerCheck.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                listener!!.setOnClickListener(item, checkListener(holder, item))
            } else {
                if (!isChecked) {
                    listener!!.setOnClickListenerCheck(isChecked, item, checkListener(holder, item))
                }
            }
        }

        this.setOnClickListener {
            if (containerCheck.isChecked) {
                containerCheck.isChecked = false
                listener!!.setOnClickListenerCheck(
                    containerCheck.isChecked,
                    item,
                    checkListener(holder, item)
                )
            } else {
                containerCheck.isChecked = true
                listener!!.setOnClickListener(item, checkListener(holder, item))
            }
        }
    }

    private fun checkListener(
        holder: ViewHolder,
        item: StatusTaskExtended
    ): ArrayList<StatusTaskExtended> {
        if (holder.itemView.containerCheck.isChecked) {
            if (check.firstOrNull { it.id == item.id } == null) {
                check.add(item)
            }
        } else {
            if (check.size != 0) {
                check.remove(item)
            }
        }
        return check
    }

    private fun getParameters(color: Int, holder: ViewHolder, text: String, textDis: String) =
        with(holder.itemView) {
            containerImage.setColorFilter(ContextCompat.getColor(context, color))
            percentageFilling.text = "$text%"
        }

    fun setCheck(check: Boolean = false) {
        isCheck = check
    }

    fun listReceipts(receipts: ArrayList<StatusTaskExtended>) {
        receiptsId = receipts
    }

    fun setTasks(tasks: List<StatusTaskExtended>) {
        check.clear()
        check.addAll(tasks)
    }

    interface GarbageListenerState {
        fun setOnClickListener(item: StatusTaskExtended, list: ArrayList<StatusTaskExtended>)
        fun setOnClickListenerCheck(
            isCheck: Boolean,
            item: StatusTaskExtended,
            list: ArrayList<StatusTaskExtended>
        )
    }
}