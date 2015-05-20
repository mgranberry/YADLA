package com.kludgenics.logdata.location.data;

import android.os.Parcelable;

/**
 * Created by matthiasgranberry on 5/12/15.
 */
public interface Location {
    String getId();
    CharSequence getName();
    String getLocationTypes();
    CharSequence getAddress();
    Position getPosition();
    CharSequence getAttributionSnippet();
}
