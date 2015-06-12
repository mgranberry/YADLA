package com.kludgenics.cgmlogger.model.location.data;

import com.kludgenics.cgmlogger.model.location.Position;

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
