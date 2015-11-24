package com.kludgenics.cgmlogger.model.nightscout

import retrofit.Call
import retrofit.http.GET
import retrofit.http.Query
import java.util.*

/**
 * Created by matthiasgranberry on 6/3/15.
 */
interface NightscoutApiEndpoint {
    @GET("/api/v1/entries/sgv.json")
    fun getSgvEntries(@Query("count") count: Int, @Query("find[dateString][\$gte]") start: String): Call<ArrayList<SgvEntry>>

    @GET("/api/v1/treatments")
    fun getTreatmentsSince(@Query("find[created_at][\$gte]") start: String): Call<ArrayList<LinkedHashMap<String, String>>>
}