package com.kludgenics.alrightypump.device.tandem

import com.kludgenics.alrightypump.therapy.*
import org.joda.time.Duration
import org.joda.time.Instant
import org.joda.time.LocalTime
import java.util.*

/**
 * Created by matthias on 1/8/16.
 */

data class TandemProfile(val idp: Int,
                         override val name: String,
                         override val dia: Duration? = null,
                         override val basalRates: SortedMap<LocalTime, Double>? = null,
                         override val carbFactors: SortedMap<LocalTime, Double>? = null,
                         override val correctionFactors: SortedMap<LocalTime, Double>? = null,
                         override val targets: SortedMap<LocalTime, BloodGlucoseTarget>? = null,
                         override val carbAbsorptionDelay: Duration? = null,
                         override val carbsPerHour: Double? = null,
                         override val units: Int = GlucoseUnit.MGDL) : Profile {
    constructor (idpResp: IdpResp) : this(idp = idpResp.idp,
            name = idpResp.name,
            dia = idpResp.bolusSettings.insulinDuration!!,
            basalRates = TreeMap(idpResp.timeDependentSettings.map { it.startTime to it.basalRate!! }.toMap()),
            carbFactors = TreeMap(idpResp.timeDependentSettings.map { it.startTime to it.carbRatio!! }.toMap()),
            correctionFactors = TreeMap(idpResp.timeDependentSettings.map { it.startTime to it.isf?.toDouble() }.toMap()),
            targets = TreeMap(idpResp.timeDependentSettings.map { it.startTime to TandemBloodGlucoseTarget(it.targetBg!!) }.toMap()))
}

data class TandemProfileRecord(override val default: String,
                          override val profiles: Map<String, Profile>,
                          val baseRecord: TandemTherapyRecord): ProfileRecord, TandemTherapyRecord by baseRecord