package com.kludgenics.alrightypump.therapy

import org.joda.time.Duration

/**
 * Created by matthias on 11/22/15.
 */

interface BolusRecord: Record {
    val requestedNormal: Double
    val requestedExtended: Double
    val extendedDuration: Duration
    val delivered: Double
    val manual: Boolean
}

interface BolusWizardRecord: Record {
    interface Recommendation {
        val carbBolus: Double
        val correctionBolus: Double
        val total: Double get() = carbBolus + correctionBolus
    }
    val bg: GlucoseValue
    val carbs: Int
    val insulinOnBoard: Double
    val carbRatio: Double
    val insulinSensitivity: Double
    val target: BloodGlucoseTarget
}

interface BloodGlucoseTarget {
    val targetLow: GlucoseValue
    val targetHigh: GlucoseValue
    fun targetFor(value: GlucoseValue): GlucoseValue
}
