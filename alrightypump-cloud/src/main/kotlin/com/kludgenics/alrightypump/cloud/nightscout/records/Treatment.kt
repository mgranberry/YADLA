package com.kludgenics.alrightypump.cloud.nightscout.records

import org.joda.time.format.DateTimeFormatter
import java.util.*

interface Treatment {
    companion object {
        fun fromMap(treatment: Treatment, treatmentMap: Map<String, String>) {
            treatment.id = treatmentMap["_id"]!!
            treatment.eventTime = treatment.dateParser.parseDateTime(treatmentMap["created_at"]).toDate()
            treatment.eventType = treatmentMap["eventType"]
            treatment.enteredBy = treatmentMap["enteredBy"]

            treatment.glucose = treatmentMap["glucose"]?.toDouble()
            treatment.glucoseType = treatmentMap["glucoseType"]

            treatment.insulin = treatmentMap["insulin"]?.toDouble()
            treatment.units = treatmentMap["units"]
            treatment.notes = treatmentMap["notes"]
            treatment.carbs = treatmentMap["carbs"]?.toInt()
            treatment.preBolus = treatmentMap["preBolus"]?.toInt()
        }

        fun toMap(treatment: Treatment) : Map<String, Any?> {
            val map = mapOf(
                    "_id" to treatment.id,
                    "created_at" to treatment.dateTimeString,
                    "eventType" to treatment.eventType,
                    "enteredBy" to treatment.enteredBy,
                    "glucose" to treatment.glucose,
                    "glucoseType" to treatment.glucoseType,
                    "insulin" to treatment.insulin,
                    "units" to treatment.units,
                    "notes" to treatment.notes,
                    "carbs" to treatment.carbs,
                    "preBolus" to treatment.preBolus)
            return map
        }
    }
    open var id: String
    open var eventTime: Date
    open var eventType: String?
    val dateTimeString: String get() = dateParser.print(eventTime.time)
    open var enteredBy: String?
    open var glucose: Double?
    open var glucoseType: String?
    open var insulin: Double?
    open var units: String?
    open var notes: String?
    open var carbs: Int?
    open var preBolus: Int?
    val dateParser: DateTimeFormatter
}