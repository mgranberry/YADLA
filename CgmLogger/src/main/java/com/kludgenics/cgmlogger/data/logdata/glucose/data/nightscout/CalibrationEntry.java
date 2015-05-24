package com.kludgenics.cgmlogger.data.logdata.glucose.data.nightscout;

import com.google.gson.annotations.Expose;
import io.realm.RealmObject;

/**
 * Created by matthiasgranberry on 5/24/15.
 */
public class CalibrationEntry extends RealmObject {
    @Expose
    private String device;
    @Expose
    private long date;
    @Expose
    private int slope;
    @Expose
    private int intercept;
    @Expose
    private int scale;

    public CalibrationEntry() {
        super();
    }

    public CalibrationEntry(String device, long date, int slope, int intercept, int scale) {
        this();
        this.device = device;
        this.date = date;
        this.slope = slope;
        this.intercept = intercept;
        this.scale = scale;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getSlope() {
        return slope;
    }

    public void setSlope(int slope) {
        this.slope = slope;
    }

    public int getIntercept() {
        return intercept;
    }

    public void setIntercept(int intercept) {
        this.intercept = intercept;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }
}
