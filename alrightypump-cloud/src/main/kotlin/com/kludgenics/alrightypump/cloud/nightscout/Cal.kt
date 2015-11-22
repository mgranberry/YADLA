package com.kludgenics.alrightypump.cloud.nightscout

/**
 * Created by matthias on 11/21/15.
 */
interface Cal: NightscoutEntry {
    override val type: String get() = "cal"
    val slope: Double
    val intercept: Double
    val scale: Double
}