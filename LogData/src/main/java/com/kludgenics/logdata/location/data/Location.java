package com.kludgenics.logdata.location.data;

import android.os.Parcelable;

/**
 * Created by matthiasgranberry on 5/12/15.
 */
public interface Location {
    String getId();
    String getName();
    String getLocationTypes();
    String getAddress();
    Position getPosition();
    String getAttributionSnippet();
}
