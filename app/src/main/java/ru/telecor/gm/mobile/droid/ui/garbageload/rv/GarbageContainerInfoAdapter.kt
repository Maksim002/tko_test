package ru.telecor.gm.mobile.droid.ui.garbageload.rv

import android.util.Log
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_garbage_container_info.view.*
import ru.telecor.gm.mobile.droid.R
import ru.telecor.gm.mobile.droid.entities.task.StatusTaskExtended
import ru.telecor.gm.mobile.droid.ui.base.rv.GenericRecyclerAdapter
import ru.telecor.gm.mobile.droid.ui.base.rv.ViewHolder


class GarbageContainerInfoAdapter(
    var listener: GarbageInfoListenerState? = null,
    var itemRoute: ArrayList<StatusTaskExtended> = arrayListOf()
) : GenericRecyclerAdapter<StatusTaskExtended>(itemRoute) {

    private var isCheck = false
    private var check: ArrayList<StatusTaskExtended> = arrayListOf()

    private var listInfo = mutableListOf<StatusTaskExtended>()

    var isFirst = false
    var isLast = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return super.onCreateViewHolder(parent, R.layout.item_garbage_container_info)
    }

    override fun bind(item: StatusTaskExtended, holder: ViewHolder) = with(holder.itemView) {
        photoContainer.setOnClickListener { listener?.setOnPhotoClickListener(item) }
        if (item.containerStatus != null) {
            //photo
            photoContainer.isVisible = false
            item.containerStatus.let { contStatus ->
                val photos = item.privatePhotos ?: listOf()
                if(photos.isNotEmpty()){
                    photoContainer.isVisible = true
                    val photosSize = photos.size
                    amountOfPhotos.text = if (photosSize > 0) "+$photosSize" else ""
                    val firstPhoto = photos.first()
                    Glide.with(holder.itemView.context)
                        .load(firstPhoto.photoPath)
                        .into(photoContainerItem)
                }
            }
            if (item.containerStatus.containerFailureReason != null) {
                statusDescription.isVisible = true
                statusDescription.text = item.containerStatus.containerFailureReason.name
                statusDescription.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.dialog_negative_button
                    )
                )
                weightText.isVisible = false
                val groupList = item.containerStatus?.allGroupContainersId
                if (groupList != null) {
                    val lastIndex = groupList?.lastIndex
//                    Log.d("chyngyzLog", "lastIndex: $lastIndex ,,, $groupList")

                    if (groupList.contains(item.id.toLong())) {
                        if (groupList.size > 1) {
                            groupList.forEachIndexed { index, itemInGroup ->
                                when (index) {
                                    0 -> {
                                        if (item.id == itemInGroup.toInt()) {
                                            val paramTopMargin =
                                                markInfo.layoutParams as ViewGroup.MarginLayoutParams
                                            paramTopMargin.setMargins(0, 8, 0, 0)
                                            markInfo.layoutParams = paramTopMargin

                                            markInfo.background = resources.getDrawable(
                                                R.drawable.circle_top_red_mark_info_layout,
                                                null
                                            )

                                            weightText.isVisible = true
                                            statusDescription.isVisible = true
                                            photoContainer.isVisible = false
                                            item.containerStatus.let { contStatus ->
                                                val photos = item.privatePhotos ?: listOf()
                                                if(photos.isNotEmpty()){
                                                    photoContainer.isVisible = true
                                                    val photosSize = photos.size
                                                    amountOfPhotos.text = if (photosSize > 0) "+$photosSize" else ""
                                                    val firstPhoto = photos.first()
                                                    Glide.with(holder.itemView.context)
                                                        .load(firstPhoto.photoPath)
                                                        .into(photoContainerItem)
                                                }
                                            }
                                        }

                                    }
                                    lastIndex -> {
                                        if (lastIndex > 0) {
                                            if (item.id == itemInGroup.toInt()) {
                                                val paramBotMargin =
                                                    markInfo.layoutParams as ViewGroup.MarginLayoutParams
                                                paramBotMargin.setMargins(0, 0, 0, 8)
                                                markInfo.layoutParams = paramBotMargin

                                                markInfo.background = resources.getDrawable(
                                                    R.drawable.circle_bottom_red_mark_info_layout,
                                                    null
                                                )
                                                weightText.isVisible = false
                                                statusDescription.isVisible = false
                                                photoContainer.isVisible = false
                                            }

                                        }

                                    }
                                    else -> {
//                                        Log.d("chyngyzLog", "index: mid $index")

                                        if (item.id == itemInGroup.toInt()) {
                                            markInfo.background =
                                                resources.getDrawable(
                                                    R.color.dialog_negative_button,
                                                    null
                                                )
                                            weightText.isVisible = false
                                            statusDescription.isVisible = false
                                            photoContainer.isVisible = false
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    photoContainer.isVisible = false
                    item.containerStatus.let { contStatus ->
                        val photos = item.privatePhotos ?: listOf()
                        if(photos.isNotEmpty()){
                            photoContainer.isVisible = true
                            val photosSize = photos.size
                            amountOfPhotos.text = if (photosSize > 0) "+$photosSize" else ""
                            val firstPhoto = photos.first()
                            Glide.with(holder.itemView.context)
                                .load(firstPhoto.photoPath)
                                .into(photoContainerItem)
                        }
                    }
                    weightText.isVisible = true
                    statusDescription.isVisible = true
                    markInfo.isVisible = false
                }

            } else {
                when (item.rule) {
                    "SUCCESS_FAIL" -> {
                        statusDescription.isVisible = false
                        weightText.isVisible = false
                    }
                    "SUCCESS_FAIL_MANUAL_VOLUME" -> {
                        weightText.isVisible = true
                        item.containerStatus.volumeAct?.let { weightText.text = "${it.toDouble()} м3" }
                        statusDescription.isVisible = false
                    }
                    "SUCCESS_FAIL_MANUAL_COUNT_WEIGHT" -> {
                        weightText.isVisible = true
                        item.containerStatus.weight?.let { weightText.text = "${it.toDouble()} КГ" }
                    }
                    "SUCCESS_FAIL_FILLING" -> {
                        statusDescription.isVisible = true
                        weightText.isVisible = false

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
                val groupList = item.containerStatus?.allGroupContainersId
                if (groupList != null) {
                    val lastIndex = groupList?.lastIndex
//                    Log.d("chyngyzLog", "lastIndex: $lastIndex ,,, $groupList")

                    if (groupList.contains(item.id.toLong())) {
                        if (groupList.size > 1) {
                            groupList?.forEachIndexed { index, itemInGroup ->
                                when (index) {
                                    0 -> {
                                        if (item.id == itemInGroup.toInt()) {
//                                            Log.d("chyngyzLog", "index: first $index")
                                            val paramTopMargin =
                                                markInfo.layoutParams as ViewGroup.MarginLayoutParams
                                            paramTopMargin.setMargins(0, 8, 0, 0)
                                            markInfo.layoutParams = paramTopMargin

                                            markInfo.background = resources.getDrawable(
                                                R.drawable.circle_top_green_mark_info_layout,
                                                null
                                            )
                                            weightText.isVisible = true
                                            statusDescription.isVisible = true
                                            photoContainer.isVisible = false
                                            item.containerStatus.let { contStatus ->
                                                val photos = item.privatePhotos ?: listOf()
                                                if(photos.isNotEmpty()){
                                                    photoContainer.isVisible = true
                                                    val photosSize = photos.size
                                                    amountOfPhotos.text = if (photosSize > 0) "+$photosSize" else ""
                                                    val firstPhoto = photos.first()
                                                    Glide.with(holder.itemView.context)
                                                        .load(firstPhoto.photoPath)
                                                        .into(photoContainerItem)
                                                }
                                            }
                                        }

                                    }
                                    lastIndex -> {
                                        if (lastIndex > 0) {
                                            if (item.id == itemInGroup.toInt()) {
//                                                Log.d("chyngyzLog", "index: last $index")
                                                val paramBotMargin =
                                                    markInfo.layoutParams as ViewGroup.MarginLayoutParams
                                                paramBotMargin.setMargins(0, 0, 0, 8)
                                                markInfo.layoutParams = paramBotMargin

                                                markInfo.background = resources.getDrawable(
                                                    R.drawable.circle_bottom_green_mark_info_layout,
                                                    null
                                                )
                                                weightText.isVisible = false
                                                statusDescription.isVisible = false
                                                photoContainer.isVisible = false
                                            }
                                        }
                                    }
                                    else -> {
//                                        Log.d("chyngyzLog", "index: mid $index")

                                        if (item.id == itemInGroup.toInt()) {
                                            markInfo.background =
                                                resources.getDrawable(R.color.grey_color, null)
                                            weightText.isVisible = false
                                            statusDescription.isVisible = false
                                            photoContainer.isVisible = false
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    photoContainer.isVisible = false
                    item.containerStatus.let { contStatus ->
                        val photos = item.privatePhotos ?: listOf()
                        if(photos.isNotEmpty()){
                            photoContainer.isVisible = true
                            val photosSize = photos.size
                            amountOfPhotos.text = if (photosSize > 0) "+$photosSize" else ""
                            val firstPhoto = photos.first()
                            Glide.with(holder.itemView.context)
                                .load(firstPhoto.photoPath)
                                .into(photoContainerItem)
                        }
                    }
                    weightText.isVisible = true
                    statusDescription.isVisible = true
                    markInfo.isVisible = false
                }
            }
        } else {
            markInfo.isVisible = false
            photoContainer.isVisible = false
            weightText.isVisible = false
            markInfo.isVisible = false
            photoContainer.isVisible = false
        }
//

        val empty =""
    }

    private fun getParameters(color: Int, holder: ViewHolder, text: String, textDis: String) =
        with(holder.itemView) {
            statusDescription.setTextColor(ContextCompat.getColor(context, color))
            statusDescription.text = textDis
        }


    interface GarbageInfoListenerState {
        fun setOnPhotoClickListener(item: StatusTaskExtended)
    }

    fun setTasks(tasks: List<StatusTaskExtended>) {
        check.clear()
        check.addAll(tasks)
    }
}