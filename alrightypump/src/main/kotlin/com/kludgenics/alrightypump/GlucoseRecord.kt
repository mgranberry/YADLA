package com.kludgenics.alrightypump

import org.joda.time.Instant

/**
 * Created by matthias on 11/7/15.
 */
interface GlucoseRecord {
    val time: Instant
    val value: GlucoseValue
    val manual: Boolean
}

interface DeviceRecord {
    val source: String
}

interface SmbgRecord : GlucoseRecord, DeviceRecord

interface CgmRecord : GlucoseRecord, DeviceRecord {
    override val manual: Boolean
        get() = false
}