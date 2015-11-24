package com.kludgenics.cgmlogger.model.treatment

import com.kludgenics.alrightypump.cloud.nightscout.records.Treatment
import com.kludgenics.cgmlogger.extension.dateTime
import java.util.Date

import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import org.joda.time.Duration
import org.joda.time.format.ISODateTimeFormat
import kotlin.properties.Delegates

/**
 * Created by matthiasgranberry on 6/4/15.
 */
open class Treatment : Treatment, RealmObject {

    @PrimaryKey
    override var id: String = ""
    @Required
    override var eventTime: Date = Date()
    override var eventType: String? = null
    override var enteredBy: String? = null

    override var glucose: Double? = null
    override var glucoseType: String? = null

    override var insulin: Double? = null
    override var units: String? = null

    override var notes: String? = null

    override var carbs: Int? = null
    override var preBolus: Int? = null
    override var percent: Int? = null
    override var absolute: Double? = null

    @Ignore
    override var duration: Duration? = null
    open var durationLong: Long? = null
    override var profile: String? = null

    constructor() : super() {
    }

    constructor(treatmentMap: Map<String, String>) {
        duration = if (durationLong != null) Duration(durationLong!!) else null
        Treatment.fromMap(this, treatmentMap)
    }

    constructor(id: String, eventTime: Date, eventType: String, enteredBy: String? = null,
                glucose: Double? = null, glucoseType: String? = null, insulin: Double? = null,
                units: String? = null, notes: String? = null, carbs: Int? = null,
                preBolus: Int? = null, percent: Int? = null, absolute: Double? = null,
                duration: Duration? = null, profile: String? = null) {
        this.id = id
        this.eventTime = eventTime
        this.eventType = eventType
        this.enteredBy = enteredBy
        this.glucose = glucose
        this.glucoseType = glucoseType
        this.insulin = insulin
        this.units = units
        this.notes = notes
        this.carbs = carbs
        this.preBolus = preBolus
        this.percent = percent
        this.absolute = absolute
        this.duration = duration
        this.durationLong = duration?.millis
        this.profile = profile
    }
}

