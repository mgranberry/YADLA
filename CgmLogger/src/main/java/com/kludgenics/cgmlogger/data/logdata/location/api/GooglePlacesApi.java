package com.kludgenics.cgmlogger.data.logdata.location.api;

import com.google.gson.annotations.Expose;
import com.kludgenics.cgmlogger.data.logdata.location.data.GooglePlacesLocation;
import retrofit.http.GET;
import retrofit.http.Query;

import java.util.List;

/**
 * Created by matthiasgranberry on 5/23/15.
 */
class GooglePlacesApi {
    interface NearbySearchEndpoint {

        @GET("https://maps.googleapis.com/maps/api/place/nearbysearch/json")
        NearbySearchResponse nearbySearch(@Query("key") String key, @Query("location") String location, @Query("types") String types);
    }

    class NearbySearchResponse {
        @Expose
        String htmlAttributions;
        @Expose
        List<GooglePlacesWebLocation> locations;
    }
}
