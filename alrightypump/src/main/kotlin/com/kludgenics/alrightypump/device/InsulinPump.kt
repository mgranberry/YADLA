package com.kludgenics.alrightypump.device

import com.kludgenics.alrightypump.therapy.BasalRecord
import com.kludgenics.alrightypump.therapy.BolusRecord
import com.kludgenics.alrightypump.therapy.ConsumableRecord
import org.joda.time.Chronology


/**
 * A baseline interface to query an insulin pump.  This interface only provides the ability to
 * identify and verify a connection to a pump along with metadata about the pump.  An actual
 * implementation will implement other interfaces to query the device logs and
 */
interface InsulinPump : Device {
    /**
     * A Joda Time [Chronolgy] constructed from pump time change records, allowing for
     * the translation of raw pump events to standard time.
     */
    override val chronology: Chronology

    /**
     * Opaque list of serial numbers
     */
    override val serialNumber: String

    val bolusRecords: Sequence<BolusRecord>

    val basalRecords: Sequence<BasalRecord>

    val consumableRecords: Sequence<ConsumableRecord>
}
