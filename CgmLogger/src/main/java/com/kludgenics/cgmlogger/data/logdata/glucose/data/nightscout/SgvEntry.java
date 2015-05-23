package com.kludgenics.cgmlogger.data.logdata.glucose.data.nightscout;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;
import com.kludgenics.cgmlogger.data.logdata.glucose.data.BloodGlucose;

/**
 * Created by matthiasgranberry on 5/21/15.
 */
@Table(name="Entries")
public class SgvEntry extends Model implements BloodGlucose {
    @Column(name="device")
    @Expose
    String device;
    @Column(name="date")
    @Expose
    long date;
    @Column(name="sgv")
    @Expose
    int sgv;
    @Column(name="direction")
    @Expose
    String direction;
    @Column(name="type")
    @Expose
    String type;
    @Column(name="filtered")
    @Expose
    int filtered;
    @Column(name="unfiltered")
    @Expose
    int unfiltered;
    @Column(name="rssi")
    @Expose
    int rssi;
    @Column(name="noise")
    @Expose
    int noise;

    public SgvEntry() {
        super();
    }

    @Override
    public Unit getUnit() {
        return Unit.MGDL;
    }

    @Override
    public double getValue() {
        return sgv;
    }

    @Override
    public long getTimestamp() {
        return date;
    }

    @Override
    public Type getType() {
        return Type.CGM;
    }
}
