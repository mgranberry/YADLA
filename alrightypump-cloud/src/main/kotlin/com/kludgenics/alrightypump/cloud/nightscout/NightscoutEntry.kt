package com.kludgenics.alrightypump.cloud.nightscout

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

/**
 * Created by matthiasgranberry on 5/26/15.
 */
public interface NightscoutEntry {
    val id: String
    val device: String
    val type: String
    val date: Long
    val dateString: String get() = ISODateTimeFormat.dateTime().print(date)
}