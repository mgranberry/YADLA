package com.kludgenics.cgmlogger.data.logdata.glucose.data.nightscout;

import com.google.gson.annotations.Expose;

import java.util.Date;

/**
 * Created by matthiasgranberry on 5/24/15.
 */
public class NightscoutApiEntry {
    @Expose
    private int slope;
    @Expose
    private int intercept;
    @Expose
    private int scale;
    @Expose
    private int mbg;
    @Expose
    private String device;
    @Expose
    private long date;
    @Expose
    private int sgv;
    @Expose
    private String direction;
    @Expose
    private String type;
    @Expose
    private int filtered;
    @Expose
    private int unfiltered;
    @Expose
    private int rssi;
    @Expose
    private int noise;

    public Type getType() {
        if (type.equals("mbg"))
            return Type.MBG;
        if (type.equals("sgv"))
            return Type.SGV;
        if (type.equals("cal"))
            return Type.CAL;
        return Type.UNKNOWN;
    }

    public CalibrationEntry asCalibration() {
        return new CalibrationEntry(device, date, slope, intercept, scale);
    }

    public MbgEntry asMbg() {
        return new MbgEntry(device, new Date(date), mbg);
    }

    public SgvEntry asSgv() {
        return new SgvEntry(device, new Date(date), sgv, direction, filtered, unfiltered, rssi, noise);
    }

    enum Type {
        SGV,
        MBG,
        CAL,
        UNKNOWN
    }
}
