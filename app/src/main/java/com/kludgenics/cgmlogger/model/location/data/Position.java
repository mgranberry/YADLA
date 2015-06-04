package com.kludgenics.cgmlogger.model.location.data;

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
    private float accuracy;
    private Date time;
    @Ignore
    Location location;

    public Location getLocation() {
        location = new Location("database");
        location.setLatitude(getLatitude());
        location.setLongitude(getLongitude());
        location.setAccuracy(getAccuracy());
        location.setTime(getTime().getTime());
        return location;
    }

    public Position() {
        super();
    }

    public Position(Location location) {
        this(location.getLatitude(), location.getLongitude(),  location.getAccuracy(), location.getTime());
    }

    public Position(double latitude, double longitude, float accuracy, long time) {
        this();
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
        this.time = new Date(time);
    }

    public Position(double latitude, double longitude) {
        this(latitude, longitude, 0.0f, 0);
    }

    /**
     * Get the latitude of the position
     * @return latitude WGS-84 decimal
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Set the latitude of the position
     * @param latitude WGS-84 decimal
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * Get the longitude of the position
     * @return longitude WGS-84 decimal
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Set the longitude of the position
     * @param longitude WGS-84 decimal
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * Get the accuracy associated with this position in meters
     * @return accuracy (m)
     */
    public float getAccuracy() {
        return accuracy;
    }

    /**
     * Set the accuracy associated with this position in meters
     * @param accuracy
     */
    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }


}
