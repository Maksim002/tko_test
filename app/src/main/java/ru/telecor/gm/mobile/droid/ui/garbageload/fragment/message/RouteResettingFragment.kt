package ru.telecor.gm.mobile.droid.ui.garbageload.fragment.message

import android.os.Build
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.fragment_route_resetting.*
import ru.telecor.gm.mobile.droid.R
import ru.telecor.gm.mobile.droid.ui.base.BaseBottomSheetFragment

class RouteResettingFragment(var listener: ListenerCompleteRoute) : BaseBottomSheetFragment() {

    override val layoutRes = R.layout.fragment_route_resetting

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listener.setOpen()
        initClick()
    }

    private fun initClick() {
        btnItemCancelRouteRes.setOnClickListener {
            listener.setOnClickRouteClear()
            dismiss()
        }

        btnItemRouteRes.setOnClickListener {
            listener.setOnClickRoute()
            dismiss()
        }
    }

   override fun getTheme(): Int {
    return if(Build.VERSION.SDK_INT > 22){
        R.style.AppBottomSheetDialogTheme
    }else{
        0
    }
}

    interface ListenerCompleteRoute{
        fun setOnClickRoute()
        fun setOnClickRouteClear()
        fun setOpen()
    }
}