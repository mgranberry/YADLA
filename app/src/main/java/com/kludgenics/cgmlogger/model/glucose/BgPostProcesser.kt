package com.kludgenics.cgmlogger.model.glucose

import android.util.Log
import com.kludgenics.cgmlogger.extension.createInsideTransaction
import com.kludgenics.cgmlogger.extension.dateTime
import com.kludgenics.cgmlogger.extension.where
import com.kludgenics.cgmlogger.model.math.agp.AgpUtil
import com.kludgenics.cgmlogger.model.math.bgi.BgiUtil
import com.kludgenics.cgmlogger.model.math.bgi.CachedBgi
import com.kludgenics.cgmlogger.model.math.trendline.PeriodUtil
import com.kludgenics.cgmlogger.model.realm.glucose.BgByDay
import com.kludgenics.cgmlogger.model.realm.glucose.BloodGlucoseRecord
import io.realm.Realm
import io.realm.RealmResults
import org.joda.time.DateTime

/**
 * Created by matthiasgranberry on 6/7/15.
 */

object BgPostprocesser {

    fun invalidateCaches (realm: Realm, start: DateTime, end: DateTime) {
        AgpUtil.invalidate(start, end, realm)
        PeriodUtil.invalidate(start, end, realm)
        BgiUtil.invalidate(start, end, realm)
    }

    fun groupByDay (realm: Realm, start: DateTime, end: DateTime) {
        Log.d("BgPostProcesser", "start: $start, end: $end")

        val bgEntries = realm.where<BloodGlucoseRecord> {
            greaterThanOrEqualTo("date", start.withTimeAtStartOfDay().millis)
            lessThanOrEqualTo("date", end.withTimeAtStartOfDay().plusDays(1).millis)
        }.findAllSorted("date", RealmResults.SORT_ORDER_ASCENDING)
        val entriesByDay = bgEntries.groupBy { it.dateTime.withTimeAtStartOfDay() }
        entriesByDay.forEach {
            val dailyValues = realm.where<BgByDay> { equalTo("day", it.key.millis) }
                    .findFirst() ?: realm.createInsideTransaction<BgByDay>{
                day = it.key.millis
            }
            val bgList = dailyValues.bgRecords
            bgList.clear()
            bgList.addAll(it.value)
        }
        Log.d("BgPostProcesser", "end")

    }

}