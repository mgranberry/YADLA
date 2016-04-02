package com.kludgenics.cgmlogger.app.viewmodel

import android.databinding.BaseObservable
import android.databinding.Observable
import kotlin.properties.Delegates
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Created by matthias on 3/16/16.
 */

object DataBindingDelegates {
    fun <T: Any?> observable(fieldId: Int, initialValue: T?):
            ReadWriteProperty<DataBindingObservable, T?> = ObservableVar(fieldId, initialValue)
    fun <T: Any?> observable(fieldId: Int, getter: ()->T, setter: (T)->Unit):
            ReadWriteProperty<DataBindingObservable, T> = ObservableThing(fieldId, getter, setter)
}

private class ObservableVar<in R: DataBindingObservable, T>(val fieldId: Int, initialValue: T?) : ReadWriteProperty<R, T?> {
    private var value: T? = initialValue

    override fun getValue(thisRef: R, property: KProperty<*>): T? {
        return value
    }

    override fun setValue(thisRef: R, property: KProperty<*>, value: T?) {
        thisRef.notifyPropertyChanged(fieldId)
        this.value = value
    }
}

class ObservableThing<in R: DataBindingObservable, T>(val fieldId: Int,
                                                      private val getter: ()->T,
                                                      private val setter: (T)->Unit) : ReadWriteProperty<R?, T> {
    override fun getValue(thisRef: R?, property: KProperty<*>): T {
        return getter()
    }

    override fun setValue(thisRef: R?, property: KProperty<*>, value: T) {
        setter(value)
        System.out.println("Notifying $thisRef of ${property.name} change to $value ($fieldId)")
        thisRef?.notifyPropertyChanged(fieldId)
    }
}