package com.kludgenics.cgmlogger.model.math.agp

import android.content.Context
import android.util.Log
import com.kludgenics.cgmlogger.extension.dateTime
import com.kludgenics.cgmlogger.extension.percentiles
import com.kludgenics.cgmlogger.model.glucose.BloodGlucoseRecord
import io.realm.Realm
import org.jetbrains.anko.ctx
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Period
import java.util.*
import kotlin.properties.Delegates

/**
 * Created by matthiasgranberry on 7/4/15.
 */

public class DailyAgp(val dateTime: DateTime = DateTime(), val period: Period = Period.days(30),
                      percentileValues: DoubleArray = DailyAgp.STANDARD_PERCENTILES) {
    val TAG = "DailyAgp"

    val percentiles : DoubleArray by Delegates.lazy {
        getAgp(dateTime, period, percentileValues)
    }

    companion object {
        val SPEC_HEIGHT = 400.0f
        val SPEC_WIDTH = 240.0f
        val STANDARD_PERCENTILES: DoubleArray = doubleArrayOf(10.0, 90.0, 25.0, 75.0, 50.0)
    }

    // Generate SVG paths for printing, display, etc
    val pathStrings: Array<String> by Delegates.lazy {
        val size = percentileValues.size()
        val pointArrays = Array(size, { DoubleArray(percentiles.size() / percentileValues.size()) })
        for (i in 0..pointArrays.lastIndex) {
            for (j in 0..pointArrays[i].lastIndex) {
                pointArrays[i][j] = percentiles[5 * j + i]
            }
        }
        val stringBuilders = ArrayList<StringBuilder>(Math.ceil(pointArrays.size() / 2.0).toInt())
        val xStep = SPEC_WIDTH / pointArrays[0].size()
        for (i in 0..pointArrays.lastIndex) {
            if (i % 2 == 0) {
                stringBuilders.add(StringBuilder("M0,${SPEC_HEIGHT - pointArrays[i][0]}L"))
                pointArrays[i].forEachIndexed { idx, d -> stringBuilders.last().append(" ").append(idx * xStep).append(',').append(SPEC_HEIGHT - d) }
                stringBuilders.last().append(" ${(pointArrays[i].lastIndex + 1) * xStep},${SPEC_HEIGHT - pointArrays[i][0]}")
            } else {
                stringBuilders.last().append("L${(pointArrays[i].lastIndex + 1) * xStep},${SPEC_HEIGHT - pointArrays[i][0]}L")
                for (idx in pointArrays[i].lastIndex downTo 0) {
                    stringBuilders.last().append(" ").append(idx * xStep).append(',').append(SPEC_HEIGHT - pointArrays[i][idx])
                }
            }
        }
        Array(stringBuilders.size(), {
            if (it != stringBuilders.lastIndex) {
                stringBuilders[it].append('Z')
            }
            stringBuilders[it].toString()
        })
    }

    private fun getAgp(dateTime: DateTime, period: Period = Period.days(30), percentileValues: DoubleArray = STANDARD_PERCENTILES): DoubleArray {
        val realm = Realm.getDefaultInstance()
        realm.use {
            val timeAtStartOfDay = dateTime.withTimeAtStartOfDay()
            val res = realm.where(javaClass<BloodGlucoseRecord>())
                    .greaterThanOrEqualTo("date", timeAtStartOfDay.minus(period).getMillis())
                    .lessThanOrEqualTo("date", timeAtStartOfDay.getMillis())
                    .findAll()
            val dateBgPairs = res.map { it.dateTime.minuteOfDay().get() / 30 to it.value }
            val map = TreeMap<Int, MutableList<Pair<Int, Double>>>()
            val times = dateBgPairs.groupByTo (map, { it.first })

            return times.flatMap { it.getValue().percentiles(percentileValues, { it.second }).toList() }.toDoubleArray()
        }
    }
}