package com.kludgenics.logdata.location.data;

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
