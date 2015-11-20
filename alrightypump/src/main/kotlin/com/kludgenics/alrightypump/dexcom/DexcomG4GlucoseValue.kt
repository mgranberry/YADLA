package com.kludgenics.alrightypump.dexcom

import com.kludgenics.alrightypump.GlucoseUnit
import com.kludgenics.alrightypump.RawGlucoseValue

/**
 * Created by matthias on 11/19/15.
 */
public class DexcomG4GlucoseValue(val egvRecord: EgvRecord,
                                  val sgvRecord: SgvRecord?,
                                  override public var calibration: CalSetRecord?) : RawGlucoseValue {
    override val filtered: Int?
        get() = sgvRecord?.filtered
    override val unfiltered: Int?
        get() = sgvRecord?.unfiltered
    override val glucose: Double
        get() = egvRecord.glucose.toDouble()
    override val unit: Int
        get() = GlucoseUnit.MGDL

    override fun toString(): String {
        return "${this.javaClass.simpleName}(glucose=$glucose, unit=$unit, raw=$raw, calibration=$calibration, egv=$egvRecord, sgv=$sgvRecord)"
    }
}