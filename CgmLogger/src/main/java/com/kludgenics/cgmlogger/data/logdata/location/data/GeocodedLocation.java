package com.kludgenics.cgmlogger.data.logdata.location.data;

import com.kludgenics.cgmlogger.data.logdata.location.data.Position;

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
