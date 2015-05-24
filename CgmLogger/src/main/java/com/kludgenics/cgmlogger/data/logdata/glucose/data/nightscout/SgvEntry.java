package com.kludgenics.cgmlogger.data.logdata.glucose.data.nightscout;

import com.google.gson.annotations.Expose;
import com.kludgenics.cgmlogger.data.logdata.glucose.data.BloodGlucose;
import io.realm.RealmObject;
import kotlin.Unit;

import java.util.Date;

/**
 * Created by matthiasgranberry on 5/21/15.
 */
public class SgvEntry implements BloodGlucose {
    @Expose
    private String device;
    @Expose
    private Date date;
    @Expose
    private int sgv;
    @Expose
    private String direction;
    @Expose
    private int filtered;
    @Expose
    private int unfiltered;
    @Expose
    private int rssi;
    @Expose
    private int noise;

    public SgvEntry() {
        super();
    }

    public SgvEntry(String device, Date date, int sgv, String direction, int filtered, int unfiltered, int rssi, int noise) {
        this();
        this.device = device;
        this.date = date;
        this.sgv = sgv;
        this.direction = direction;
        this.filtered = filtered;
        this.unfiltered = unfiltered;
        this.rssi = rssi;
        this.noise = noise;
    }

    @Override
    public String getUnit() {
        return BloodGlucose.UNIT_MGDL;
    }

    @Override
    public double getValue() {
        return sgv;
    }

    @Override
    public String getType() {
        return BloodGlucose.TYPE_CGM;
    }

    public String getDevice() {
        return device;
    }

    @Override
    public Date getDate() {
        return date;
    }

    public int getSgv() {
        return sgv;
    }

    public String getDirection() {
        return direction;
    }

    public int getFiltered() {
        return filtered;
    }

    public int getUnfiltered() {
        return unfiltered;
    }

    public int getRssi() {
        return rssi;
    }

    public int getNoise() {
        return noise;
    }


}
