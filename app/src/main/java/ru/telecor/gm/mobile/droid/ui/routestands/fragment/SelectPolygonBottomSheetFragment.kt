package ru.telecor.gm.mobile.droid.ui.routestands.fragment

import android.os.Build
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.fragment_select_polygon_bottom_sheet.*
import ru.telecor.gm.mobile.droid.R
import ru.telecor.gm.mobile.droid.entities.VisitPoint
import ru.telecor.gm.mobile.droid.ui.base.BaseBottomSheetFragment
import ru.telecor.gm.mobile.droid.ui.routestands.fragment.rv.ListPolygonsAdapter

class SelectPolygonBottomSheetFragment(
    var list: List<VisitPoint>,
    var isNavigate: Boolean,
    var listener: ListPolygonsAdapter.Listener,
    var onNavigateListener: NavigateToListener?,
) : BaseBottomSheetFragment() {

    override val layoutRes = R.layout.fragment_select_polygon_bottom_sheet

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initClick()
        val adapters = ListPolygonsAdapter(object : ListPolygonsAdapter.Listener {
            override fun setOnClickPolygons(item: VisitPoint) {
                listener.setOnClickPolygons(item)
                dismiss()
            }
        })
        adapters.update(list)
        selectRecyclerView.adapter = adapters
    }

    private fun initClick() {

        selectPolygonBtn.setOnClickListener {
            if (isNavigate) {
                onNavigateListener?.onNavigate()
                dismiss()
            } else {
                dismiss()
            }
        }



    }

    override fun getTheme(): Int {
        return if (Build.VERSION.SDK_INT > 22) {
            R.style.AppBottomSheetDialogTheme
        } else {
            0
        }
    }

    interface NavigateToListener {
        fun onNavigate()
    }
}