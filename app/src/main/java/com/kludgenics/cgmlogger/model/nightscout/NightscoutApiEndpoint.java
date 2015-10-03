package com.kludgenics.cgmlogger.model.nightscout;

import java.util.List;
import java.util.Map;

import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by matthiasgranberry on 6/3/15.
 */
public interface NightscoutApiEndpoint {
    @GET("/api/v1/entries.json")
    List<NightscoutApiEntry> getEntries(@Query("count") int count);

    @GET("/api/v1/entries/sgv.json")
    List<SgvEntry> getSgvEntries(@Query("count") int count, @Query("find[dateString][$gte]") String start);

    @GET("/api/v1/treatments")
    List<Map<String, String>> getTreatments();

}