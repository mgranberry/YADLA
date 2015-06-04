package com.kludgenics.cgmlogger.model.location.data;

/**
 * Created by matthiasgranberry on 5/12/15.
 */
public interface GeocodedLocation {
    String getId();
    CharSequence getName();
    String getLocationTypes();
    CharSequence getAddress();
    Position getPosition();
    CharSequence getAttributionSnippet();
}
