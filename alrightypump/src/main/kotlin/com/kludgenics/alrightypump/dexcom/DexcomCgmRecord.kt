package com.kludgenics.alrightypump.dexcom

import com.kludgenics.alrightypump.CgmRecord
import org.joda.time.Instant

/**
 * Created by matthias on 11/19/15.
 */
class DexcomCgmRecord(egvRecord: EgvRecord, sgvRecord: SgvRecord?,
                      calSetRecord: CalSetRecord?) : CgmRecord {
    override val time: Instant
    override val value: DexcomG4GlucoseValue
    override val source: String
        get() = DexcomG4.SOURCE

    init {
        time = egvRecord.displayTime
        value = DexcomG4GlucoseValue(egvRecord, sgvRecord, calSetRecord)
    }

    override fun toString(): String {
        return "${this.javaClass.simpleName}(time=\"$time\", value=$value, source=\"$source\")"
    }
}