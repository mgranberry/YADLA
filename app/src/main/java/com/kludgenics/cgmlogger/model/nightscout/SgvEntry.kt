package com.kludgenics.cgmlogger.model.nightscout

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.kludgenics.alrightypump.cloud.nightscout.records.Sgv
import com.kludgenics.cgmlogger.model.glucose.BloodGlucose

/**
 * Created by matthiasgranberry on 5/21/15.
 */
public open class SgvEntry() : Sgv, BloodGlucose {
    @Expose
    @SerializedName("_id")
    override var id: String = ""
    @Expose
    override var device: String = ""
    @Expose
    override var date: Long = 0
    @Expose override var sgv: Int = 0
    @Expose override var direction: String = ""
    @Expose override var filtered: Double = 0.0
    @Expose override var unfiltered: Double = 0.0
    @Expose override var rssi: Int = 0
    @Expose override var noise: Int = 0

    public constructor(id: String, device: String, date: Long, sgv: Int, direction: String, filtered: Double, unfiltered: Double, rssi: Int, noise: Int) : this() {
        this.id = id
        this.device = device
        this.date = date
        this.sgv = sgv
        this.direction = direction
        this.filtered = filtered
        this.unfiltered = unfiltered
        this.rssi = rssi
        this.noise = noise
    }

    override val unit: String get() = BloodGlucose.UNIT_MGDL
    override val value: Double get() = sgv.toDouble()

}

