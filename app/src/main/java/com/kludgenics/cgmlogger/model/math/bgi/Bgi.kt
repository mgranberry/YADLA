package com.kludgenics.cgmlogger.model.math.bgi

import com.kludgenics.cgmlogger.extension.dateTime
import com.kludgenics.cgmlogger.model.glucose.BloodGlucoseRecord
import org.joda.time.DateTime
import java.util.*

/**
 * Calculate various risk indices for blood glucose values
 * From Clarke W, Kovatchev B. Statistical Tools to Analyze Continuous Glucose Monitor Data. Diabetes Technology & Therapeutics. 2009;11(Suppl 1):S-45-S-54. doi:10.1089/dia.2008.0138.
 * These formulas expect mg/dL units.
 */

object Bgi {

    val ADRR_RISK = sortedMapOf(
            20.0 to "Low",
            30.0 to "Moderate (low)",
            40.0 to "Moderate (high)",
            50.0 to "High",
            500.0 to "Very high")

    /*  LBGI, Minimal (LBGI ≤1.1), Low (1.1 <LBGI ≤2.5), Moderate (2.5 < LBGI ≤5), and High (LBGI >5.0)18;
        HBGI, Low (HBGI ≤4.5), Moderate (4.5 <HBGI ≤9.0), and High (HBGI >9.0)
     */
    val LBGI_RISK = sortedMapOf(
            1.1 to "Minimal",
            2.5 to "Low",
            5.0 to "Moderate",
            500.0 to "High")
    val HBGI_RISK = sortedMapOf(
            4.5 to "Low",
            9.0 to "Moderate",
            500.0 to "High")

    private fun rf(bg: Double): Double {
        return 1.509 * (Math.pow(Math.log(bg), 1.084) - 5.381)
    }

    private fun rl(bg: Double): Double {
        val rv = rf(bg)
        return if (rv < 0) -10 * rv else 0.0
    }

    private fun rh(bg: Double): Double {
        val rv = rf(bg)
        return if (rv > 0) 10 * rv else 0.0
    }

    public fun lbgi(records: List<BloodGlucoseRecord>): Double {
        return records.map { rl(it.value) }.average()
    }

    public fun hbgi(records: List<BloodGlucoseRecord>): Double {
        return records.map { rh(it.value) }.average()
    }

    public fun bgRiskIndices(records: List<BloodGlucoseRecord>): Pair<Double, Double> {
        val rlRh = records.map { rl(it.value) to rh(it.value)}
        return rlRh.map { it.first }.average() to rlRh.map { it.second }.average()
    }

    public fun bgri(records: List<BloodGlucoseRecord>): Double {
        val (lbgi, hbgi) = bgRiskIndices(records)
        return lbgi + hbgi
    }

    /**
     * Calculates the ADRR as given by
     * Evaluation of a New Measure of BloodGlucose Variability in Diabetes
     */
    public fun adrr(records: List<BloodGlucoseRecord>): Double {
        val grouped = records.groupBy { DateTime(it.date).withTimeAtStartOfDay() }
        return grouped.filter { it.getValue().size() > 0 }.map {
            val bgValues = it.getValue().map { it.value }
            val minBg = bgValues.min()!! // cannot be null
            val maxBg = bgValues.max()!! // cannot be null
            rl(minBg) + rh(maxBg)
        }.average()
    }

    public fun bgRiByTimeBucket(records: List<BloodGlucoseRecord>): List<Pair<Int,FloatArray>> {
        val sortedMap = sortedMapOf<Int, MutableList<BloodGlucoseRecord>>()
        records.groupByTo(sortedMap) { it.dateTime.minuteOfDay().get() / 30}
        return sortedMap.map {
            val (lbgi, hbgi) = bgRiskIndices(it.getValue())
            it.getKey() to floatArrayOf(-lbgi.toFloat(), hbgi.toFloat())
        }
    }

    public fun evaluateRisk(map: SortedMap<Double, String>, value: Double) : String {
        val key = map.tailMap(value).firstKey()
        return map.getOrElse(key, {map[map.lastKey()]})!!
    }

}