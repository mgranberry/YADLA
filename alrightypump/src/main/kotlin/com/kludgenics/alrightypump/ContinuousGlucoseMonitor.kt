package com.kludgenics.alrightypump

/**
 * Created by matthias on 11/4/15.
 */
interface ContinuousGlucoseMonitor : Glucometer {
    val cgmRecords: Sequence<CgmRecord>
}