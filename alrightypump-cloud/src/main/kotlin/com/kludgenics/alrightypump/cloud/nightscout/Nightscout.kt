package com.kludgenics.alrightypump.cloud.nightscout

import com.kludgenics.alrightypump.cloud.nightscout.records.Cal
import com.kludgenics.alrightypump.cloud.nightscout.records.Meter
import com.kludgenics.alrightypump.cloud.nightscout.records.NightscoutEntry
import com.kludgenics.alrightypump.cloud.nightscout.records.Sgv
import com.kludgenics.alrightypump.therapy.*
import com.squareup.okhttp.RequestBody
import com.squareup.okhttp.ResponseBody
import org.joda.time.Instant
import retrofit.Call
import retrofit.http.Body
import retrofit.http.GET
import retrofit.http.POST
import retrofit.http.Query
import java.util.*



interface NightscoutRecord : Record, NightscoutEntry {
    override val time: Instant
        get() = Instant(date)
    override val source: String
        get() = "nightscout-$id"
}

data class NightscoutSgv(override val sgv: Int,
                         override val direction: String,
                         override val filtered: Double?,
                         override val unfiltered: Double?,
                         override val rssi: Int?,
                         override val noise: Int?,
                         override val id: String,
                         override val device: String,
                         override val date: Long) : NightscoutRecord, Sgv, CgmRecord, GlucoseValue {
    override val glucose: Double
        get() = sgv.toDouble()
    override val unit: Int
        get() = GlucoseUnit.MGDL
    override val value: GlucoseValue
        get() = this
}

data class NightscoutMeter(override val mbg: Int,
                           override val id: String,
                           override val device: String,
                           override val type: String,
                           override val date: Long) : NightscoutRecord, Meter, SmbgRecord, GlucoseValue {
    override val value: GlucoseValue
        get() = this
    override val manual: Boolean
        get() = true
    override val glucose: Double?
        get() = mbg.toDouble()
    override val unit: Int
        get() = GlucoseUnit.MGDL
}

data class NightscoutCal(override val slope: Double,
                         override val intercept: Double,
                         override val scale: Double,
                         override val id: String,
                         override val device: String,
                         override val date: Long) : Cal

interface NightscoutApi {
    @GET("/api/v1/entries/sgv.json")
    fun getSgvRecords(@Query("count") count: Int, @Query("find[dateString][\$gte]") start: String): Call<ArrayList<NightscoutSgv>>

    @GET("/api/v1/entries/mbg.json")
    fun getMeterRecords(@Query("count") count: Int, @Query("find[dateString][\$gte]") start: String): Call<ArrayList<NightscoutMeter>>

    @GET("/api/v1/entries/cal.json")
    fun getCalRecords(@Query("count") count: Int, @Query("find[dateString][\$gte]") start: String): Call<ArrayList<NightscoutCal>>

    @POST("/api/v1/entries")
    fun postRecords(@Body body: RequestBody): Call<ResponseBody>

    @GET("/api/v1/treatments")
    fun getTreatmentsSince(@Query("find[created_at][\$gte]") since: String): Call<ArrayList<Map<String, String>>>

    @GET("/api/v1/treatments")
    fun getTreatmentsSince(@Query("find[created_at][\$gte]") since: String, @Query("count") count: Int): Call<LinkedHashMap<String, String>>

    @POST("/api/v1/treatments")
    fun postTreatments(@Body treatments: ArrayList<Map<String, String>>): Call<ResponseBody>

    @GET("/api/v1/profile")
    fun getProfile(): Call<ResponseBody>

    @POST("/api/v1/profile")
    fun postProfiles(@Body profile: RequestBody): Call<ResponseBody>
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
