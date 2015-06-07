package com.kludgenics.cgmlogger.model.glucose;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

import org.jetbrains.annotations.NotNull;

import java.util.Date;

/**
 * Created by matthiasgranberry on 5/24/15.
 */
public class BloodGlucoseRecord extends RealmObject implements BloodGlucose {
    private double value;
    private long date;
    private String type;
    private String unit;
    @PrimaryKey
    private String id;

    public BloodGlucoseRecord() {
        super();
    }

    public BloodGlucoseRecord(String id, double value, long date, String type, String unit) {
        this.id = id;
        this.value = value;
        this.date = date;
        this.type = type;
        this.unit = unit;
    }

    public BloodGlucoseRecord(BloodGlucose glucose) {
        this(glucose.getId(), glucose.getValue(), glucose.getDate(), glucose.getType(), glucose.getUnit());
    }

    @Override
    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public long getDate() {
        return date;
    }

    public void setDate(long date) {
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

