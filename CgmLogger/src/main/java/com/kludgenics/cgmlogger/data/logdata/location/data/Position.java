package com.kludgenics.cgmlogger.data.logdata.location.data;

import android.location.Location;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;

import java.util.Date;

/**
 * Created by matthiasgranberry on 5/11/15.
 */
public class Position extends RealmObject {
    private double latitude;
    private double longitude;
    private float radius;
    private Date time;
    @Ignore
    Location location;

    public Location getLocation() {
        location = new Location("database");
        location.setAccuracy(getRadius());
        return location;
    }

    public Position() {
        super();
    }

    public Position(Location location) {
        this(location.getLatitude(), location.getLongitude(),  location.getAccuracy(), location.getTime());
    }

    public Position(double latitude, double longitude, float radius, long time) {
        this();
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.time = new Date(time);
    }

    public Position(double latitude, double longitude) {
        this(latitude, longitude, 0.0f, 0);
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
    public float getRadius() {
        return radius;
    }

    /**
     * Set the radius assoziated with this position in meters
     * @param radius
     */
    public void setRadius(float radius) {
        this.radius = radius;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }


}
