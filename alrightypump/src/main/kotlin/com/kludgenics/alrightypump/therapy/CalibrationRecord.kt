package com.kludgenics.alrightypump.therapy

/**
 * Created by matthias on 11/23/15.
 */

interface CalibrationRecord : Record {
    val slope: Double
    val intercept: Double
    val scale: Double
}