package com.kludgenics.cgmlogger.model.math

import android.content.Context
import com.kludgenics.cgmlogger.extension.percentiles
import com.kludgenics.cgmlogger.model.glucose.BloodGlucoseRecord
import io.realm.Realm
import org.jetbrains.anko.ctx
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Period
import java.util.*

/**
 * Created by matthiasgranberry on 7/4/15.
 */

class Agp(val ctx: Context) {

    companion object {
        val percentiles: DoubleArray = doubleArrayOf(10.0, 25.0, 50.0, 75.0, 90.0)
    }

    fun getAgp(period: Period = Period.days(30), percentileValues: DoubleArray = percentiles): List<Pair<Int, DoubleArray>> {
        val realm = Realm.getInstance(ctx)
        realm.use {
            val timeAtStartOfDay = DateTime().withTimeAtStartOfDay()
            val res = realm.where(javaClass<BloodGlucoseRecord>())
                    .greaterThanOrEqualTo("date", timeAtStartOfDay.minus(period).getMillis())
                    .lessThanOrEqualTo("date", timeAtStartOfDay.getMillis())
                    .findAll()
            if (res.isNotEmpty()) {
                val dateBgPairs = res.map { DateTime(it.getDate()).minuteOfDay().get() / 30 to it.getValue() }
                val map = TreeMap<Int, MutableList<Pair<Int, Double>>>()
                val times = dateBgPairs.groupByTo (map, { it.first })
                val tperc: List<Pair<Int, DoubleArray>> = times.map { it.getKey() to it.getValue().percentiles(percentileValues, { it.second }) }

                return tperc
            }
        }
        return emptyList()
    }
}