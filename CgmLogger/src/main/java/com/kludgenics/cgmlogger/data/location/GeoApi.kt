package com.kludgenics.cgmlogger.data.location

import com.kludgenics.cgmlogger.data.location.places.AutoCompleteResult
import com.kludgenics.cgmlogger.data.location.data.GeocodedLocation
import com.kludgenics.cgmlogger.data.location.data.Position
import rx.Observable

/**
 * Created by matthiasgranberry on 5/11/15.
 */
public interface GeoApi {
    public fun getCurrentLocation(): Observable<GeocodedLocation>
    public fun getCurrentLocation(categories: String): Observable<GeocodedLocation>
    public fun search(position: Position): Observable<GeocodedLocation>
    public fun search(position: Position, categories: String?): Observable<GeocodedLocation>
    public fun autoComplete(position: Position, query: String?): Observable<AutoCompleteResult>
    public fun autoComplete(position: Position, query: String?, categories: String?): Observable<AutoCompleteResult>
    public fun getInfo(id: String): Observable<GeocodedLocation>
}
