package com.kludgenics.alrightypump.cloud.nightscout.records.json

import org.joda.time.LocalDateTime
import org.joda.time.format.ISODateTimeFormat

/**
 * Created by matthiasgranberry on 5/26/15.
 */

interface NightscoutEntry {
    val id: String?
    val source: String
    val type: String
    val time: LocalDateTime
    val dateString: String get() = ISODateTimeFormat.dateTime().print(time)
}