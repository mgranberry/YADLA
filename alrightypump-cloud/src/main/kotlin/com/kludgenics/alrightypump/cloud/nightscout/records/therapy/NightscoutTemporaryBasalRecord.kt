package com.kludgenics.alrightypump.cloud.nightscout.records.therapy

import com.kludgenics.alrightypump.cloud.nightscout.NightscoutApiTempBasalTreatment
import com.kludgenics.alrightypump.therapy.TemporaryBasalRecord
import org.joda.time.Duration

/**
 * Created by matthias on 12/1/15.
 */
class NightscoutTemporaryBasalRecord(override val rawEntry: NightscoutApiTempBasalTreatment) : NightscoutRecord, TemporaryBasalRecord {
    override val percent: Double?
        get() = rawEntry.percent?.plus(100.0)
    override val duration: Duration
        get() = Duration(rawEntry.duration * 60000)
    override val rate: Double?
        get() = rawEntry.rate
}