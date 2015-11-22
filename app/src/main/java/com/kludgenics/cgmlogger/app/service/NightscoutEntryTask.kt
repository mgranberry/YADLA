package com.kludgenics.cgmlogger.app.service

import android.content.Context
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import com.kludgenics.cgmlogger.extension.where
import com.kludgenics.cgmlogger.model.glucose.BgPostprocessor
import com.kludgenics.cgmlogger.model.realm.glucose.BloodGlucoseRecord
import com.kludgenics.cgmlogger.model.nightscout.NightscoutApiEndpoint
import com.kludgenics.cgmlogger.model.nightscout.RealmSgvEntry
import io.realm.Realm
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.info
import org.joda.time.DateTime
import org.joda.time.Period

/**
 * Created by matthiasgranberry on 6/4/15.
 */

class NightscoutEntryTask(override val ctx: Context,
                          override val nightscoutEndpoint: NightscoutApiEndpoint,
                          val count: Int = 0): NightscoutTask, AnkoLogger {

    override val init: NightscoutApiEndpoint.() -> List<RealmSgvEntry>
        get() = fun (): List<RealmSgvEntry> {
            val requestCount = if (count == 0) {
                val realm = Realm.getDefaultInstance()
                val latestTime = DateTime(realm.use {
                    realm.where<BloodGlucoseRecord> {
                        this
                    }.findAllSorted("date", false).first().date
                })
                val difference = Period(latestTime, DateTime())
                if (difference.minutes < 5) 0 else 1 + difference.minutes / 5
            } else count
            val entries = if (requestCount > 0) {
                info("getting $requestCount entries")
                var e: List<RealmSgvEntry> = emptyList()
                try {
                     val f = getSgvEntries(requestCount, "2010").execute()
                     info(f)
                     e = f.body()
                } catch (e: Exception) {
                    info(e)
                    e.printStackTrace()
                }
                    info("got")
                e
            } else {
                info("already current.  skipping update.")
                emptyList()
            }
            Answers.getInstance().logCustom(CustomEvent("Nightscout Entry Sync")
                    .putCustomAttribute("entry_count", entries.size))
            return entries
        }

    override val copy: Realm.(Any) -> Unit
        get() = fun (it: Any) {
            if (it is RealmSgvEntry && it.sgv >= 39) {
                copyToRealmOrUpdate(BloodGlucoseRecord(value = it.value, date = it.date,
                        unit = it.unit, id = it.id))
            }
        }

    override fun postprocess (realm: Realm, l: List<*>) {
        debug("Postprocessing $l")
        val fi = l.first() as RealmSgvEntry
        val li = l.last() as RealmSgvEntry
        debug("Invalidating caches")
        val startDt = DateTime(li.date).withTimeAtStartOfDay()
        val endDt = DateTime(fi.date).withTimeAtStartOfDay()
        BgPostprocessor.invalidateCaches(realm, startDt, endDt)
        debug("Beginning daily grouping")
        BgPostprocessor.updatePeriods(realm, startDt.millis, endDt.plusDays(1).millis)
        debug("Finished daily grouping")
    }
}