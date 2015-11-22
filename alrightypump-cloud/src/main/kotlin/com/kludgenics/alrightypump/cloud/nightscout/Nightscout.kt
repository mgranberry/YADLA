package com.kludgenics.alrightypump.cloud.nightscout

import retrofit.Call
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

    @retrofit.http.POST("/api/v1/entries")
    fun postRecords(entries: MutableList<NS>): Call<MutableList<NS>>
}
