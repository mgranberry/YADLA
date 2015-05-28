package com.kludgenics.cgmlogger.data.activity;

import java.util.Date;

/**
 * Created by matthiasgranberry on 5/28/15.
 */
public interface Activity {
    int getActivityId();

    int getConfidence();

    Date getTime();
}
