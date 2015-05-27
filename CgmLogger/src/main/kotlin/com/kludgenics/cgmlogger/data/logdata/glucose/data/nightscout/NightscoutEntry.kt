package com.kludgenics.cgmlogger.data.logdata.glucose.data.nightscout

import java.util.Date

/**
 * Created by matthiasgranberry on 5/26/15.
 */
public trait NightscoutEntry {
    public fun getId(): String

    public fun getDevice(): String

    public fun getDate(): Date
}