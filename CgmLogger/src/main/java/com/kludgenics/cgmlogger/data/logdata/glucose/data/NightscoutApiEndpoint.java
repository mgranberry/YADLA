package com.kludgenics.cgmlogger.data.logdata.glucose.data;

import com.kludgenics.cgmlogger.data.logdata.glucose.data.nightscout.NightscoutApiEntry;
import com.kludgenics.cgmlogger.data.logdata.glucose.data.nightscout.NightscoutApiTreatment;
import retrofit.http.GET;
import retrofit.http.Query;

import java.util.List;

/**
 * Created by matthiasgranberry on 5/21/15.
 */
interface NightscoutApiEndpoint {
    @GET("/api/v1/entries.json")
    List<NightscoutApiEntry> getEntries(@Query("count") int count);

    @GET("/api/v1/treatments")
    List<NightscoutApiTreatment> getTreatments();
}
