package com.kludgenics.cgmlogger.model.nightscout

/**
 * Created by matthiasgranberry on 5/26/15.
 */
public interface NightscoutEntry {
    public fun getId(): String

    public fun getDevice(): String

    public fun getDate(): Long
}