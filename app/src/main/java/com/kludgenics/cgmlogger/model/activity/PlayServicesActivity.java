package com.kludgenics.cgmlogger.model.activity;

import java.util.Date;

import io.realm.RealmObject;

/**
 * Created by matthiasgranberry on 5/15/15.
 */
public class PlayServicesActivity extends RealmObject implements Activity {
    private int activityId;
    private int confidence;
    private Date time;

    @Override
    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    @Override
    public int getConfidence() {
        return confidence;
    }

    public void setConfidence(int confidence) {
        this.confidence = confidence;
    }

    @Override
    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
