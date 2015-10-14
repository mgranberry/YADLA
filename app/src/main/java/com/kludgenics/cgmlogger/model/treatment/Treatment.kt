package com.kludgenics.cgmlogger.model.treatment

import java.util.Date

import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import org.joda.time.format.ISODateTimeFormat

/**
 * Created by matthiasgranberry on 6/4/15.
 */
open class Treatment : RealmObject {
    @Ignore
    val dateParser = ISODateTimeFormat.dateTimeParser()

    @PrimaryKey
    open var id: String = ""
    @Required
    open var eventTime: Date = Date()
    open var eventType: String? = null
    open var enteredBy: String? = null

    open var glucose: Double? = null
    open var glucoseType: String? = null

    open var insulin: Double? = null
    open var units: String? = null

    open var notes: String? = null

    open var carbs: Int? = null
    open var preBolus: Int? = null

    constructor() : super() {
    }

    constructor(treatmentMap: Map<String, String>) {
        id = treatmentMap.get("_id")!!
        eventTime = dateParser.parseDateTime(treatmentMap.get("created_at")).toDate()
        eventType = treatmentMap.get("eventType")
        enteredBy = treatmentMap.get("enteredBy")

        glucose = treatmentMap.get("glucose")?.toDouble()
        glucoseType = treatmentMap.get("glucoseType")

        insulin = treatmentMap.get("insulin")?.toDouble()
        units = treatmentMap.get("units")
        notes = treatmentMap.get("notes")
        carbs = treatmentMap.get("carbs")?.toInt()
        preBolus = treatmentMap.get("preBolus")?.toInt()
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
