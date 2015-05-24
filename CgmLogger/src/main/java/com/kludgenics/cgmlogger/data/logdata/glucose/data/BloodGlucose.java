package com.kludgenics.cgmlogger.data.logdata.glucose.data;

import java.util.Date;

/**
 * Created by matthiasgranberry on 5/12/15.
 */
public interface BloodGlucose {
    String UNIT_MGDL = "mg/dl";
    String TYPE_CGM = "cgm";
    String TYPE_SMBG = "smbg";

    double getValue();
    Date getDate();
    String getType();
    String getUnit();
}
