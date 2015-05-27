package com.kludgenics.cgmlogger.data.logdata.glucose.data.nightscout

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import java.util.Date

/**
 * Created by matthiasgranberry on 5/24/15.
 */
public class CalibrationEntry() : RealmObject(), NightscoutEntry {
    Expose
    SerializedName("_id")
    private var id: String = ""
    Expose
    private var device: String = ""
    Expose
    private var date: Date = Date()
    Expose
    public var slope: Int = 0
    Expose
    public var intercept: Int = 0
    Expose
    public var scale: Int = 0

    public constructor(device: String, date: Date, slope: Int, intercept: Int, scale: Int) : this() {
        this.device = device
        this.date = date
        this.slope = slope
        this.intercept = intercept
        this.scale = scale
    }

    override fun getId(): String {
        return id
    }

    public fun setId(id: String) {
        this.id = id
    }

    override fun getDevice(): String {
        return device
    }

    public fun setDevice(device: String) {
        this.device = device
    }

    override fun getDate(): Date {
        return date
    }

    public fun setDate(date: Date) {
        this.date = date
    }
}