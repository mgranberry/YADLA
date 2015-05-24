package com.kludgenics.cgmlogger.data.logdata.glucose.data.nightscout;

import com.google.gson.annotations.Expose;
import com.kludgenics.cgmlogger.data.logdata.glucose.data.BloodGlucose;
import io.realm.RealmObject;

/**
 * Created by matthiasgranberry on 5/24/15.
 */
public class MbgEntry extends RealmObject implements BloodGlucose {
    @Expose
    private String device;
    @Expose
    private int mbg;
    @Expose
    private long date;

    public MbgEntry() {
        super();
    }

    public MbgEntry(String device, long date, int mbg) {
        this();
        this.device = device;
        this.mbg = mbg;
        this.date = date;
    }

    @Override
    public double getValue() {
        return mbg;
    }

    @Override
    public long getTimestamp() {
        return date;
    }

    @Override
    public Type getType() {
        return Type.SMBG;
    }

    @Override
    public Unit getUnit() {
        return Unit.MGDL;
    }
}
