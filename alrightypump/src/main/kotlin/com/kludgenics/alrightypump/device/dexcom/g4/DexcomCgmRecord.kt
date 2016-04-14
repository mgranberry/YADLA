package com.kludgenics.alrightypump.device.dexcom.g4

import com.kludgenics.alrightypump.therapy.RawCgmRecord
import org.joda.time.LocalDateTime

/**
 * Created by matthias on 11/19/15.
 */
data class DexcomCgmRecord(override val id: String,
                           override val time: LocalDateTime,
                           override val value: DexcomG4GlucoseValue,
                           override val source: String = DexcomG4.source,
                           val egvRecord: EgvRecord,
                           val sgvRecord: SgvRecord?,
                           val calSetRecord: CalSetRecord?) : RawCgmRecord {
    constructor(egvRecord: EgvRecord, sgvRecord: SgvRecord?,
                calSetRecord: CalSetRecord?): this(egvRecord.id, egvRecord.displayTime, DexcomG4GlucoseValue(egvRecord,
            sgvRecord, calSetRecord), egvRecord = egvRecord, sgvRecord = sgvRecord, calSetRecord = calSetRecord)

    override val trendArrow: Int
        get() = egvRecord.trendArrow

    override val noise: Int
        get() = egvRecord.noise

    override val rssi: Int?
        get() = sgvRecord?.rssi
}