package com.kludgenics.cgmlogger.model.nightscout;

import java.util.List;

import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

/**
 * Created by matthiasgranberry on 6/3/15.
 */
public interface NightscoutApiEndpoint {
    @GET("/api/v1/entries.json")
    List<NightscoutApiEntry> getEntries(@Query("count") int count);

    @GET("/api/v1/entries.json")
    Observable<List<NightscoutApiEntry>> getEntriesObservable(@Query("count") int count);

    @GET("/api/v1/treatments")
    List<NightscoutApiTreatment> getTreatments();

    @GET("/api/v1/treatments")
    Observable<List<NightscoutApiTreatment>> getTreatmentsObservable();

}