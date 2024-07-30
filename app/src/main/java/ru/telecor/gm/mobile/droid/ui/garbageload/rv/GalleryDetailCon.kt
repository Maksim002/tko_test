package ru.telecor.gm.mobile.droid.ui.garbageload.rv

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_gallery_detail_con.view.*
import ru.telecor.gm.mobile.droid.R
import ru.telecor.gm.mobile.droid.entities.db.ProcessingPhoto
import ru.telecor.gm.mobile.droid.entities.photo.GarbagePhotoModel
import ru.telecor.gm.mobile.droid.entities.task.StatusTaskExtended
import ru.telecor.gm.mobile.droid.ui.base.rv.GenericRecyclerAdapter
import ru.telecor.gm.mobile.droid.ui.base.rv.ViewHolder

class GalleryDetailCon(
    private var itemTask: StatusTaskExtended? = null,
    var item: ArrayList<ProcessingPhoto> = arrayListOf(),
    private val deleteOption: (ProcessingPhoto) -> Unit,
) : GenericRecyclerAdapter<ProcessingPhoto>(item) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return super.onCreateViewHolder(parent, R.layout.item_gallery_detail_con)
    }

    override fun bind(item: ProcessingPhoto, holder: ViewHolder): Unit = with(holder.itemView) {
        Glide.with(holder.itemView.context)
            .load(item.photoPath)
            .into(itemProblemPhotoCon)

        itemTroublePhotoRemoveCon.setOnClickListener {
            deleteOption(item)
        }
    }
}