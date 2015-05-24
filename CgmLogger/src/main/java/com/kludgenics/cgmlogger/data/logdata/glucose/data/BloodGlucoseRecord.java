package com.kludgenics.cgmlogger.data.logdata.glucose.data;

import io.realm.RealmObject;

import java.util.Date;

/**
 * Created by matthiasgranberry on 5/24/15.
 */
public class BloodGlucoseRecord extends RealmObject implements BloodGlucose {
    private double value;
    private Date date;
    private String type;
    private String unit;

    public BloodGlucoseRecord() {
        super();
    }

    public BloodGlucoseRecord(double value, Date date, String type, String unit) {
        this.value = value;
        this.date = date;
        this.type = type;
        this.unit = unit;
    }

    public BloodGlucoseRecord(BloodGlucose glucose) {
        this(glucose.getValue(), glucose.getDate(), glucose.getType(), glucose.getUnit());

    }

    @Override
    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
