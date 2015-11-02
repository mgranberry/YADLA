package com.kludgenics.cgmlogger.model.glucose

import android.util.Log
import com.kludgenics.cgmlogger.extension.createInsideTransaction
import com.kludgenics.cgmlogger.extension.dateTime
import com.kludgenics.cgmlogger.extension.group
import com.kludgenics.cgmlogger.extension.where
import com.kludgenics.cgmlogger.model.math.agp.AgpUtil
import com.kludgenics.cgmlogger.model.math.bgi.BgiUtil
import com.kludgenics.cgmlogger.model.math.bgi.CachedBgi
import com.kludgenics.cgmlogger.model.math.trendline.PeriodUtil
import com.kludgenics.cgmlogger.model.realm.glucose.BgByPeriod
import com.kludgenics.cgmlogger.model.realm.glucose.BloodGlucoseRecord
import com.kludgenics.cgmlogger.model.realm.glucose.refreshRecords
import io.realm.Realm
import io.realm.RealmResults
import org.joda.time.DateTime

/**
 * Created by matthiasgranberry on 6/7/15.
 */

object BgPostprocessor {

    fun invalidateCaches (realm: Realm, start: DateTime, end: DateTime) {
        AgpUtil.invalidate(start, end, realm)
        PeriodUtil.invalidate(start, end, realm)
        BgiUtil.invalidate(start, end, realm)
    }

    fun updatePeriods(realm: Realm, start: DateTime, end: DateTime) {
        Log.d("BgPostProcesser", "start: $start, end: $end")
        val bgPeriods = realm.where<BgByPeriod>().findAll()
        bgPeriods.filter {
            val periodEnd = it.start + it.duration
            (it.start <= end.millis) && (periodEnd >= start.millis)
        }.forEach { it.refreshRecords() }
        Log.d("BgPostProcesser", "end")

    }

}