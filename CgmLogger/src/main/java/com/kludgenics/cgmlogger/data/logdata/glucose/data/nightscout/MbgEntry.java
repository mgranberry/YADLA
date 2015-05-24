package com.kludgenics.cgmlogger.data.logdata.glucose.data.nightscout;

import com.google.gson.annotations.Expose;
import com.kludgenics.cgmlogger.data.logdata.glucose.data.BloodGlucose;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import kotlin.Unit;

import java.util.Date;

/**
 * Created by matthiasgranberry on 5/24/15.
 */
public class MbgEntry implements BloodGlucose {
    @Expose
    private String device;
    @Expose
    private int mbg;
    @Expose
    private Date date;

    public MbgEntry() {
        super();
    }

    public MbgEntry(String device, Date date, int mbg) {
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
    public Date getDate() {
        return date;
    }

    @Override
    public String getType() {
        return BloodGlucose.TYPE_SMBG;
    }

    @Override
    public String getUnit() {
        return BloodGlucose.UNIT_MGDL;
    }
}
