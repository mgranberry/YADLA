package com.kludgenics.cgmlogger.app.viewmodel

import android.databinding.BaseObservable
import android.databinding.BindingAdapter
import android.text.Editable
import android.text.TextWatcher
import com.kludgenics.cgmlogger.app.R

class ObservableString(initialValue: String?) : BaseObservable(), CharSequence {
    internal var value: String? = initialValue

    fun get(): String? {
        return value
    }

    fun set(value: String?) {
        if (!(if (this.value == null) value == null else this.value == value)) {
            this.value = value
            notifyChange()
        }
    }

    override val length: Int get() {
        return if (value != null) value!!.length else 0
    }

    override fun get(index: Int): Char {
        return value!![index]
    }

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        return value!!.subSequence(startIndex, endIndex)
    }

    val isEmpty: Boolean
        get() = value == null || value!!.isEmpty()

}

object ObservableStringBinding {
    @JvmStatic
    @BindingAdapter("app:textBinding")
    fun bindEditTextToObservableString(editText: android.support.design.widget.TextInputEditText,
                                       observableString: ObservableString) {
        val pair = editText.getTag(R.id.id_text_watcher) as? Pair<Any, Any>
        if (pair == null || pair.first !== observableString) {
            val updatedValue = observableString.get()
            if (editText.text.toString() != updatedValue)
                editText.setText(updatedValue)
            if (pair != null) {
                editText.removeTextChangedListener(pair.second as TextWatcher)
            }
            val watcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    observableString.set(s.toString())
                }

                override fun afterTextChanged(s: Editable) {

                }
            }
            editText.setTag(R.id.id_text_watcher, Pair<ObservableString, TextWatcher>(observableString, watcher))
            editText.addTextChangedListener(watcher)
        }
    }
}