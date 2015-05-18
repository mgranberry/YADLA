package com.kludgenics.logdata.glucose.data;

/**
 * Created by matthiasgranberry on 5/12/15.
 */
public interface BloodGlucose {
    public double getValue();
    public long getTimestamp();
    public BloodGlucose.Type getType();

    public enum Type {
        CGM,
        SMBG
    }

    public enum Unit {
        MGDL,
        MMOL_L
    }
}
