package com.kludgenics.cgmlogger.app.viewmodel

import android.databinding.PropertyChangeRegistry
import com.kludgenics.cgmlogger.app.model.PersistedRawCgmRecord
import com.kludgenics.cgmlogger.extension.where
import io.realm.Realm
import io.realm.RealmResults
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.joda.time.DateTime
import org.joda.time.Period

data class QuantileResult(val quantile: Int, val value: Double)

class Quantiles(values: RealmResults<PersistedRawCgmRecord>,
                val timeFieldName: String,
                val aggregationPeriodMillis: Int,
                vararg val quantiles: Int) : AnkoLogger {
    private val subPeriods = values.max(timeFieldName).toLong() / aggregationPeriodMillis
    private val subResults = (1..subPeriods).map { subPeriod ->
        with(values.where()) {
            between(timeFieldName, (subPeriod - 1) * aggregationPeriodMillis, subPeriod * aggregationPeriodMillis)
            greaterThan("_glucose", 38)
            isNotNull("_glucose")
        }.findAllSorted("_glucose")
    }

    val quantileResults: List<List<QuantileResult>> =
                subResults.map { realmResults ->
                    quantiles.map { quantile ->
                        calculateQuantile(realmResults, quantile)
                    }
                }
    val averages: List<Double> = subResults.map { it.average("_glucose") }

    fun calculateQuantile(values: RealmResults<PersistedRawCgmRecord>, quantile: Int): QuantileResult {
        val rank = (values.size) * quantile / 100.0
        val rR = Math.floor(rank).toInt()
        val rF = rank - rR
        val iMin = values[rR]
        val iMax = if (rR < values.size + 1)
            values[rR + 1]
        else values[rR]

        val glucose = iMin.glucose!!
        return QuantileResult(quantile, glucose + (iMax.glucose!! - glucose) * rF)
    }
}

class AgpViewModel(realm: Realm, val period: Period, vararg val endTimes: DateTime) : DataBindingObservable {
    override var mCallbacks: PropertyChangeRegistry? = null

    private val results = realm.where<PersistedRawCgmRecord> {
        endTimes.forEachIndexed { i, endTime ->
            if (i > 0)
                or().between("_date", (endTime - period).toDate(), endTime.toDate())
            else
                between("_date", (endTime - period).toDate(), endTime.toDate())
        }
        this
    }.findAll()

    private var quantiles = Quantiles(results, "millisOfDay", 30 * 60 * 1000, 10, 25, 50, 75, 90)
    val agps: List<List<QuantileResult>> get() = quantiles.quantileResults
    val averages: List<Double> = quantiles.averages

    init {
        results.addChangeListener {
            quantiles = Quantiles(results, "millisOfDay", 30 * 60 * 1000, 10, 25, 50, 75, 90)
            notifyChange()
        }
    }


}