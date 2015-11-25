package com.kludgenics.alrightypump.therapy

import org.joda.time.Instant
import java.util.*

/**
 * Created by matthias on 11/22/15.
 */
class TreeMapTherapyTimeline : TherapyTimeline {
    val _events: TreeMap<Instant, HashSet<Record>> = TreeMap()
    override val events: Sequence<Record> get() = _events.values.flatMap { it }.asSequence()
    override val bolusEvents: Sequence<BolusRecord> get() = events.filterIsInstance<BolusRecord>()
    override val glucoseEvents: Sequence<GlucoseRecord> get() = events.filterIsInstance<GlucoseRecord>()
    override val basalEvents: Sequence<BasalRecord> get() = events.filterIsInstance<BasalRecord>()

    override fun events(start: Instant, end: Instant): List<Record> =
            _events.tailMap(start).headMap(end).values.flatMap { it }


    override fun merge(vararg additionalEvents: Sequence<Record>) {
        merge({true}, *additionalEvents)
    }

    override fun merge(predicate: (Record) -> Boolean, vararg additionalEvents: Sequence<Record>) {
        val e = additionalEvents.map { it }
        val events = additionalEvents.flatMap { it.takeWhile(predicate).toList() }
        for (event in events)
            _events.getOrPut(event.time, { HashSet(1) }).add(event)
    }
}