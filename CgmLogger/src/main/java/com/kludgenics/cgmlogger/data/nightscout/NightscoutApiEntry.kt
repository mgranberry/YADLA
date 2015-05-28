package com.kludgenics.cgmlogger.data.nightscout

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.Date

/**
 * Created by matthiasgranberry on 5/24/15.
 */
public class NightscoutApiEntry : NightscoutEntry {
    Expose
    SerializedName("_id")
    private var id: String = ""
    Expose
    private val slope: Int = 0
    Expose
    private val intercept: Int = 0
    Expose
    private val scale: Int = 0
    Expose
    private val mbg: Int = 0
    Expose
    private val device: String = ""
    Expose
    private val date: Long = 0
    Expose
    private val sgv: Int = 0
    Expose
    private val direction: String = ""
    Expose
    private val type: String = ""
    Expose
    private val filtered: Int = 0
    Expose
    private val unfiltered: Int = 0
    Expose
    private val rssi: Int = 0
    Expose
    private val noise: Int = 0

    public fun getType(): Type {
        if (type == "mbg")
            return Type.MBG
        if (type == "sgv")
            return Type.SGV
        if (type == "cal")
            return Type.CAL
        return Type.UNKNOWN
    }

    public fun asCalibration(): CalibrationEntry {
        return CalibrationEntry(device, Date(date), slope, intercept, scale)
    }

    public fun asMbg(): MbgEntry {
        return MbgEntry(device, Date(date), mbg)
    }

    public fun asSgv(): SgvEntry {
        return SgvEntry(device, Date(date), sgv, direction, filtered, unfiltered, rssi, noise)
    }

    override fun getId(): String {
        return id
    }

    override fun getDevice(): String {
        return device
    }

    override fun getDate(): Date {
        return Date(date)
    }

    enum class Type {
        SGV
        MBG
        CAL
        UNKNOWN
    }
}