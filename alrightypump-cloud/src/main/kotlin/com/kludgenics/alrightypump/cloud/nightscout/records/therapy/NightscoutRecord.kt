package com.kludgenics.alrightypump.cloud.nightscout.records.therapy

import com.kludgenics.alrightypump.cloud.nightscout.records.json.NightscoutEntry
import com.kludgenics.alrightypump.therapy.Record
import org.joda.time.Instant

/**
 * Created by matthias on 12/1/15.
 */
interface NightscoutRecord : Record {
    val rawEntry: NightscoutEntry
    override val id: String? get() = rawEntry.id
    override val time: Instant get() = rawEntry.time
    override val source: String get() = rawEntry.source
}