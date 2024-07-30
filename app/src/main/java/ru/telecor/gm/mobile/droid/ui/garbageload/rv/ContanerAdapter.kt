package ru.telecor.gm.mobile.droid.ui.garbageload.rv

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.telecor.gm.mobile.droid.ui.base.rv.GenericRecyclerAdapter
import ru.telecor.gm.mobile.droid.ui.base.rv.ViewHolder
import ru.telecor.gm.mobile.droid.R
import ru.telecor.gm.mobile.droid.entities.db.ProcessingPhoto

class ContanerAdapter(
    var item: ArrayList<ProcessingPhoto> = arrayListOf(),
    private val deleteOption: (ProcessingPhoto) -> Unit
) : GenericRecyclerAdapter<ProcessingPhoto>(item) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return super.onCreateViewHolder(parent, R.layout.item_contaner)
    }

    override fun bind(item: ProcessingPhoto, holder: ViewHolder): Unit = with(holder.itemView) {

    }
}