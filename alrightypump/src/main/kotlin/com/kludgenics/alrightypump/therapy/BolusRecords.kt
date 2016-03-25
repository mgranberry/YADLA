package com.kludgenics.alrightypump.therapy

import org.joda.time.Duration

/**
 * Created by matthias on 11/22/15.
 */

interface BolusRecord: Record {
    val requestedNormal: Double?
    val deliveredNormal: Double?

    val requestedExtended: Double?
    val deliveredExtended: Double?
    val extendedDuration: Duration?
    val expectedExtendedDuration: Duration?
    val bolusWizard: BolusWizardRecord?
    val manual: Boolean
}

interface NormalBolusRecord: BolusRecord {
    override val requestedNormal: Double
    override val deliveredNormal: Double?
}

interface ExtendedBolusRecord: BolusRecord {
    override val requestedExtended: Double
    override val expectedExtendedDuration: Duration
}

interface ComboBolusRecord: BolusRecord, NormalBolusRecord, ExtendedBolusRecord {
    override val requestedNormal: Double
    override val requestedExtended: Double
    override val expectedExtendedDuration: Duration
}

interface BolusWizardRecord: Record {
    interface Recommendation {
        val carbBolus: Double
        val correctionBolus: Double
        val totalBolus: Double get() = carbBolus + correctionBolus
    }
    val bg: GlucoseValue
    val carbs: Int
    val insulinOnBoard: Double
    val carbRatio: Double
    val insulinSensitivity: Double
    val target: BloodGlucoseTarget
    val recommendation: Recommendation
}

interface BloodGlucoseTarget {
    val targetLow: GlucoseValue
    val targetHigh: GlucoseValue
}
