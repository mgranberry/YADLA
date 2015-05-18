package com.kludgenics.logdata.activity.data;

/**
 * Created by matthiasgranberry on 5/16/15.
 */
public interface Activity {
    enum Type {
        DRIVING,
        CYCLING,
        RUNNING,
        WALKING,
        WALKING_OR_RUNNING,
        STILL,
        ORIENTATION_CHANGE,
        UNKNOWN,
        EATING,
        SLEEPING
    }

    Activity.Type getMostLikelyActivity();

}
