package com.kludgenics.alrightypump.device

import com.kludgenics.alrightypump.therapy.CgmRecord
import com.kludgenics.alrightypump.therapy.ConsumableRecord

/**
 * Created by matthias on 11/4/15.
 */
interface ContinuousGlucoseMonitor : Glucometer {
    val cgmRecords: Sequence<CgmRecord>
    val consumableRecords: Sequence<ConsumableRecord>
}