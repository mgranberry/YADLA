package com.kludgenics.cgmlogger.model.nightscout

import java.util.Date

/**
 * Created by matthiasgranberry on 5/26/15.
 */
public interface NightscoutEntry {
    public fun getId(): String

    public fun getDevice(): String

    public fun getDate(): Long
}