package com.kludgenics.cgmlogger.app.service

import com.google.android.gms.gcm.GcmNetworkManager
import com.kludgenics.alrightypump.cloud.nightscout.Nightscout
import com.kludgenics.alrightypump.therapy.Record
import com.kludgenics.cgmlogger.model.glucose.BgPostprocessor
import io.realm.Realm
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.joda.time.DateTime
import java.util.concurrent.Callable

/**
 * Created by matthiasgranberry on 6/4/15.
 */

class NightscoutTask(val nightscout: Nightscout, val full: Boolean = false): Callable<Int>, AnkoLogger {
    fun postprocess (realm: Realm, l: List<Record>) {
        debug("Postprocessing $l")
        val fi = l.first()
        val li = l.last()
        debug("Invalidating caches")
        val startDt = DateTime(li.time).withTimeAtStartOfDay()
        val endDt = DateTime(fi.time).withTimeAtStartOfDay()
        BgPostprocessor.invalidateCaches(realm, startDt, endDt)
        debug("Beginning daily grouping")
        BgPostprocessor.updatePeriods(realm, startDt.millis, endDt.plusDays(1).millis)
        debug("Finished daily grouping")
    }


    override fun call(): Int {
        if (full == true) {
            val records = nightscout.entries
            val treatments = nightscout.treatments
        }
        return GcmNetworkManager.RESULT_SUCCESS
    }
}