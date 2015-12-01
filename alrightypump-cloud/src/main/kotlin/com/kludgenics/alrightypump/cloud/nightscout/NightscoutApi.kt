package com.kludgenics.alrightypump.cloud.nightscout

import com.kludgenics.alrightypump.cloud.nightscout.records.json.Cal
import com.kludgenics.alrightypump.cloud.nightscout.records.json.NightscoutEntry
import com.kludgenics.alrightypump.cloud.nightscout.records.json.Sgv
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.okhttp.RequestBody
import com.squareup.okhttp.ResponseBody
import org.joda.time.Instant
import retrofit.Call
import retrofit.http.*
import kotlin.properties.getValue

interface NightscoutApiEntry : NightscoutEntry {
    override val date: Instant get() = Instant.parse(dateString)
    override val dateString: String get() = date.toString()
}

interface NightscoutApiMbgEntry : NightscoutApiEntry {
    val mbg: Int
}

interface NightscoutApiSgvEntry : NightscoutApiEntry, Sgv {
}

interface NightscoutApiCalEntry : NightscoutApiEntry, Cal {
    val decay: Double?
}

interface NightscoutApiTreatment : NightscoutApiBaseTreatment {
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
    override val date: Instant get() = Instant.parse(created_at)
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

data class NightscoutTreatment(public val map: MutableMap<String, Any?>) : NightscoutApiTreatment {
    // these two aren't present, but it is useful to unify treatments and entries
    override val device: String get() = enteredBy
    override val type: String get() = eventType

    override val _id: String? by map
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

    private data class NightscoutCarbTreatment(public override val carbs: Int,
                                               public val rawTreatment: NightscoutTreatment) :
            NightscoutApiCarbTreatment,
            NightscoutApiBaseTreatment by rawTreatment

    private data class NightscoutBolusTreatment(public override val insulin: Double,
                                                public val rawTreatment: NightscoutTreatment) :
            NightscoutApiBolusTreatment,
            NightscoutApiBaseTreatment by rawTreatment

    private data class NightscoutTempBasalTreatment(public override val rate: Double?,
                                                    public override val percent: Double?,
                                                    public override val absolute: Double?,
                                                    public override val duration: Long,
                                                    public val rawTreatment: NightscoutTreatment) :
            NightscoutApiTempBasalTreatment,
            NightscoutApiBaseTreatment by rawTreatment

    private data class NightscoutProfileChangeTreatment(public override val profile: String,
                                                        public val rawTreatment: NightscoutTreatment) :
            NightscoutApiProfileChangeTreatment,
            NightscoutApiBaseTreatment by rawTreatment

    fun asCarbTreatment(): NightscoutApiCarbTreatment? {
        return if (carbs != null)
            NightscoutCarbTreatment(carbs!!, this)
        else null
    }

    fun asBolusTreatment(): NightscoutApiBolusTreatment? {
        return if (insulin != null)
            NightscoutBolusTreatment(insulin!!, this)
        else null
    }

    fun asTempBasalTreatment(): NightscoutApiTempBasalTreatment? {
        return if (duration != null && (rate != null || absolute != null || percent != null))
            NightscoutTempBasalTreatment(rate, absolute, percent, duration!!, this)
        else null
    }

    fun asProfileChangeTreatment(): NightscoutApiProfileChangeTreatment? {
        return if (profile != null)
            NightscoutProfileChangeTreatment(profile!!, this)
        else null
    }
}

class NightscoutJsonAdapter {

    @FromJson
    public fun treatmentFromJson(map: MutableMap<String, Any?>): NightscoutTreatment {
        return NightscoutTreatment(map.withDefault { null })
    }

    @ToJson
    public fun treatmentToJson(treatment: NightscoutTreatment): Map<String, Any?> {
        return treatment.map
    }

    @ToJson
    public fun entryToJson(entry: NightscoutEntryJson): Map<String, Any?> {
        val map = hashMapOf<String, Any?>()
        map.putAll("_id" to entry._id,
                "date" to entry.date.millis,
                "dateString" to entry.dateString,
                "device" to entry.device,
                "type" to entry.type)
        when (entry.rawEntry) {
            is NightscoutApiSgvEntry -> {
                map.putAll("sgv" to entry.rawEntry.sgv,
                        "direction" to entry.rawEntry.direction,
                        "filtered" to entry.rawEntry.filtered,
                        "unfiltered" to entry.rawEntry.unfiltered,
                        "noise" to entry.rawEntry.noise,
                        "rssi" to entry.rawEntry.rssi,
                        "type" to "sgv")
            }
            is NightscoutApiMbgEntry ->
                    map.putAll("mbg" to entry.rawEntry.mbg)
            is NightscoutApiCalEntry ->
                    map.putAll("slope" to entry.rawEntry.slope,
                            "intercept" to entry.rawEntry.intercept,
                            "scale" to entry.rawEntry.scale,
                            "decay" to entry.rawEntry.decay)
        }
        return map
    }

    @FromJson
    public fun entryFromJson(entry: MutableMap<String, String>): NightscoutEntryJson {
        return when(entry.getOrElse("type", {null})) {
            "sgv" -> {
                NightscoutEntryJson(NightscoutSgvJson(_id=entry["_id"] as String,
                        type = entry["type"]!!,
                        date = Instant(entry["date"]?.toLong()!!),
                        dateString = entry["dateString"]!!,
                        device = entry["device"]!!,
                        sgv = entry["sgv"]?.toInt()!!,
                        filtered = entry["filtered"]?.toDouble()?.toInt(), // this works around an old xDrip bug
                        unfiltered = entry["unfiltered"]?.toDouble()?.toInt(), // this works around an old xDrip bug
                        direction = entry["direction"]!!,
                        noise = entry["noise"]?.toInt(),
                        rssi = entry["rssi"]?.toInt()))

            }
            "mbg" -> NightscoutEntryJson(NightscoutMbgJson(_id=entry["_id"] as String,
                    type = entry["type"] as String,
                    date = Instant(entry["date"]?.toLong()!!),
                    dateString = entry["dateString"] as String,
                    device = entry["device"] as String,
                    mbg = entry["mbg"]?.toInt()!!))
            "cal" -> NightscoutEntryJson(NightscoutCalJson(_id=entry["_id"] as String,
                    type = entry["type"] as String,
                    date = Instant(entry["date"]?.toLong()!!),
                    dateString = entry["dateString"] as String,
                    device = entry["device"] as String,
                    slope = entry["slope"]?.toDouble()!!,
                    intercept = entry["intercept"]?.toDouble()!!,
                    scale = entry["scale"]?.toDouble()!!,
                    decay = entry["decay"]?.toDouble()))
            else -> throw JsonDataException("Invalid type: ${entry.getOrElse("type", {"null"})}")
        }
    }
}

data class NightscoutEntryJson(public val rawEntry: NightscoutApiEntry) : NightscoutApiEntry by rawEntry

data class NightscoutSgvJson(public override val _id: String?,
                             public override val type: String,
                             public override val dateString: String,
                             public override val date: Instant,
                             public override val device: String,
                             public override val sgv: Int,
                             public override val direction: String,
                             public override val noise: Int?,
                             public override val filtered: Int?,
                             public override val unfiltered: Int?,
                             public override val rssi: Int?) : NightscoutApiSgvEntry

data class NightscoutMbgJson(public override val _id: String?,
                             public override val type: String,
                             public override val dateString: String,
                             public override val date: Instant,
                             public override val device: String,
                             public override val mbg: Int) : NightscoutApiMbgEntry

data class NightscoutCalJson(public override val _id: String?,
                             public override val type: String,
                             public override val dateString: String,
                             public override val date: Instant,
                             public override val device: String,
                             public override val slope: Double,
                             public override val intercept: Double,
                             public override val scale: Double,
                             public override val decay: Double?) : NightscoutApiCalEntry


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
    fun postRecords(@Header("api-secret") apiSecret: String, @Body body: MutableList<NightscoutEntryJson>): Call<MutableList<NightscoutEntryJson>>

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
    fun postTreatments(@Header("api-secret") apiSecret: String, @Body treatments: MutableList<NightscoutTreatment>): Call<ResponseBody>

    @GET("/api/v1/profile")
    fun getProfile(): Call<ResponseBody>

    @POST("/api/v1/profile")
    fun postProfiles(@Header("api-secret") apiSecret: String, @Body profile: RequestBody): Call<ResponseBody>
    /*
 startDate:2015-11-23T02:38:00.000Z
defaultProfile:New profile
store[Default][dia]:3
store[Default][carbratio][0][time]:00:00
store[Default][carbratio][0][value]:30
store[Default][carbs_hr]:20
store[Default][delay]:20
store[Default][sens][0][time]:00:00
store[Default][sens][0][value]:100
store[Default][timezone]:UTC
store[Default][basal][0][time]:00:00
store[Default][basal][0][value]:0.1
store[Default][target_low][0][time]:00:00
store[Default][target_low][0][value]:0
store[Default][target_high][0][time]:00:00
store[Default][target_high][0][value]:0
store[Default][units]:mg/dl
store[New profile][dia]:3
store[New profile][carbratio][0][time]:00:00
store[New profile][carbratio][0][value]:30
store[New profile][carbs_hr]:20
store[New profile][delay]:20
store[New profile][sens][0][time]:00:00
store[New profile][sens][0][value]:100
store[New profile][timezone]:UTC
store[New profile][basal][0][time]:00:00
store[New profile][basal][0][value]:0.1
store[New profile][target_low][0][time]:00:00
store[New profile][target_low][0][value]:0
store[New profile][target_high][0][time]:00:00
store[New profile][target_high][0][value]:0
store[New profile][units]:mg/dl

 {"startDate":"2015-11-23T02:38:00.000Z",
    "defaultProfile":"New profile",
    "store":
        {"Default":
            {"dia":"3",
            "carbratio":[{"time":"00:00","value":"30"}],
            "carbs_hr":"20",
            "delay":"20",
            "sens":[{"time":"00:00","value":"100"}],
            "timezone":"UTC",
            "basal":[{"time":"00:00","value":"0.1"}],
            "target_low":[{"time":"00:00","value":"0"}],
            "target_high":[{"time":"00:00","value":"0"}],
            "units":"mg/dl"
            },
        "New profile":{
          "dia":"3",
          "carbratio":[{"time":"00:00","value":"30"}],
          "carbs_hr":"20",
          "delay":"20",
          "sens":[{"time":"00:00","value":"100"}],
          "timezone":"UTC",
          "basal":[{"time":"00:00","value":"0.1"}],
          "target_low":[{"time":"00:00","value":"0"}],
          "target_high":[{"time":"00:00","value":"0"}],
          "units":"mg/dl"}},"_id":"56527c4a13170c0f0fd217b3","created_at":"2015-11-23T02:40:39.040Z"} */
}
