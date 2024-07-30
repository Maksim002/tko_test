package ru.telecor.gm.mobile.droid.ui.dumping.fragment.authorization

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import kotlinx.android.synthetic.main.fragment_authorization_bottom_sheet.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import ru.telecor.gm.mobile.droid.R
import ru.telecor.gm.mobile.droid.di.Scopes
import ru.telecor.gm.mobile.droid.extensions.inputFilterNumberRange
import ru.telecor.gm.mobile.droid.ui.base.BaseBottomSheetFragment
import toothpick.Toothpick
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class AuthorizationBottomSheetFragment() : BaseBottomSheetFragment(), AuthorizationView {

    private var decimalSeparator: Char = ' '
    private var thousandSeparator: Char = ' '

    override val layoutRes = R.layout.fragment_authorization_bottom_sheet

    @InjectPresenter
    lateinit var presenter: AuthorizationPresenter

    @ProvidePresenter
    fun providePresenter(): AuthorizationPresenter {
        return Toothpick.openScope(Scopes.SERVER_SCOPE)
            .getInstance(AuthorizationPresenter::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setEditTextFilter()
        initClick()
    }

    private fun setEditTextFilter() {
        etPersonnelNumber.inputFilterNumberRange(0..100000)
        etPersonnelNumber.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

                etPersonnelNumber.removeTextChangedListener(this)

                try {
                    var givenstring: String = p0.toString()
                    if (givenstring.contains(thousandSeparator)) {
                        givenstring = givenstring.replace(thousandSeparator.toString(), "")
                    }

                    val doubleVal: Double = givenstring.toDouble()
                    val unusualSymbols = DecimalFormatSymbols()
                    unusualSymbols.decimalSeparator = decimalSeparator
                    unusualSymbols.groupingSeparator = thousandSeparator

                    val formatter = DecimalFormat("#,##0.##", unusualSymbols)
                    formatter.groupingSize = 3
                    val formattedString = formatter.format(doubleVal)

                    etPersonnelNumber.setText(formattedString)
                    etPersonnelNumber.setSelection(etPersonnelNumber.text!!.length)
                } catch (nfe: NumberFormatException) {
                    nfe.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                etPersonnelNumber.addTextChangedListener(this)

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

        })
    }

    override fun getTheme(): Int {
        return if (Build.VERSION.SDK_INT > 22) {
            R.style.AppBottomSheetDialogTheme
        } else {
            0
        }
    }

    private fun initClick() {
        weightCancelBtn.setOnClickListener {
            dismiss()
        }

        weightSaveBtn.setOnClickListener {
            presenter.onPolygonWeightEntered(etPersonnelNumber.text.toString())
        }
    }

    override fun clearWindow() {
        dismiss()
    }

    override fun showMessage(msg: String) {

    }
}