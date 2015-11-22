package com.kludgenics.alrightypump

import com.kludgenics.alrightypump.therapy.CgmRecord

/**
 * Created by matthias on 11/4/15.
 */
interface ContinuousGlucoseMonitor : Glucometer {
    val cgmRecords: Sequence<CgmRecord>
}