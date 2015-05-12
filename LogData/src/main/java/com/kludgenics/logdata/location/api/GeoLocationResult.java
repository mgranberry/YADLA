package com.kludgenics.logdata.location.api;

import com.kludgenics.logdata.location.data.Location;
import rx.Observable;

import java.util.List;

/**
 * Created by matthiasgranberry on 5/12/15.
 */
public interface GeoLocationResult {
    Observable<Location> getLocations();
}
