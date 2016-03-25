package com.kludgenics.alrightypump.device.tandem

import com.kludgenics.alrightypump.therapy.*
import org.joda.time.DateTimeZone
import org.joda.time.Duration

/**
 * Created by matthias on 11/24/15.
 */

data class TandemBloodGlucoseTarget(val target: GlucoseValue) : BloodGlucoseTarget {
    override val targetLow: GlucoseValue
        get() = target
    override val targetHigh: GlucoseValue
        get() = target
}

data class TandemBolusWizard(val event1: BolusRequest1,
                             val event2: BolusRequest2,
                             val event3: BolusRequest3) : BolusWizardRecord, TandemTherapyRecord, LogEvent by event1 {
    data class TandemBolusRecommendation(override val carbBolus: Double,
                                         override val correctionBolus: Double,
                                         override val totalBolus: Double) : BolusWizardRecord.Recommendation

    override val bg: GlucoseValue
        get() = event1.bg
    override val carbs: Int
        get() = event1.carbs
    override val insulinOnBoard: Double
        get() = event1.iob.toDouble()
    override val carbRatio: Double
        get() = event1.carbRatio
    override val insulinSensitivity: Double
        get() = event2.isf.toDouble()
    override val target: BloodGlucoseTarget
        get() = TandemBloodGlucoseTarget(event2.targetBg)
    override val recommendation: BolusWizardRecord.Recommendation
        get() = TandemBolusRecommendation(carbBolus = event3.foodBolusRecommendation.toDouble(),
                correctionBolus = event3.correctionBolusRecommendation.toDouble(),
                totalBolus = event3.totalBolus.toDouble())
}

interface TandemBolus : BolusRecord {
    override val manual: Boolean get() = false
}

data class TandemNormalBolus(public val bolusActivated: BolusActivated,
                             public val bolusCompleted: BolusCompleted?,
                             public override val bolusWizard: TandemBolusWizard) : NormalBolusRecord,
        TandemBolus,
        TandemTherapyRecord,
        LogEvent by bolusWizard {

    override val requestedNormal: Double
        get() = bolusActivated.bolusSize.toDouble()

    override val deliveredNormal: Double
        get() = bolusCompleted?.insulinDelivered?.toDouble() ?: requestedNormal

    override val deliveredExtended: Double?
        get() = null

    override val requestedExtended: Double?
        get() = null

    override val extendedDuration: Duration?
        get() = null

    override val expectedExtendedDuration: Duration?
        get() = null
}

data class TandemExtendedBolus(public val extendedActivated: BolexActivated,
                               public val bolexCompleted: BolexCompleted?,
                               public override val bolusWizard: TandemBolusWizard) : ExtendedBolusRecord,
        TandemBolus,
        TandemTherapyRecord,
        LogEvent by bolusWizard {

    override val requestedNormal: Double?
        get() = null
    override val deliveredNormal: Double?
        get() = null

    override val requestedExtended: Double
        get() = extendedActivated.bolusSize.toDouble()

    override val deliveredExtended: Double
        get() = bolexCompleted?.insulinDelivered?.toDouble() ?: requestedExtended

    override val extendedDuration: Duration?
        get() = if (bolexCompleted != null)
            Duration(extendedActivated.timestamp.toDateTime(DateTimeZone.UTC), bolexCompleted.timestamp.toDateTime(DateTimeZone.UTC))
        else
            null

    override val expectedExtendedDuration: Duration
        get() = bolusWizard.event2.duration

}

data class TandemComboBolus(public val bolusActivated: BolusActivated,
                            public val extendedActivated: BolexActivated,
                            public val bolusCompleted: BolusCompleted?,
                            public val bolexCompleted: BolexCompleted?,
                            public override val bolusWizard: TandemBolusWizard) : ComboBolusRecord,
        TandemBolus,
        TandemTherapyRecord,
        LogEvent by bolusWizard {
    override val requestedNormal: Double
        get() = bolusActivated.bolusSize.toDouble()

    override val deliveredNormal: Double
        get() = bolusCompleted?.insulinDelivered?.toDouble() ?: requestedNormal

    override val requestedExtended: Double
        get() = extendedActivated.bolusSize.toDouble()

    override val deliveredExtended: Double
        get() = bolexCompleted?.insulinDelivered?.toDouble() ?: requestedExtended

    override val extendedDuration: Duration?
        get() = if (bolexCompleted != null)
            Duration(extendedActivated.timestamp.toDateTime(DateTimeZone.UTC), bolexCompleted.timestamp.toDateTime(DateTimeZone.UTC))
        else
            null

    override val expectedExtendedDuration: Duration
        get() = bolusWizard.event2.duration
}