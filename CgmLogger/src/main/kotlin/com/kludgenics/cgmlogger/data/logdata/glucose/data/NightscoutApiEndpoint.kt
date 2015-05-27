package com.kludgenics.cgmlogger.data.logdata.glucose.data

import com.kludgenics.cgmlogger.data.logdata.glucose.data.nightscout.NightscoutApiEntry
import com.kludgenics.cgmlogger.data.logdata.glucose.data.nightscout.NightscoutApiTreatment
import retrofit.http.GET
import retrofit.http.Query
import rx.Observable

/**
 * Created by matthiasgranberry on 5/21/15.
 */
trait NightscoutApiEndpoint {
    GET("/api/v1/entries.json")
    public fun getEntries(Query("count") count: Int): Observable<List<NightscoutApiEntry>>

    GET("/api/v1/treatments")
    public fun getTreatments(): Observable<List<NightscoutApiTreatment>>
}