package com.kludgenics.cgmlogger.model.nightscout;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by matthiasgranberry on 6/3/15.
 */
public class CalibrationEntry extends RealmObject implements NightscoutEntry {
    @Expose
    @SerializedName("_id")
    @PrimaryKey
    private String id;
    @Expose
    private String device;
    @Expose
    private Date date;
    @Expose
    private double slope;
    @Expose
    private long intercept;
    @Expose
    private double scale;

    public CalibrationEntry() {
        super();
    }

    public CalibrationEntry(String id, String device, Date date, double slope, long intercept, double scale) {
        super();
        this.id = id;
        this.device = device;
        this.date = date;
        this.slope = slope;
        this.intercept = intercept;
        this.scale = scale;
    }

    @NotNull
    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @NotNull
    @Override
    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    @NotNull
    @Override
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getSlope() {
        return slope;
    }

    public void setSlope(double slope) {
        this.slope = slope;
    }

    public long getIntercept() {
        return intercept;
    }

    public void setIntercept(long intercept) {
        this.intercept = intercept;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

}
