package com.kludgenics.cgmlogger.model.math.bgi

import android.util.Log
import com.kludgenics.cgmlogger.model.glucose.BloodGlucoseRecord
import io.realm.RealmList
import org.joda.time.DateTime

/**
 * Calculate various risk indices for blood glucose values
 * From Clarke W, Kovatchev B. Statistical Tools to Analyze Continuous Glucose Monitor Data. Diabetes Technology & Therapeutics. 2009;11(Suppl 1):S-45-S-54. doi:10.1089/dia.2008.0138.
 * These formulas expect mg/dL units.
 */

object BgiUtil {

    val ADRR_RISK = sortedMapOf(20.0 to "Low",
            30.0 to "Moderate (low)",
            40.0 to "Moderate (high)",
            50.0 to "High",
            500.0 to "Very high")

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

    fun lbgi(records: List<BloodGlucoseRecord>): Double {
        return records.map { rl(it.value) }.average()
    }

    fun hbgi(records: List<BloodGlucoseRecord>): Double {
        return records.map { rh(it.value) }.average()
    }

    fun bgri(records: List<BloodGlucoseRecord>): Double {
        return lbgi(records) + hbgi(records)
    }

    /**
     * Calculates the ADRR as given by
     * Evaluation of a New Measure of BloodGlucose Variability in Diabetes
     */
    fun adrr(records: List<BloodGlucoseRecord>): Double {
        val grouped = records.groupBy { DateTime(it.date).withTimeAtStartOfDay() }
        return grouped.filter { it.getValue().size() > 0 }.map {
            val minBg = it.getValue().map { it.value }.min()!! // cannot be null
            val maxBg = it.getValue().map { it.value }.max()!! // cannot be null
            rl(minBg) + rh(maxBg)
        }.average()
    }


    fun adrr_risk(adrr: Double) : String {
        val key = ADRR_RISK.tailMap(adrr).firstKey()
        return ADRR_RISK.getOrElse(key, {ADRR_RISK[ADRR_RISK.lastKey()]})!!
    }

}