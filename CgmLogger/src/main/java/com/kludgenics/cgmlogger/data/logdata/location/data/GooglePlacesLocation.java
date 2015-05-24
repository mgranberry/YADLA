package com.kludgenics.cgmlogger.data.logdata.location.data;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by matthiasgranberry on 5/18/15.
 */
public class GooglePlacesLocation implements GeocodedLocation {
    Place mPlace;
    CharSequence mAttribution;
    float mLikelihood;

    public GooglePlacesLocation(Place place, float likelihood, CharSequence attribution) {
        mPlace = place;
        mLikelihood = likelihood;
        mAttribution = attribution;
    }

    @Override
    public String getId() {
        return mPlace.getId();
    }

    @Override
    public CharSequence getName() {
        return mPlace.getName();
    }

    @Override
    public String getLocationTypes() {
        List<Integer> types = mPlace.getPlaceTypes();
        StringBuilder placeString = new StringBuilder();
        for (Integer i: types)
            placeString.append(i).append(';');
        return placeString.toString();
    }

    @Override
    public CharSequence getAddress() {
        return mPlace.getAddress();
    }

    @Override
    public Position getPosition() {
        LatLng latLng = mPlace.getLatLng();
        return new Position(latLng.latitude, latLng.longitude);
    }

    public float getLikelihood() {
        return mLikelihood;
    }

    @Override
    public CharSequence getAttributionSnippet() {
        return mAttribution;
    }
}
