package com.kludgenics.alrightypump.device

import com.kludgenics.alrightypump.DateTimeChangeRecord
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


