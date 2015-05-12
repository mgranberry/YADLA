package com.kludgenics.glucosegrapher;

import java.util.Date;

/**
 * Created by matthiasgranberry on 5/9/15.
 */
public interface BgRecord {
    double getValue();
    Date getTimestamp();
}
