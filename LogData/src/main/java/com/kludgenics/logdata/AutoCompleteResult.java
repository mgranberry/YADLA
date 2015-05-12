package com.kludgenics.logdata;

import java.util.List;

/**
 * Created by matthiasgranberry on 5/11/15.
 */
public interface AutoCompleteResult {
    List<Integer> getCategories();
    CharSequence getId();
    CharSequence getName();
}
