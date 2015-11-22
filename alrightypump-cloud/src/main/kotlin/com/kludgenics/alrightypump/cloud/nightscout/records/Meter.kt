package com.kludgenics.alrightypump.cloud.nightscout.records

/**
 * Created by matthias on 11/21/15.
 */
interface Meter : NightscoutEntry {
    val mbg: Int
}