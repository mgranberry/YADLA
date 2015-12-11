package com.kludgenics.alrightypump.therapy

/**
 * Created by matthias on 11/7/15.
 */
interface GlucoseRecord: Record {
    val value: GlucoseValue
    val manual: Boolean
}

interface SmbgRecord : GlucoseRecord

interface CgmRecord : GlucoseRecord {
    override val manual: Boolean
        get() = false
}

interface RawCgmRecord: CgmRecord {
    override val value: RawGlucoseValue
}