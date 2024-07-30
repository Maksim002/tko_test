package ru.telecor.gm.mobile.droid.ui.task.fragment

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.fragment_getting_started.*
import ru.telecor.gm.mobile.droid.R
import ru.telecor.gm.mobile.droid.ui.base.BaseBottomSheetFragment

class GettingStartedFragment(var listener: Listener, var standName: String) :
    BaseBottomSheetFragment() {

    override val layoutRes = R.layout.fragment_getting_started

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addressText.text = standName

        btnToRoute.setOnClickListener {
            listener.setOnClickRouteListener()
            dismiss()
        }

        btnToBeginning.setOnClickListener {
            listener.setOnClickBeginningListener()
            dismiss()
        }
    }


    //переопределил метод чтобы изменить фон
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        if (dialog.window != null) {
            dialog.window!!.setBackgroundDrawableResource(R.color.color_background_dialog)
        }
        return dialog

    }

    override fun getTheme(): Int {
        return if (Build.VERSION.SDK_INT > 22) {
            R.style.AppBottomSheetDialogTheme
        } else {
            0
        }
    }

    interface Listener {
        fun setOnClickBeginningListener()
        fun setOnClickRouteListener()
    }
}