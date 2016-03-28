package com.kludgenics.alrightypump.cloud.nightscout.records.therapy

import com.kludgenics.alrightypump.cloud.nightscout.NightscoutApiSgvEntry
import com.kludgenics.alrightypump.therapy.Calibration
import com.kludgenics.alrightypump.therapy.GlucoseUnit
import com.kludgenics.alrightypump.therapy.RawGlucoseValue

data class NightscoutGlucoseValue(val sgv: NightscoutApiSgvEntry) : RawGlucoseValue {
    override val calibration: Calibration?
        get() = null
    override val filtered: Int?
        get() = sgv.filtered
    override val unfiltered: Int?
        get() = sgv.unfiltered
    override val glucose: Double?
        get() = sgv.sgv.toDouble()
    override val unit: Int
        get() = GlucoseUnit.MGDL
}