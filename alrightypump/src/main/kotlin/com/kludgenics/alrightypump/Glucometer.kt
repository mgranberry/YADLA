package com.kludgenics.alrightypump

import com.kludgenics.alrightypump.therapy.SmbgRecord
import org.joda.time.Chronology

/**
 * Created by matthias on 11/4/15.
 */

interface Glucometer {
    val smbgRecords: Sequence<SmbgRecord>
    val dateTimeChangeRecords: Sequence<DateTimeChangeRecord>
    val chronology: Chronology
    val outOfRangeHigh: Double
    val outOfRangeLow: Double
}


