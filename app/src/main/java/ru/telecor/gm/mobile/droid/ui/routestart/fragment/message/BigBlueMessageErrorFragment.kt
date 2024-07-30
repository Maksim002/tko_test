package ru.telecor.gm.mobile.droid.ui.routestart.fragment.message

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import kotlinx.android.synthetic.main.fragment_message_error.*
import ru.telecor.gm.mobile.droid.R
import ru.telecor.gm.mobile.droid.ui.base.BaseBottomSheetFragment

class BigBlueMessageErrorFragment(var textMessage: String? = "") : BaseBottomSheetFragment() {

    override val layoutRes = R.layout.fragment_big_blue_message_error

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (textMessage != ""){
            messageText.text = textMessage
        }

        Handler().postDelayed({ // Do something after 5s = 500ms
            dismiss()
        }, 3000)
    }

   override fun getTheme(): Int {
       return if (Build.VERSION.SDK_INT > 22) {
           R.style.AppBottomSheetDialogTheme
       } else {
           0
       }
   }
}