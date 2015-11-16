package com.kludgenics.alrightypump

import org.joda.time.Chronology

/**
 * Created by matthias on 11/4/15.
 */

interface Glucometer {
    val smbgRecords: List<SmbgRecord>
    val dateTimeChangeRecords: List<DateTimeChangeRecord>
    val chronology: Chronology
    val outOfRangeHigh: Double
    val outOfRangeLow: Double

}


