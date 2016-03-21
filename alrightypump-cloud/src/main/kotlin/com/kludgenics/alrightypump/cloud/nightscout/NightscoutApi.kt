package com.kludgenics.alrightypump.cloud.nightscout

import com.kludgenics.alrightypump.cloud.nightscout.records.json.Cal
import com.kludgenics.alrightypump.cloud.nightscout.records.json.NightscoutEntry
import com.kludgenics.alrightypump.cloud.nightscout.records.json.Sgv
import com.kludgenics.alrightypump.cloud.nightscout.records.therapy.NightscoutGlucoseValue
import com.kludgenics.alrightypump.device.dexcom.g4.DexcomCgmRecord
import com.kludgenics.alrightypump.therapy.*
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.joda.time.*
import retrofit2.Call
import retrofit2.http.*
import java.text.DecimalFormat
import java.util.*

interface NightscoutApiEntry : NightscoutEntry, Record {
    override val time: LocalDateTime get() = LocalDateTime.parse(dateString)
    override val dateString: String get() = time.toString()
}

interface NightscoutApiMbgEntry : NightscoutApiEntry, SmbgRecord {
    val mbg: Int
}

interface NightscoutApiSgvEntry : NightscoutApiEntry, Sgv, RawCgmRecord {
}

interface NightscoutApiCalEntry : NightscoutApiEntry, Cal, CalibrationRecord {
    val decay: Double?
}

interface NightscoutApiTreatment : NightscoutApiBaseTreatment, Record {
    val glucose: String?
    val glucoseType: String?
    val carbs: Int?
    val insulin: Double?
    val units: String?
    val duration: Long?
    val rate: Double?
    val percent: Double?
    val absolute: Double?
    val profile: String?
}

interface NightscoutApiBaseTreatment : NightscoutApiEntry {
    // these two aren't quite accurate, but it is useful to unify entries and treatments.
    override val time: LocalDateTime get() = LocalDateTime.parse(created_at)
    override val dateString: String get() = created_at
    val eventType: String
    val created_at: String
    val enteredBy: String
    val notes: String?
}

interface NightscoutApiCarbTreatment : NightscoutApiBaseTreatment {
    val carbs: Int
}

interface NightscoutApiBolusTreatment : NightscoutApiBaseTreatment {
    val insulin: Double
}

interface NightscoutApiTempBasalTreatment : NightscoutApiBaseTreatment {
    val rate: Double?
    val percent: Double?
    val absolute: Double?
    val duration: Long
}

interface NightscoutApiProfileChangeTreatment : NightscoutApiBaseTreatment {
    val profile: String
}

interface NightscoutProfile {
    val startDate: LocalDateTime
    val defaultProfile: String
    val store: Map<String, NightscoutProfileEntry>
    val created_at: LocalDateTime
}

interface NightscoutProfileEntry {
    val dia: String
    val carbRatio: ArrayList<NightscoutProfileItem>
    val carbs_hr: String
    val delay: String
    val sens: ArrayList<NightscoutProfileItem>
    val timezone: String
    val basal: ArrayList<NightscoutProfileItem>
    val target_low: ArrayList<NightscoutProfileItem>
    val target_high: ArrayList<NightscoutProfileItem>
    val units: String
}

interface NightscoutProfileItem {
    val time: LocalTime
    val value: String
    val timeAsSeconds: String
}

open class NightscoutTreatment(private val _map: MutableMap<String, Any?>) : NightscoutApiTreatment {
    override val time: LocalDateTime
        get() = time

    companion object {
        val bolusFormat = DecimalFormat("####.##")
        val basalFormat = DecimalFormat("###.###")
    }

    val map: Map<String, Any?> get() = _map

    // these two aren't present, but it is useful to unify treatments and entries
    override val source: String get() = enteredBy
    override val type: String get() = eventType

    override val id: String? by map
    override val eventType: String by map
    override val created_at: String by map
    override val enteredBy: String by map
    override val notes: String? by map
    override val glucose: String? by map
    override val glucoseType: String? by map
    override val carbs: Int? by map
    override val insulin: Double? by map
    override val units: String? by map
    override val duration: Long? by map
    override val rate: Double? by map
    override val percent: Double? by map
    override val absolute: Double? by map
    override val profile: String? by map

    fun applyRecord(record: Record) {
        _map.putAll(arrayOf<Pair<String, Any?>>(
                "enteredBy" to record.source,
                "created_at" to record.time.safeDateTime().toString(),
                //"notes" to record.toString(),
                "eventType" to "<none>"))
        if (record is NormalBolusRecord)
            _map.put("insulin", bolusFormat.format(record.requestedNormal).toDouble())
        if (record is BolusRecord) {
            if (record.bolusWizard != null) {
                _map.putAll(arrayOf("glucose" to if (record.bolusWizard?.bg?.glucose != 0.0) record.bolusWizard?.bg?.glucose else null,
                        "units" to if (record.bolusWizard?.bg != null && record.bolusWizard?.bg?.glucose != 0.0) {
                            if (record.bolusWizard?.bg?.unit == GlucoseUnit.MGDL)
                                "mg/dl"
                            else "mmol"
                        } else null,
                        "carbs" to if (record.bolusWizard?.carbs != 1) record.bolusWizard?.carbs else null,
                        "eventType" to
                                if ((record.bolusWizard?.recommendation?.carbBolus ?: 0.0) >=
                                        (record.bolusWizard?.recommendation?.correctionBolus ?: 0.0)) "Meal Bolus"
                                else "Correction Bolus"))
            }
        }
        if (record is ExtendedBolusRecord) {
            _map.putAll(arrayOf<Pair<String, Any?>>("enteredInsulin" to basalFormat.format((record.requestedNormal ?: 0.0) + record.requestedExtended).toDouble(),
                    "duration" to (record.extendedDuration ?: record.expectedExtendedDuration).standardMinutes,
                    "relative" to basalFormat.format((record.deliveredExtended ?: record.requestedExtended)
                            / (record.expectedExtendedDuration.standardMinutes / 60.00)).toDouble(),
                    "eventType" to "Combo Bolus"))
        }
        if (record is TemporaryBasalRecord) {
            if (record.rate != null) {
                _map["rate"] = basalFormat.format(record.rate!!).toDouble()
                _map["absolute"] = basalFormat.format(record.rate!!).toDouble()
            }
            _map.putAll(
                    arrayOf("duration" to record.duration.standardMinutes,
                            "percent" to record.percent?.minus(100.0),
                            "eventType" to "Temp Basal"))
        }
        if (record is FoodRecord) {
            _map.putAll(arrayOf<Pair<String, Any?>>("carbs" to record.carbohydrateGrams))
        }
        if (record is CgmInsertionRecord) {
            _map.putAll(arrayOf<Pair<String, Any?>>("eventType" to "Sensor Start"))
        }
        if (record is CannulaChangedRecord) {
            _map.putAll(arrayOf<Pair<String, Any?>>("eventType" to "Site Change"))
        }
        if (record is CartridgeChangeRecord) {
            _map.putAll(arrayOf<Pair<String, Any?>>("eventType" to "Insulin Change"))
        }

    }
}

class NightscoutJsonAdapter {

    @FromJson fun treatmentFromJson(map: MutableMap<String, Any?>): NightscoutTreatment {
        return NightscoutTreatment(map.withDefault { null })
    }

    @ToJson fun treatmentToJson(treatment: NightscoutTreatment): Map<String, Any?> {
        return treatment.map
    }

    @ToJson fun entryToJson(entry: NightscoutEntryJson): Map<String, Any?> {
        val map = hashMapOf<String, Any?>()
        map.putAll(arrayOf("date" to entry.time.safeDateTime().millis,
                "dateString" to entry.dateString,
                "device" to entry.source,
                "type" to entry.type))
        when (entry.rawEntry) {
            is NightscoutApiSgvEntry -> {
                map.putAll(arrayOf("sgv" to entry.rawEntry.sgv,
                        "direction" to entry.rawEntry.direction,
                        "filtered" to entry.rawEntry.filtered,
                        "unfiltered" to entry.rawEntry.unfiltered,
                        "noise" to entry.rawEntry.noise,
                        "rssi" to entry.rawEntry.rssi,
                        "type" to "sgv"))
            }
            is NightscoutApiMbgEntry ->
                map.putAll(arrayOf<Pair<String, Any?>>("mbg" to entry.rawEntry.mbg))
            is NightscoutApiCalEntry ->
                map.putAll(arrayOf<Pair<String, Any?>>("slope" to entry.rawEntry.slope,
                        "intercept" to entry.rawEntry.intercept,
                        "scale" to entry.rawEntry.scale,
                        "decay" to entry.rawEntry.decay))
        }
        return map
    }

    @FromJson fun entryFromJson(entry: MutableMap<String, String>): NightscoutEntryJson {
        return when (entry["type"] ?:  null) {
            "sgv" -> {
                NightscoutEntryJson(NightscoutSgvJson(id = entry["_id"] as String,
                        type = entry["type"]!!,
                        time = LocalDateTime(entry["date"]?.toLong()!!),
                        dateString = entry["dateString"]!!,
                        source = entry["device"]!!,
                        sgv = entry["sgv"]?.toInt()!!,
                        filtered = entry["filtered"]?.toDouble()?.toInt(), // this works around an old xDrip bug
                        unfiltered = entry["unfiltered"]?.toDouble()?.toInt(), // this works around an old xDrip bug
                        direction = entry["direction"]!!,
                        noise = entry["noise"]?.toInt(),
                        rssi = entry["rssi"]?.toInt()))

            }
            "mbg" -> NightscoutEntryJson(NightscoutMbgJson(id = entry["_id"] as String,
                    type = entry["type"] as String,
                    time = LocalDateTime(entry["date"]?.toLong()!!),
                    dateString = entry["dateString"] as String,
                    source = entry["device"] as String,
                    mbg = entry["mbg"]?.toInt()!!))
            "cal" -> NightscoutEntryJson(NightscoutCalJson(id = entry["_id"] as String,
                    type = entry["type"] as String,
                    time = LocalDateTime(entry["date"]?.toLong()!!),
                    dateString = entry["dateString"] as String,
                    source = entry["device"] as String,
                    slope = entry["slope"]?.toDouble()!!,
                    intercept = entry["intercept"]?.toDouble()!!,
                    scale = entry["scale"]?.toDouble()!!,
                    decay = entry["decay"]?.toDouble()))
            else -> throw JsonDataException("Invalid type: ${entry["type"] ?: null}")
        }
    }
}

data class NightscoutEntryJson(val rawEntry: NightscoutApiEntry) : NightscoutApiEntry by rawEntry

data class NightscoutSgvJson(override val id: String?,
                             override val type: String,
                             override val dateString: String,
                             override val time: LocalDateTime,
                             override val source: String,
                             override val sgv: Int,
                             override val direction: String?,
                             override val noise: Int?,
                             override val filtered: Int?,
                             override val unfiltered: Int?,
                             override val rssi: Int?) : NightscoutApiSgvEntry {
    override val value: RawGlucoseValue
        get() = NightscoutGlucoseValue(this)

    companion object {

        fun directionString(direction: Int?): String {
            when (direction) {
                1 -> return "DoubleUp"
                2 -> return "SingleUp"
                3 -> return "FortyFiveUp"
                4 -> return "Flat"
                5 -> return "FortyFiveDown"
                6 -> return "SingleDown"
                7 -> return "DoubleDown"
                else -> return ""
            }
        }
    }

    constructor(record: DexcomCgmRecord) : this(id = record.id, type = "sgv", time = record.time, dateString = record.time.safeDateTime().toString(),
            source = record.source, sgv = record.value.mgdl!!.toInt(), direction = directionString(record.egvRecord.trendArrow), rssi = record.sgvRecord?.rssi,
            unfiltered = record.sgvRecord?.unfiltered,
            filtered = record.sgvRecord?.filtered, noise = record.egvRecord.noise)

    constructor(record: RawCgmRecord) : this(id = record.id, type = "sgv", time = record.time, dateString = record.time.safeDateTime().toString(),
            source = record.source, sgv = record.value.mgdl!!.toInt(), direction = null, rssi = null,
            unfiltered = record.value.unfiltered,
            filtered = record.value.filtered, noise = null)

    constructor(record: CgmRecord) : this(id = record.id, type = "sgv", time = record.time, dateString = record.time.safeDateTime().toString(),
            source = record.source, sgv = record.value.mgdl!!.toInt(), direction = null, rssi = null, unfiltered = null,
            filtered = null, noise = null)

}

data class NightscoutMbgJson(override val id: String?,
                             override val type: String,
                             override val dateString: String,
                             override val time: LocalDateTime,
                             override val source: String,
                             override val mbg: Int) : NightscoutApiMbgEntry {
    override val value: GlucoseValue
        get() = BaseGlucoseValue(mbg.toDouble(), GlucoseUnit.MGDL)
    override val manual: Boolean
        get() = true

    constructor (smbgRecord: SmbgRecord) : this(id = smbgRecord.id, type = "mbg", time = smbgRecord.time,
            dateString = smbgRecord.time.toString(),
            source = smbgRecord.source,
            mbg = smbgRecord.value.mgdl!!.toInt())
}

data class NightscoutCalJson(override val id: String? = null,
                             override val type: String,
                             override val dateString: String,
                             override val time: LocalDateTime,
                             override val source: String,
                             override val slope: Double,
                             override val intercept: Double,
                             override val scale: Double,
                             override val decay: Double? = null) : NightscoutApiCalEntry {
    constructor (calibrationRecord: CalibrationRecord) : this(type = "cal", time = calibrationRecord.time,
            dateString = calibrationRecord.time.toString(),
            source = calibrationRecord.source,
            slope = calibrationRecord.slope,
            intercept = calibrationRecord.intercept,
            scale = calibrationRecord.scale)
}

@Suppress("UNUSED_METHOD")
interface NightscoutApi {
    companion object {
        fun registerTypeAdapters(builder: Moshi.Builder): Moshi.Builder {
            return builder.add(NightscoutJsonAdapter())
        }
    }

    @GET("/api/v1/entries/sgv.json")
    fun getSgvRecords(@Query("count") count: Int, @Query("find[date][\$gte]") start: Long): Call<MutableList<NightscoutSgvJson>>

    @GET("/api/v1/entries/mbg.json")
    fun getMeterRecords(@Query("count") count: Int, @Query("find[date][\$gte]") start: Long): Call<MutableList<NightscoutMbgJson>>

    @GET("/api/v1/entries/cal.json")
    fun getCalRecords(@Query("count") count: Int, @Query("find[date][\$gte]") start: Long): Call<MutableList<NightscoutCalJson>>

    @GET("/api/v1/entries.json")
    fun getRecordsSince(@Query("find[date][\$gte]") since: Long, @Query("count") count: Int): Call<MutableList<NightscoutEntryJson>>

    @GET("/api/v1/entries.json")
    fun getRecordsBefore(@Query("find[date][\$lt]") before: Long, @Query("count") count: Int): Call<MutableList<NightscoutEntryJson>>

    @GET("/api/v1/entries.json")
    fun getRecordsBetween(@Query("find[date][\$gte]") since: Long, @Query("find[date][\$lt]") before: Long, @Query("count") count: Int): Call<MutableList<NightscoutEntryJson>>

    @POST("/api/v1/entries.json")
    @Headers("Content-Type: application/json")
    fun postRecords(@Header("api-secret") apiSecret: String, @Body body: MutableList<NightscoutEntryJson>): Call<MutableList<NightscoutEntryJson>>

    @POST("/api/v1/entries.json")
    @Headers("Content-Type: application/json")
    fun postRecords(@Body body: MutableList<NightscoutEntryJson>): Call<ResponseBody>

    @GET("/api/v1/treatments")
    fun getTreatmentsBefore(@Query("find[created_at][\$lt]") since: String): Call<MutableList<NightscoutTreatment>>

    @GET("/api/v1/treatments")
    fun getTreatmentsBefore(@Query("find[created_at][\$lt]") before: String, @Query("count") count: Int): Call<MutableList<NightscoutTreatment>>

    @GET("/api/v1/treatments")
    fun getTreatmentsSince(@Query("find[created_at][\$gte]") since: String): Call<MutableList<NightscoutTreatment>>

    @GET("/api/v1/treatments")
    fun getTreatmentsSince(@Query("find[created_at][\$gte]") since: String, @Query("count") count: Int): Call<MutableList<NightscoutTreatment>>

    @GET("/api/v1/treatments")
    fun getTreatmentsBetween(@Query("find[created_at][\$gte]") since: String, @Query("find[created_at][\$lt]") before: String, @Query("count") count: Int): Call<MutableList<NightscoutTreatment>>

    @POST("/api/v1/treatments")
    @Headers("Content-Type: application/json")
    fun postTreatments(@Header("api-secret") apiSecret: String, @Body treatments: MutableList<NightscoutTreatment>): Call<ResponseBody>

    @POST("/api/v1/treatments")
    @Headers("Content-Type: application/json")
    fun postTreatments(@Body treatments: MutableList<NightscoutTreatment>): Call<ResponseBody>

    @GET("/api/v1/profile")
    fun getProfile(): Call<ResponseBody>

    @POST("/api/v1/profile")
    fun postProfiles(@Header("api-secret") apiSecret: String, @Body profile: RequestBody): Call<ResponseBody>
}

fun LocalDateTime.safeDateTime(): DateTime = try {
    this.toDateTime()
} catch (e: org.joda.time.IllegalInstantException) {
    val localTime = toLocalTime()
    val localDate = this.toLocalDate().toDateTimeAtStartOfDay().plus(localTime.millisOfDay.toLong())
    this.plus(Duration(DateTimeZone.getDefault().getOffsetFromLocal(localDate.millis).toLong())).toDateTime()
}
