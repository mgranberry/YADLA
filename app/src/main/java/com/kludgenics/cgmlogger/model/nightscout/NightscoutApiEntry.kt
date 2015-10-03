package com.kludgenics.cgmlogger.model.nightscout

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.kludgenics.cgmlogger.model.glucose.BloodGlucose
import com.kludgenics.cgmlogger.model.glucose.BloodGlucoseRecord
import io.realm.RealmObject
import java.util.Date

/**
 * Created by matthiasgranberry on 5/24/15.
 */
public class NightscoutApiEntry : NightscoutEntry {
    @Expose
    @SerializedName("_id")
    private val id: String = ""
    @Expose
    private val slope: Double = 0.0
    @Expose
    private val intercept: Double = 0.0
    @Expose
    private val scale: Double = 0.0
    @Expose
    private val mbg: Double = 0.0
    @Expose
    private val device: String = ""
    @Expose
    private val date: Long = 0
    @Expose
    private val sgv: Double = 0.0
    @Expose
    private val direction: String = ""
    @Expose
    private val type: String = ""
    @Expose
    private val filtered: Double = 0.0
    @Expose
    private val unfiltered: Double = 0.0
    @Expose
    private val rssi: Double = 0.0
    @Expose
    private val noise: Double = 0.0

    public fun getType(): String {
        return type
    }

    override fun getId(): String {
        return id
    }

    override fun getDevice(): String {
        return device
    }

    override fun getDate(): Long {
        return date
    }
}