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
import com.kludgenics.cgmlogger.extension.*
import org.joda.time.Period

/**
 * Created by matthiasgranberry on 6/4/15.
 */

class NightscoutEntryTask(override val ctx: Context,
                          override val nightscoutEndpoint: NightscoutApiEndpoint,
                          val count: Int = 0): NightscoutTask, AnkoLogger {

    override val init: NightscoutApiEndpoint.() -> List<SgvEntry>
        get() = fun (): List<SgvEntry> {
            val requestCount = if (count == 0) {
                val realm = Realm.getDefaultInstance()
                val latestTime = DateTime(realm.use {
                    realm.where<BloodGlucoseRecord> {
                        this
                    }.findAllSorted("date", false).first().date
                })
                val difference = Period(latestTime, DateTime())
                1 + difference.getMinutes() / 5
            } else count
            return getSgvEntries(requestCount)
        }

    override val copy: Realm.(Any) -> Unit
        get() = fun (it: Any) {
            if (it is SgvEntry && it.sgv >= 39) {
                copyToRealmOrUpdate(BloodGlucoseRecord(value=it.getValue(), date=it.getDate(),
                        type=it.getType(), unit=it.getUnit(), id=it.getId()))
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