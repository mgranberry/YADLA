package com.kludgenics.logdata;

/**
 * Created by matthiasgranberry on 5/12/15.
 */
public interface Location {
    String getId();
    String getName();
    String getLocationTypes();
    String getAddress();
    Location getPosition();
}
