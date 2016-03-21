package com.kludgenics.cgmlogger.app.viewmodel

import android.databinding.Bindable
import android.databinding.BaseObservable
import android.databinding.PropertyChangeRegistry

import com.kludgenics.cgmlogger.app.BR
import io.realm.RealmChangeListener
import io.realm.RealmObject

import java.util.Date

/**
 * Created by matthias on 3/17/16.
 */

interface Status {
    companion object {
        val SUCCESS = "Success"
        val FAILURE = "Failure"
    }
    var modificationTime: Date?
    var statusText: String?
    var serialNumber: String?
}

open class RealmStatus(override var modificationTime: Date? = Date(),
                  override var statusText: String? = null,
                  override var serialNumber: String? = null) : Status, RealmObject()

class ObservableStatus(private val s: RealmStatus) : DataBindingObservable, Status, RealmChangeListener {
    init {
        if (s.isValid)
            s.addChangeListener(this)
    }

    override fun onChange() {
        notifyChange()
    }

    override var mCallbacks: PropertyChangeRegistry? = null
    @get:Bindable
    override var modificationTime: Date? by DataBindingDelegates.observable(BR.modificationTime, {s.modificationTime}, {s.modificationTime=it})
    @get:Bindable
    override var statusText: String? by DataBindingDelegates.observable(BR.statusText, {s.statusText}, {s.statusText=it})
    @get:Bindable
    override var serialNumber: String? by DataBindingDelegates.observable(BR.serialNumber, {s.serialNumber}, {s.serialNumber=it})
}
