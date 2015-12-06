package com.kludgenics.alrightypump.therapy

import org.joda.time.Duration

/**
 * Created by matthias on 11/22/15.
 */
interface BasalRecord: Record {
    val rate: Double?
}

interface ScheduledBasalRecord: BasalRecord {
    val schedule: BasalSchedule?
}

interface TemporaryBasalRecord: BasalRecord {
    val percent: Double?
    val duration: Duration
}

interface TermoraryBasalEndRecord: TemporaryBasalRecord {
    override val rate: Double? get() = null
    override val duration: Duration get() = Duration.ZERO
}

interface SuspendedBasalRecord: BasalRecord
interface BasalSchedule