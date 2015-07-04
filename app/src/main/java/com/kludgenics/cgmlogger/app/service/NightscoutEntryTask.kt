package com.kludgenics.cgmlogger.app.service

import android.content.Context
import android.util.Log
import com.kludgenics.cgmlogger.model.glucose.BgPostprocesser
import com.kludgenics.cgmlogger.model.glucose.BloodGlucoseRecord
import com.kludgenics.cgmlogger.model.nightscout.NightscoutApiEndpoint
import com.kludgenics.cgmlogger.model.nightscout.NightscoutApiEntry
import com.kludgenics.cgmlogger.model.nightscout.NightscoutApiTreatment
import com.kludgenics.cgmlogger.model.nightscout.SgvEntry
import io.realm.Realm
import org.jetbrains.anko.AnkoLogger
import org.joda.time.DateTime
import kotlin.properties.Delegates

/**
 * Created by matthiasgranberry on 6/4/15.
 */

class NightscoutEntryTask(override val ctx: Context,
                          override val nightscoutEndpoint: NightscoutApiEndpoint,
                          val count: Int = 10): NightscoutTask, AnkoLogger {

    override val init: NightscoutApiEndpoint.() -> List<SgvEntry>
        get() = fun (): List<SgvEntry> {
            return getSgvEntries(count)
        }

    override val copy: Realm.(Any) -> Unit
        get() = fun (it: Any) {
            if (it is SgvEntry && it.sgv >= 39) {
                copyToRealmOrUpdate(BloodGlucoseRecord(it))
            }
        }

    override fun postprocess (realm: Realm, l: List<*>) {
        debug("Postprocessing ${l}")
        val fi = l.first() as SgvEntry
        val li = l.last() as SgvEntry
        debug("Beginning daily grouping")
        BgPostprocesser.groupByDay(realm, DateTime(li.getDate()), DateTime(fi.getDate()))
        debug("Finished daily grouping")
    }
}