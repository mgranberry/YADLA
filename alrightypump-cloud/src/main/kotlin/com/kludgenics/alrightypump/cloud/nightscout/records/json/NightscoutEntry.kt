package com.kludgenics.alrightypump.cloud.nightscout.records.json

import org.joda.time.Instant
import org.joda.time.format.ISODateTimeFormat

/**
 * Created by matthiasgranberry on 5/26/15.
 */

public interface NightscoutEntry {
    val id: String?
    val source: String
    val type: String
    val time: Instant
    val dateString: String get() = ISODateTimeFormat.dateTime().print(time)
}