package com.kludgenics.logdata.location.api;

import com.kludgenics.logdata.location.data.Location;
import com.kludgenics.logdata.location.data.Position;
import rx.Observable;

import java.util.List;

/**
 * Created by matthiasgranberry on 5/11/15.
 */
public interface GeoApi {
    Observable<Location> search(Position position);
    Observable<Location> search(Position position, String categories);
    Observable<AutoCompleteResult> autoComplete(Position position, String query);
    Observable<AutoCompleteResult> autoComplete(Position position, String query, String categories);
    Observable<Location> getInfo(String id);
}
