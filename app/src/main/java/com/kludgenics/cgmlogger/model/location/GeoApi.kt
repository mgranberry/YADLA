package com.kludgenics.cgmlogger.model.location

import com.kludgenics.cgmlogger.model.location.places.AutoCompleteResult
import com.kludgenics.cgmlogger.model.location.data.GeocodedLocation
import com.kludgenics.cgmlogger.model.location.Position

/**
 * Created by matthiasgranberry on 5/11/15.
 */
public interface GeoApi {
    public fun getCurrentLocation(): GeocodedLocation?
    public fun getCurrentLocation(categories: String): GeocodedLocation?
    public fun search(position: Position): GeocodedLocation?
    public fun search(position: Position, categories: String?): GeocodedLocation?
    public fun autoComplete(position: Position, query: String?): AutoCompleteResult?
    public fun autoComplete(position: Position, query: String?, categories: String?): AutoCompleteResult?
    public fun getInfo(id: String): GeocodedLocation?
}
