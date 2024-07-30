package ru.telecor.gm.mobile.droid.ui.garbageload.rv.diffutil

import androidx.recyclerview.widget.DiffUtil
import ru.telecor.gm.mobile.droid.entities.task.StatusTaskExtended

/**
 * Project Truck Crew
 * Package ru.telecor.gm.mobile.droid.ui.garbageload.rv.diffutil
 *
 *
 *
 * Created by Emil Zamaldinov (aka Piligrim) 07.05.2021
 * Copyright © 2021 TKO-Inform. All rights reserved.
 */
class VolumeContainerDiffUtilCallback(
    val old: List<StatusTaskExtended>,
    val new: List<StatusTaskExtended>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = old.size

    override fun getNewListSize(): Int = new.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return old[oldItemPosition].id == new[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return old[oldItemPosition] == new[newItemPosition]
    }
}