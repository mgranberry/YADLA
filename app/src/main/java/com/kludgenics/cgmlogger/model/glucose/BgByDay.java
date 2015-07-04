package com.kludgenics.cgmlogger.model.glucose;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by matthiasgranberry on 6/7/15.
 */
public class BgByDay extends RealmObject {
    @PrimaryKey
    private long day;
    private RealmList<BloodGlucoseRecord> bgRecords;

    public BgByDay() {
    }

    public long getDay() {
        return day;
    }

    public void setDay(long day) {
        this.day = day;
    }

    public RealmList<BloodGlucoseRecord> getBgRecords() {
        return bgRecords;
    }

    public void setBgRecords(RealmList<BloodGlucoseRecord> bgRecords) {
        this.bgRecords = bgRecords;
    }

}
