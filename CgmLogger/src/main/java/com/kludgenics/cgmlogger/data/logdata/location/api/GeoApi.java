package com.kludgenics.cgmlogger.data.logdata.location.api;

import com.kludgenics.cgmlogger.data.logdata.location.data.GeocodedLocation;
import com.kludgenics.cgmlogger.data.logdata.location.data.Position;
import rx.Observable;

/**
 * Created by matthiasgranberry on 5/11/15.
 */
public interface GeoApi {
    Observable<GeocodedLocation> getCurrentLocation();
    Observable<GeocodedLocation> getCurrentLocation(String categories);
    Observable<GeocodedLocation> search(Position position);
    Observable<GeocodedLocation> search(Position position, String categories);
    Observable<AutoCompleteResult> autoComplete(Position position, String query);
    Observable<AutoCompleteResult> autoComplete(Position position, String query, String categories);
    Observable<GeocodedLocation> getInfo(String id);
}
