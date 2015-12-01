package com.kludgenics.alrightypump.cloud.nightscout.records.therapy

import com.kludgenics.alrightypump.cloud.nightscout.NightscoutApiBolusTreatment
import com.kludgenics.alrightypump.therapy.BolusRecord
import com.kludgenics.alrightypump.therapy.BolusWizardRecord
import org.joda.time.Duration

/**
 * Created by matthias on 12/1/15.
 */
class NightscoutBolusRecord(override val rawEntry: NightscoutApiBolusTreatment) : BolusRecord, NightscoutRecord {
    override val requestedNormal: Double
        get() = rawEntry.insulin
    override val deliveredNormal: Double?
        get() = rawEntry.insulin
    override val requestedExtended: Double?
        get() = null
    override val deliveredExtended: Double?
        get() = null
    override val extendedDuration: Duration?
        get() = null
    override val expectedExtendedDuration: Duration?
        get() = null
    override val bolusWizard: BolusWizardRecord?
        get() = null
    override val manual: Boolean
        get() = true
}