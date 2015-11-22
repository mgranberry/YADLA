package com.kludgenics.cgmlogger.model.treatment

import com.kludgenics.alrightypump.cloud.nightscout.records.Treatment
import java.util.Date

import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import org.joda.time.format.ISODateTimeFormat

/**
 * Created by matthiasgranberry on 6/4/15.
 */
open class Treatment : Treatment, RealmObject {
    @Ignore
    override val dateTimeString: String = super.dateTimeString
    @Ignore
    override val dateParser = ISODateTimeFormat.dateTimeParser()

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

    constructor() : super() {
    }

    constructor(treatmentMap: Map<String, String>) {
        Treatment.fromMap(this, treatmentMap)
    }

    constructor(id: String, eventTime: Date, eventType: String, enteredBy: String, glucose: Double?, glucoseType: String, insulin: Double?, units: String, notes: String, carbs: Int?, preBolus: Int?) {
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
    }
}

