package com.kludgenics.cgmlogger.data.logdata.location.data;

import android.location.Location;
import android.support.annotation.Nullable;

/**
 * Created by matthiasgranberry on 5/11/15.
 */
public class Position {
    private double latitude;
    private double longitude;
    private Double radius;

    public Position(Location location) {
        this(location.getLatitude(), location.getLongitude(), (double) location.getAccuracy());
    }

    public Position(double latitude, double longitude, @Nullable Double radius) {
        this.latitude = latitude;
        this.longitude = longitude;
        if (radius != null && radius != 0.0)
            this.radius = radius;
    }

    public Position(double latitude, double longitude) {
        this(latitude, longitude, null);
    }

    /**
     * Get the latitude of the position
     * @return latitude
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Set the latitude of the position
     * @param latitude
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * Get the longitude of the position
     * @return longitude
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Set the longitude of the position
     * @param longitude
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * Get the radius associated with this position in meters
     * @return radius (m)
     */
    @Nullable
    public Double getRadius() {
        return radius;
    }

    /**
     * Set the radius assoziated with this position in meters
     * @param radius
     */
    public void setRadius(@Nullable Double radius) {
        this.radius = radius;
    }
}
