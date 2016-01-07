package com.kludgenics.alrightypump.therapy

import org.joda.time.Duration
import org.joda.time.LocalTime
import java.util.*

/**
 * Created by matthias on 12/27/15.
 */

interface ProfileRecord : Record {
    val default: String
    val profiles: Map<String, Profile>
}

interface Profile {
    val name: String
    val dia: Duration
    val carbsPerHour: Double?
    val carbAbsorptionDelay: Double?
    val carbFactors: SortedMap<LocalTime, Double>
    val correctionFactors: SortedMap<LocalTime, Double>
    val basalRates: SortedMap<LocalTime, Double>
    val targets: SortedMap<LocalTime, BloodGlucoseTarget>
    val units: Int
}