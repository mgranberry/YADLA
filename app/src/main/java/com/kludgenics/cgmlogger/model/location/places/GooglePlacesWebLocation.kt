package com.kludgenics.cgmlogger.model.location.places

import com.google.gson.annotations.Expose
import com.kludgenics.cgmlogger.model.location.data.GeocodedLocation
import com.kludgenics.cgmlogger.model.location.Position
import java.util.Collections

/**
 * Created by matthiasgranberry on 5/23/15.
 */
public class GooglePlacesWebLocation : GeocodedLocation {
    @Expose
    var place_id: String = ""
    @Expose
    var types: List<String> = Collections.emptyList()
    @Expose
    var name: String = ""
    @Expose
    var geometry: Geometry = Geometry()

    inner class Geometry {
        @Expose
        var location: GeometryLocation = GeometryLocation()

        fun getPosition(): Position {
            return Position(location.lat.toDouble(), location.lng.toDouble())
        }

        inner class GeometryLocation {
            @Expose
            var lat: Float = 0.toFloat()
            @Expose
            var lng: Float = 0.toFloat()
        }
    }

    override fun getId(): String {
        return place_id
    }

    override fun getName(): CharSequence {
        return name
    }

    override fun getLocationTypes(): String {
        return types.joinToString("|")
    }

    override fun getAddress(): CharSequence {
        return ""
    }

    override fun getPosition(): Position {
        return geometry.getPosition()
    }

    override fun getAttributionSnippet(): CharSequence? {
        return null
    }
}