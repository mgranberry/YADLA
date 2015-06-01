package com.kludgenics.cgmlogger.model.nightscout

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.kludgenics.cgmlogger.model.glucose.BloodGlucose
import java.util.Date

/**
 * Created by matthiasgranberry on 5/21/15.
 */
public class SgvEntry() : BloodGlucose, NightscoutEntry {
    Expose
    SerializedName("_id")
    private var id: String = ""
    Expose
    private var device: String = ""
    Expose
    private var date: Date = Date()
    Expose
    public var sgv: Int = 0
    Expose
    public var direction: String = ""
    Expose
    public var filtered: Int = 0
    Expose
    public var unfiltered: Int = 0
    Expose
    public var rssi: Int = 0
    Expose
    public var noise: Int = 0

    public constructor(device: String, date: Date, sgv: Int, direction: String, filtered: Int, unfiltered: Int, rssi: Int, noise: Int) : this() {
        this.device = device
        this.date = date
        this.sgv = sgv
        this.direction = direction
        this.filtered = filtered
        this.unfiltered = unfiltered
        this.rssi = rssi
        this.noise = noise
    }

    override fun getId(): String {
        return id
    }

    override fun getUnit(): String {
        return BloodGlucose.UNIT_MGDL
    }

    override fun getValue(): Double {
        return sgv.toDouble()
    }

    override fun getType(): String {
        return BloodGlucose.TYPE_CGM
    }

    override fun getDevice(): String {
        return device
    }

    override fun getDate(): Date {
        return date
    }


}