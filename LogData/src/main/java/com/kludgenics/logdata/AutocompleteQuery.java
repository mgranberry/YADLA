package com.kludgenics.logdata;

import java.util.List;

/**
 * Created by matthiasgranberry on 5/11/15.
 */
public interface AutocompleteQuery {
    double getLatutude();
    double getLongitude();
    String getQueryText();
    String getLocationTypes();
}
