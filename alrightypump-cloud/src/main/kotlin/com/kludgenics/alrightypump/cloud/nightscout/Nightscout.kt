package com.kludgenics.alrightypump.cloud.nightscout

import com.kludgenics.alrightypump.cloud.nightscout.records.Cal
import com.kludgenics.alrightypump.cloud.nightscout.records.Meter
import com.kludgenics.alrightypump.cloud.nightscout.records.NightscoutEntry
import com.kludgenics.alrightypump.cloud.nightscout.records.Sgv
import com.squareup.okhttp.ResponseBody
import retrofit.Call
import retrofit.http.Body
import retrofit.http.GET
import retrofit.http.POST
import retrofit.http.Query

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
}
