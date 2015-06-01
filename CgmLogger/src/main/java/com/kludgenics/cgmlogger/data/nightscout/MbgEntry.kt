package com.kludgenics.cgmlogger.data.nightscout

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.kludgenics.cgmlogger.data.glucose.BloodGlucose
import java.util.Date

/**
 * Created by matthiasgranberry on 5/24/15.
 */
public class MbgEntry() : BloodGlucose, NightscoutEntry {

    Expose
    SerializedName("_id")
    private var id: String = ""
    Expose
    private var device: String = ""
    Expose
    private var mbg: Int = 0
    Expose
    private var date: Date = Date()

    public constructor(device: String, date: Date, mbg: Int) : this() {
        this.device = device
        this.mbg = mbg
        this.date = date
    }

    override fun getValue(): Double {
        return mbg.toDouble()
    }

    override fun getId(): String {
        return id
    }

    override fun getDevice(): String {
        return device
    }

    override fun getDate(): Date {
        return date
    }

    override fun getType(): String {
        return BloodGlucose.TYPE_SMBG
    }

    override fun getUnit(): String {
        return BloodGlucose.UNIT_MGDL
    }
}