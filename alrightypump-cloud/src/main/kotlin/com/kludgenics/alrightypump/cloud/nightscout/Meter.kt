package com.kludgenics.alrightypump.cloud.nightscout

/**
 * Created by matthias on 11/21/15.
 */
interface Meter : NightscoutEntry {
    val mbg: Int
}