package com.kludgenics.logdata;

import java.util.List;

/**
 * Created by matthiasgranberry on 5/11/15.
 */
public interface GeoApi {
    void search(Position position);
    void search(Position position, String categories);
    List<AutoCompleteResult> autoComplete(Position position, String query);
    List<AutoCompleteResult> autoComplete(Position position, String query, String categories);
    void getInfo(String id);
}
