package com.kludgenics.cgmlogger.data.logdata.glucose.data;

/**
 * Created by matthiasgranberry on 5/12/15.
 */
public interface BloodGlucose {
    double getValue();
    long getTimestamp();
    BloodGlucose.Type getType();
    BloodGlucose.Unit getUnit();

    enum Type {
        CGM,
        SMBG
    }

    enum Unit {
        MGDL,
        MMOL_L
    }
}
