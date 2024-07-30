package ru.telecor.gm.mobile.droid.ui.tasktrouble.problem.rv

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.telecor.gm.mobile.droid.ui.base.rv.GenericRecyclerAdapter
import ru.telecor.gm.mobile.droid.ui.base.rv.ViewHolder
import kotlinx.android.synthetic.main.item_trouble_photo.view.itemProblemPhoto
import kotlinx.android.synthetic.main.item_trouble_photo.view.itemTroublePhotoRemove
import ru.telecor.gm.mobile.droid.R
import ru.telecor.gm.mobile.droid.entities.db.ProcessingPhoto

open class ProblemPhotoAdapter(
    var item: ArrayList<ProcessingPhoto> = arrayListOf(),
    private val deleteOption: (ProcessingPhoto) -> Unit,
    private val photo: (ProcessingPhoto) -> Unit
) : GenericRecyclerAdapter<ProcessingPhoto>(item) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return super.onCreateViewHolder(parent,R.layout.item_trouble_photo)
    }

    override fun bind(item: ProcessingPhoto, holder: ViewHolder): Unit = with(holder.itemView) {
        Glide.with(holder.itemView.context)
            .load(item.photoPath)
            .into(itemProblemPhoto)

        itemTroublePhotoRemove.setOnClickListener {
            deleteOption(item)
        }

        this.setOnClickListener {
            photo(item)
        }
    }
}