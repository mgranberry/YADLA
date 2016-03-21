package com.kludgenics.cgmlogger.app.viewmodel

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Created by matthias on 3/16/16.
 */

object DataBindingDelegates {
    fun <T: Any?> observable(fieldId: Int): ReadWriteProperty<DataBindingObservable?, T> = ObservableVar(fieldId)
    fun <T: Any?> observable(fieldId: Int, getter: ()->T, setter: (T)->Unit):
            ReadWriteProperty<DataBindingObservable?, T>  = ObservableThing(fieldId, getter, setter)
}

private class ObservableVar<in R: DataBindingObservable, T>(val fieldId: Int) : ReadWriteProperty<R?, T> {
    private var value: T? = null

    override fun getValue(thisRef: R?, property: KProperty<*>): T {
        return value ?: throw IllegalStateException("Property ${property.name} should be initialized before get.")
    }

    override fun setValue(thisRef: R?, property: KProperty<*>, value: T) {
        thisRef?.notifyPropertyChanged(fieldId)
        this.value = value
    }
}

private class ObservableThing<in R: DataBindingObservable, T>(val fieldId: Int,
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