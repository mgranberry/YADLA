package com.kludgenics.cgmlogger.data.logdata.glucose.data;

import retrofit.http.GET;
import retrofit.http.Query;

import java.util.List;

/**
 * Created by matthiasgranberry on 5/21/15.
 */
interface NightscoutApiEndpoint {
    @GET("/api/v1/entries.json")
    List<BloodGlucose> getEntries(@Query("count") int count);

    @GET("/api/v1/treatments")
    List<Integer> getTreatments();
}
