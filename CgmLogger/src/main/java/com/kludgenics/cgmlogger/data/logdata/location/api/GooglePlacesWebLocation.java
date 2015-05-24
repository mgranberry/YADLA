package com.kludgenics.cgmlogger.data.logdata.location.api;

import com.google.gson.annotations.Expose;
import com.kludgenics.cgmlogger.data.logdata.location.data.GeocodedLocation;
import com.kludgenics.cgmlogger.data.logdata.location.data.Position;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Created by matthiasgranberry on 5/23/15.
 */
public class GooglePlacesWebLocation implements GeocodedLocation {
    @Expose
    String place_id;
    @Expose
    List<String> types;
    @Expose
    String name;
    @Expose
    Geometry geometry;

    class Geometry {
        @Expose
        GeometryLocation location;
        Position position;

        Position getPosition() {
            if (position == null) {
                position = new Position(location.lat, location.lng);
            }
            return position;
        }

        class GeometryLocation {
            @Expose
            float lat;
            @Expose
            float lng;
        }
    }
    @Override
    public String getId() {
        return place_id;
    }

    @Override
    public CharSequence getName() {
        return name;
    }

    @Override
    public String getLocationTypes() {
        return null;
    }

    @Override
    public CharSequence getAddress() {
        return StringUtils.join(types, '|');
    }

    @Override
    public Position getPosition() {
        return geometry.getPosition();
    }

    @Override
    public CharSequence getAttributionSnippet() {
        return null;
    }
}
