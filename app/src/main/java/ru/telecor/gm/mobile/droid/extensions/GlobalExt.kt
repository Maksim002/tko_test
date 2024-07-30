package ru.telecor.gm.mobile.droid.extensions

import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Project Truck Crew
 * Package ru.telecor.gm.mobile.droid.extensions
 *
 * Global Kotlin extensions for anything Android-project.
 *
 * Created by Artem Skopincev (aka sharpyx) 13.07.2020
 * Copyright © 2020 TKOInform. All rights reserved.
 */

/**
 * Showing toast in current context
 *
 * @param message Message to show by toast
 * @param duration Short by default
 * @param otherContext If need use other context
 */
fun Context.showToast(
    message: String, duration: Int = Toast.LENGTH_SHORT,
    otherContext: Context? = null
) {
    Toast.makeText(otherContext ?: this, message, duration).show()
}

/**
 * Returns int color from resource.
 * @param resId Resource ID
 */
fun Context.color(resId: Int) = ContextCompat.getColor(this, resId)

/**
 * Choose view Visible state. By default invisible (false) is GONE.
 * @param visible true or false
 * @param invisibleState by default - GONE, may be INVISIBLE
 */
fun View.visible(visible: Boolean, invisibleState: Int = View.GONE) {
    visibility = if (visible) View.VISIBLE else invisibleState
}

/**
 * Return Date from Long (this).
 */
fun Long.toDate(): Date {
    return Date(this * 1000L)
}

inline fun <T> tryOrNull(block: () -> T?): T? {
    return try {
        block()
    } catch (e: Throwable) {
        null
    }
}

fun TextInputEditText.inputFilterNumberRange(range: IntRange){
    filterMin(range.first)
    filters = arrayOf<InputFilter>(InputFilterMax(range.last))
}

class InputFilterMax(private var max: Int) : InputFilter {
    override fun filter(
        p0: CharSequence, p1: Int, p2: Int, p3: Spanned?, p4: Int, p5: Int
    ): CharSequence? {
        try {
            val replacement = p0.subSequence(p1, p2).toString()
            var newVal = p3.toString().substring(0, p4) + replacement + p3.toString()
                .substring(p5, p3.toString().length)
            if (newVal.contains(' ')) {
                newVal = newVal.filterNot { it == ' ' }
            }
            val input = newVal.toInt()
            if (input <= max) return null
        } catch (e: NumberFormatException) { }
        return ""
    }
}

fun TextInputEditText.filterMin(min: Int){
    onFocusChangeListener = View.OnFocusChangeListener { view, b ->
        if (!b) {
            // set minimum number if inputted number less than minimum
            setTextMin(min)
            // hide soft keyboard on edit text lost focus
            context.hideSoftKeyboard(this)
        }
    }

    setOnEditorActionListener { v, actionId, event ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            // set minimum number if inputted number less than minimum
            setTextMin(min)

            // hide soft keyboard on press action done
            context.hideSoftKeyboard(this)
        }
        false
    }
}

fun TextInputEditText.setTextMin(min: Int){
    try {
        val value = text.toString().toInt()
        if (value < min){
            setText("$min")
        }
    }catch (e: Exception){
        setText("$min")
    }
}

fun Context.hideSoftKeyboard(editText: EditText){
    (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).apply {
        hideSoftInputFromWindow(editText.windowToken, 0)
    }
}