package com.kludgenics.cgmlogger.model.glucose

import android.util.Log
import com.google.android.gms.location.places.Place
import io.realm.Realm
import org.joda.time.DateTime
import com.kludgenics.cgmlogger.extension.*
import com.kludgenics.cgmlogger.model.nightscout.SgvEntry
import io.realm.RealmObject
import io.realm.RealmQuery
import io.realm.RealmResults
import java.util.*

/**
 * Created by matthiasgranberry on 6/7/15.
 */

object BgPostprocesser {

    fun groupByDay (realm: Realm, start: DateTime, end: DateTime) {
        Log.d("BgPostProcesser", "start: ${start}, end: ${end}")
        val bgEntries = realm.where<BloodGlucoseRecord> {
            greaterThanOrEqualTo("date", start.withTimeAtStartOfDay().getMillis())
            lessThanOrEqualTo("date", end.withTimeAtStartOfDay().plusDays(1).getMillis())
        }.findAllSorted("date", RealmResults.SORT_ORDER_ASCENDING)
        val entriesByDay = bgEntries.groupBy { DateTime(it.getDate()).withTimeAtStartOfDay() }
        entriesByDay.forEach {
            val dailyValues = realm.where<BgByDay> { equalTo("day", it.getKey().getMillis()) }
                    .findFirst() ?: realm.createInsideTransaction<BgByDay>{
                setDay(it.getKey().getMillis())
            }
            val bgList = dailyValues.getBgRecords()
            bgList.clear()
            bgList.addAll(it.getValue())
        }
        Log.d("BgPostProcesser", "end")

    }

    fun groupByPlace (realm: Realm, place: Place) {

    }

}