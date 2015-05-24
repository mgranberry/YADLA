package com.kludgenics.cgmlogger.data.logdata.location.api;

import android.util.Log;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.*;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.kludgenics.cgmlogger.data.logdata.location.data.GooglePlacesLocation;
import com.kludgenics.cgmlogger.data.logdata.location.data.Location;
import com.kludgenics.cgmlogger.data.logdata.location.data.Position;
import org.apache.commons.lang3.StringUtils;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by matthiasgranberry on 5/23/15.
 */
class GooglePlacesApi implements GeoApi {

    private GoogleApiClient mClient;
    private NearbySearchEndpoint mGooglePlacesEndpoint;
    private static final String PLACES_API_URL="https://maps.googleapis.com/";
    private static final String MAPS_API_KEY="AIzaSyCah6xLW8jBtxdgmZrFVLrQx7rWQbDkSxI";
    private static final String TAG = "GooglePlacesApi";
    private PlaceFilter filter;

    public static final Func1<NearbySearchResponse, Observable<GooglePlacesWebLocation>> RESPONSE_LOCATION_FUNC = new Func1<NearbySearchResponse, Observable<GooglePlacesWebLocation>>() {
        @Override
        public Observable<GooglePlacesWebLocation> call(NearbySearchResponse nearbySearchResponse) {
            return Observable.from(nearbySearchResponse.results);
        }
    };
    private static final Func1<GooglePlacesWebLocation, Observable<String>> LOCATION_ID_FUNC = new Func1<GooglePlacesWebLocation, Observable<String>>() {
        @Override
        public Observable<String> call(GooglePlacesWebLocation googlePlacesWebLocation) {
            Log.d("LOCATION_ID_FUNC", googlePlacesWebLocation.getName().toString() + ": " + googlePlacesWebLocation.getId());

            return Observable.just(googlePlacesWebLocation.getId());
        }
    };

    public GooglePlacesApi () {
        this(null);
    }

    public GooglePlacesApi (GoogleApiClient client) {
        mClient = client;
        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(PLACES_API_URL)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setConverter(new GsonConverter(new GsonBuilder()
                        .excludeFieldsWithoutExposeAnnotation()
                        .create())).build();
        mGooglePlacesEndpoint =
                adapter.create(NearbySearchEndpoint.class);


    }

    @Override
    public Observable<Location> getCurrentLocation() {
        return getCurrentLocation("");
    }

    @Override
    public Observable<Location> getCurrentLocation(String categories) {

        ArrayList<String> placeSet = new ArrayList<String>();
        NearbySearchResponse response = null;

        if (categories.contains("food"))  {
            Log.d(TAG, "foodQuery");

            Observable<NearbySearchResponse> responseObservable = mGooglePlacesEndpoint.nearbySearchObservable(MAPS_API_KEY, "30.485277,-97.678878", 200, categories);

            // Perform a Places API query on the returned results of the filtered WS call

            return responseObservable
                    .flatMap(RESPONSE_LOCATION_FUNC)
                    .flatMap(LOCATION_ID_FUNC)
                    .toList()
                    .flatMap(new Func1<List<String>, Observable<Location>>() {

                        @Override
                        public Observable<Location> call(List<String> placeIds) {
                            final PlaceFilter filter = new PlaceFilter(true, placeIds);
                            return Observable.create(new Observable.OnSubscribe<Location>() {
                                @Override
                                public void call(final Subscriber<? super Location> subscriber) {
                                    Log.d(TAG, "filter: " + StringUtils.join(filter.getPlaceIds(),"|"));
                                    PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace(mClient, filter);

                                    result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                                        @Override
                                        public void onResult(PlaceLikelihoodBuffer placeLikelihoods) {
                                            for (PlaceLikelihood placeLikelihood: placeLikelihoods) {
                                                Place place = placeLikelihood.getPlace();
                                                if (!subscriber.isUnsubscribed() && filter.matches(place)) // this filter.matches(place) is a work-around for a google bug
                                                    subscriber.onNext(new GooglePlacesLocation(place, placeLikelihood.getLikelihood(), placeLikelihoods.getAttributions()));
                                            }
                                            Status status = placeLikelihoods.getStatus();
                                            if (!status.isSuccess()) {
                                                // @TODO propagate this error somehow
                                                // subscriber.onError();
                                            }
                                            placeLikelihoods.release();
                                            if (!subscriber.isUnsubscribed())
                                                subscriber.onCompleted();
                                        }
                                    });
                                }
                            });
                        }
                    });
        } else {
            return Observable.create(new Observable.OnSubscribe<Location>() {

                @Override
                public void call(final Subscriber<? super Location> subscriber) {
                    PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace(mClient, null);
                    result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                        @Override
                        public void onResult(PlaceLikelihoodBuffer placeLikelihoods) {
                            for (PlaceLikelihood placeLikelihood : placeLikelihoods) {
                                Place place = placeLikelihood.getPlace();
                                if (!subscriber.isUnsubscribed())
                                    subscriber.onNext(new GooglePlacesLocation(place, placeLikelihood.getLikelihood(), placeLikelihoods.getAttributions()));
                            }
                            Status status = placeLikelihoods.getStatus();
                            if (!status.isSuccess()) {
                                // @TODO propagate this error somehow
                                // subscriber.onError();
                            }
                            placeLikelihoods.release();
                            if (!subscriber.isUnsubscribed())
                                subscriber.onCompleted();
                        }
                    });
                }
            });
        }
    }

    @Override
    public Observable<Location> search(Position position) {
        return search(position, null);
    }

    @Override
    public Observable<Location> search(Position position, final String categories) {
        return Observable.create(new Observable.OnSubscribe<Location>() {
            @Override
            public void call(Subscriber<? super Location> subscriber) {

            }
        });
    }

    @Override
    public Observable<AutoCompleteResult> autoComplete(Position position, String query) {
        return autoComplete(position, query, null);
    }

    @Override
    public Observable<AutoCompleteResult> autoComplete(Position position, String query, String categories) {
        return null;
    }

    @Override
    public Observable<Location> getInfo(String id) {
        final String placeId = id;

        return Observable.create(new Observable.OnSubscribe<Location>() {

            @Override
            public void call(final Subscriber<? super Location> subscriber) {
                PendingResult<PlaceBuffer> result = Places.GeoDataApi.getPlaceById(mClient, placeId);
                result.setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(PlaceBuffer places) {
                        for (Place place: places) {
                            if (!subscriber.isUnsubscribed())
                                subscriber.onNext(new GooglePlacesLocation(place, 1.0f, places.getAttributions()));
                        }
                        Status status = places.getStatus();
                        if (!status.isSuccess()) {
                            // @TODO propagate this error somehow
                            //subscriber.onError();
                        }
                        places.release();
                        if (!subscriber.isUnsubscribed())
                            subscriber.onCompleted();
                    }
                });
            }
        });
    }

    interface NearbySearchEndpoint {

        @GET("/maps/api/place/nearbysearch/json")
        NearbySearchResponse nearbySearch(@Query("key") String key, @Query("location") String location, @Query("radius") float radius);

        @GET("/maps/api/place/nearbysearch/json")
        NearbySearchResponse nearbySearch(@Query("key") String key, @Query("location") String location, @Query("radius") float radius, @Query("types") String types);

        @GET("/maps/api/place/nearbysearch/json")
        Observable<NearbySearchResponse> nearbySearchObservable(@Query("key") String key, @Query("location") String location, @Query("radius") float radius);

        @GET("/maps/api/place/nearbysearch/json")
        Observable<NearbySearchResponse> nearbySearchObservable(@Query("key") String key, @Query("location") String location, @Query("radius") float radius, @Query("types") String types);


    }

    static class NearbySearchResponse {
        @Expose
        String htmlAttributions;
        @Expose
        List<GooglePlacesWebLocation> results;

        public NearbySearchResponse() {
        }
    }
}
