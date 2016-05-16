package com.kludgenics.cgmlogger.app.viewmodel

import com.kludgenics.cgmlogger.app.model.PersistedRawCgmRecord
import com.kludgenics.justgivemeachart.ValueAdapter
import io.realm.RealmResults
import org.jetbrains.anko.AnkoLogger
import java.util.*

class BloodGlucoseValueAdapter(val results: RealmResults<PersistedRawCgmRecord>,
                               override val maxValue: Float = 200f,
                               override val minValue: Float = 39f,
                               val maxMillis: Long = Date().time,
                               val minMillis: Long = results.firstOrNull()?._date?.time ?: 0L) : ValueAdapter<PersistedRawCgmRecord, Date>, AnkoLogger {

    override val maxIndex: Float get() = (maxMillis - minMillis).toFloat()
    override val minIndex: Float get() = 0f

    override fun scaledValue(value: PersistedRawCgmRecord): Float {
        val result = ((value._glucose?.toFloat() ?: minValue) - minValue) / (maxValue - minValue)
        return result
    }

    override fun scaledIndex(index: Date): Float {
        val result = (index.time - minMillis) / (maxIndex - minIndex)
        return result
    }

    override val points: List<ValueAdapter.Point<PersistedRawCgmRecord, Date>?> = {
        var latestMillis = results.first()._date.time
        results.filter { it._glucose != null && it._glucose!! >= 39f }.flatMap {
            val list = arrayListOf<ValueAdapter.Point<PersistedRawCgmRecord, Date>?>(ValueAdapter.Point(it, it._date))
            if (it._date.time - latestMillis > (60000 * 6)) {
                list.add(0, null)
            }
            latestMillis = it._date.time
            list
        }
    }()

    override val scaledPoints: List<ValueAdapter.Point<Float, Float>?> by lazy { points.map { it?.scaled } }
}