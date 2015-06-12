package com.kludgenics.cgmlogger.model.glucose

import com.google.android.gms.location.places.Place
import io.realm.Realm
import org.joda.time.DateTime
import com.kludgenics.cgmlogger.extension.*
import io.realm.RealmObject
import io.realm.RealmQuery
import io.realm.RealmResults

/**
 * Created by matthiasgranberry on 6/7/15.
 */

object BgPostprocesser {
    fun groupByDay (realm: Realm, start: DateTime, end: DateTime) {
        val bgEntries = realm.where<BloodGlucoseRecord>{
            greaterThanOrEqualTo("date", start.withTimeAtStartOfDay().getMillis())
            lessThanOrEqualTo("date", end.withTimeAtStartOfDay().getMillis())
        }.findAllSorted("date", RealmResults.SORT_ORDER_ASCENDING)
        val entriesByDay = bgEntries.groupBy { DateTime(it.getDate()).withTimeAtStartOfDay() }
        entriesByDay.forEach {
            realm.create<BgByDay>{
                setDay(it.getKey().getMillis())
                val bgList = getBgRecords()
                bgList.clear()
                bgList.addAll(it.getValue())
            }
        }
    }

    fun groupByPlace (realm: Realm, place: Place) {

    }

}