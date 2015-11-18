package com.kludgenics.alrightypump

import org.joda.time.DateTime

/**
 * Created by matthias on 11/7/15.
 */
interface GlucoseRecord {
    val date: DateTime
    val value: GlucoseValue
}

interface DeviceRecord {
    val source: String
}

open class SmbgRecord(override public val date: DateTime,
                      override public val value: GlucoseValue,
                      override public val source: String) : GlucoseRecord, DeviceRecord

open class CgmRecord(override public val date: DateTime,
                     override public val value: GlucoseValue,
                     public val unfiltered: GlucoseValue?,
                     public val filtered: GlucoseValue?,
                     override public val source: String) : GlucoseRecord, DeviceRecord