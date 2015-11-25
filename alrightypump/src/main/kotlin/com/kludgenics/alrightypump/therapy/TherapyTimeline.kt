package com.kludgenics.alrightypump.therapy

import org.joda.time.Instant

/**
 * Created by matthias on 11/22/15.
 */

interface TherapyTimeline {
    val events: Sequence<Record>
    val bolusEvents: Sequence<BolusRecord>
    val glucoseEvents: Sequence<GlucoseRecord>
    val basalEvents: Sequence<BasalRecord>

    fun events(start: Instant, end: Instant = Instant.now()): Collection<Record>
    fun merge(vararg additionalEvents: Sequence<Record>)
    fun merge(predicate: (Record) -> Boolean, vararg additionalEvents: Sequence<Record>)
}