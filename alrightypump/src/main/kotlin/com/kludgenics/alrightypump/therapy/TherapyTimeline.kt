package com.kludgenics.alrightypump.therapy

import org.joda.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListMap

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


abstract class AbstractMapTherapyTimeline: TherapyTimeline {
    protected abstract val _events: NavigableMap<Instant, MutableSet<Record>>
    protected abstract val _eventSet: () -> MutableSet<Record>
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
        val events = additionalEvents.flatMap { it.takeWhile(predicate).asIterable() }
        for (event in events)
            _events.getOrPut(event.time, _eventSet ).add(event)
    }
}

open class NavigableMapTherapyTimeline : AbstractMapTherapyTimeline() {
    protected override val _events: NavigableMap<Instant, MutableSet<Record>> = TreeMap()
    protected override val _eventSet: () -> MutableSet<Record> = { HashSet(1) }
}

class ConcurrentSkipListTherapyTimeline : AbstractMapTherapyTimeline() {
    protected override val _events: NavigableMap<Instant, MutableSet<Record>> = ConcurrentSkipListMap()
    protected override val _eventSet = { Collections.newSetFromMap(ConcurrentHashMap<Record, Boolean>())}
}