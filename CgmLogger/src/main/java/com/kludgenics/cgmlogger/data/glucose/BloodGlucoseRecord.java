package com.kludgenics.cgmlogger.data.glucose;

import io.realm.RealmObject;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

/**
 * Created by matthiasgranberry on 5/24/15.
 */
public class BloodGlucoseRecord extends RealmObject implements BloodGlucose {
    private double value;
    private Date date;
    private String type;
    private String unit;
    private String id;

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

    @NotNull
    @Override
    public Date getDate() {
        return date;
    }

    public void setDate(@NotNull Date date) {
        this.date = date;
    }

    @NotNull
    @Override
    public String getType() {
        return type;
    }

    public void setType(@NotNull String type) {
        this.type = type;
    }

    @NotNull
    @Override
    public String getUnit() {
        return unit;
    }

    public void setUnit(@NotNull String unit) {
        this.unit = unit;
    }

    @NotNull
    @Override
    public String getId() {
        return id;
    }

    public void setId(@NotNull String id) {
        this.id = id;
    }

}

