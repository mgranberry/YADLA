package com.kludgenics.cgmlogger.app.viewmodel

import android.databinding.Bindable
import android.databinding.PropertyChangeRegistry
import com.kludgenics.cgmlogger.app.BR
import com.kludgenics.cgmlogger.app.R
import io.realm.RealmChangeListener
import io.realm.RealmObject
import io.realm.annotations.Index
import java.util.*

/**
 * Created by matthias on 3/17/16.
 */

interface Status {
    companion object {
        val SUCCESS = "Sync attempt successful."
        val FAILURE = "Sync attempt has failed."
        val EOF = "Sync stopped because the device was disconnected."
        val IN_PROGRESS = "Sync in progress."

        val CODE_SUCCESS = 0
        val CODE_FAILURE = 1
        val CODE_DEVICE_READ_IN_PROGRESS = 2
        val CODE_UNKNOWN = 3
        val CODE_EOF = 4
    }
    var modificationTime: Date
    var syncStartTime: Date
    var latestRecordTime: Date?
    var statusText: String?
    var statusCode: Int
    var serialNumber: String?
    var active: Boolean
    var icon: Int
    var syncId: Int
    var nextSync: Date?
    var clockOffsetMillis: Long?
}

open class RealmStatus(override var modificationTime: Date = Date(),
                       override var latestRecordTime: Date? = null,
                       override var statusText: String? = null,
                       override var statusCode: Int = Status.CODE_UNKNOWN,
                       @Index override var serialNumber: String? = null,
                       override var active: Boolean = false,
                       override var icon: Int = R.drawable.bluetooth_circle,
                       override var syncStartTime: Date = Date(),
                       override var syncId: Int = 0,
                       override var nextSync: Date? = null,
                       override var clockOffsetMillis: Long? = null) : Status, RealmObject() {
}

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
    override var modificationTime: Date by DataBindingDelegates.observable(BR.modificationTime, {s.modificationTime}, {s.modificationTime=it})

    @get:Bindable
    override var latestRecordTime: Date? by DataBindingDelegates.observable(BR.latestRecordTime, {s.latestRecordTime}, {s.latestRecordTime=it})

    @get:Bindable
    override var nextSync: Date? by DataBindingDelegates.observable(BR.nextSync, {s.nextSync}, {s.nextSync=it})

    @get:Bindable
    override var statusText: String? by DataBindingDelegates.observable(BR.statusText, {s.statusText}, {s.statusText=it})

    @get:Bindable
    override var serialNumber: String? by DataBindingDelegates.observable(BR.serialNumber, {s.serialNumber}, {s.serialNumber=it})

    @get:Bindable
    override var active: Boolean by DataBindingDelegates.observable(BR.active, {s.active}, {s.active=it})

    @get:Bindable
    override var icon: Int by DataBindingDelegates.observable(BR.icon, {s.icon}, {s.icon=it})

    @get:Bindable
    override var statusCode: Int by DataBindingDelegates.observable(BR.statusCode, {s.statusCode}, {s.statusCode=it})

    @get:Bindable
    override var syncStartTime: Date by DataBindingDelegates.observable(BR.syncStartTime, {s.syncStartTime}, {s.syncStartTime=it})

    @get:Bindable
    override var syncId: Int by DataBindingDelegates.observable(BR.syncId, {s.syncId}, {s.syncId=it})

    @get:Bindable
    override var clockOffsetMillis: Long? by DataBindingDelegates.observable(BR.clockOffsetMillis, {s.clockOffsetMillis}, {s.clockOffsetMillis=it})

}
