package com.kludgenics.alrightypump.cloud.nightscout

import com.kludgenics.alrightypump.cloud.nightscout.records.Cal
import com.kludgenics.alrightypump.cloud.nightscout.records.Meter
import com.kludgenics.alrightypump.cloud.nightscout.records.NightscoutEntry
import com.kludgenics.alrightypump.cloud.nightscout.records.Sgv
import com.squareup.okhttp.RequestBody
import com.squareup.okhttp.ResponseBody
import retrofit.Call
import retrofit.http.*

interface NightscoutApi<SGV: Sgv, METER: Meter, CAL: Cal, NS: NightscoutEntry> {
    @GET("/api/v1/entries/sgv.json")
    fun getSgvRecords(@Query("count") count: Int, @Query("find[dateString][\$gte]") start: String): Call<MutableList<SGV>>

    @GET("/api/v1/entries/mbg.json")
    fun getMeterRecords(@Query("count") count: Int, @Query("find[dateString][\$gte]") start: String): Call<MutableList<METER>>

    @GET("/api/v1/entries/cal.json")
    fun getCalRecords(@Query("count") count: Int, @Query("find[dateString][\$gte]") start: String): Call<MutableList<CAL>>

    @POST("/api/v1/entries")
    fun postRecords(@Body entries: MutableList<NS>): Call<MutableList<NS>>

    @GET("/api/v1/treatments")
    fun getTreatmentsSince(@Query("find[created_at][\$gte]") since: String): Call<MutableList<Map<String, String>>>

    @GET("/api/v1/treatments")
    fun getTreatmentsSince(@Query("find[created_at][\$gte]") since: String, @Query("count") count: Int): Call<MutableList<Map<String, String>>>

    @POST("/api/v1/treatments")
    fun postTreatments(@Body treatments: MutableList<Map<String, String>>): Call<ResponseBody>

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
