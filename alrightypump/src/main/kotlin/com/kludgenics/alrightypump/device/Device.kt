package com.kludgenics.alrightypump.device

import org.joda.time.Chronology

/**
 * Created by matthias on 12/13/15.
 */
interface Device {
    val chronology: Chronology
    val serialNumber: String
}