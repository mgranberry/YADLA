package com.kludgenics.cgmlogger.app.viewmodel

import android.databinding.Bindable
import android.databinding.BindingAdapter
import android.databinding.BindingConversion
import android.databinding.PropertyChangeRegistry
import android.graphics.Color
import android.graphics.PointF
import com.hookedonplay.decoviewlib.DecoView
import com.hookedonplay.decoviewlib.charts.SeriesItem
import com.hookedonplay.decoviewlib.events.DecoEvent
import com.kludgenics.alrightypump.therapy.BaseGlucoseValue
import com.kludgenics.alrightypump.therapy.GlucoseUnit
import com.kludgenics.alrightypump.therapy.GlucoseValue
import com.kludgenics.cgmlogger.app.BR
import com.kludgenics.cgmlogger.app.model.PersistedRawCgmRecord
import com.kludgenics.cgmlogger.extension.where
import io.realm.Realm
import io.realm.RealmResults
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Period
import java.text.DecimalFormat

/**
 * Created by matthias on 4/15/16.
 */
class DailyOverview(val realm: Realm,
                    val endTime: DateTime,
                    val periods: List<Period>,
                    val lowValue: Double,
                    val highValue: Double): AnkoLogger, DataBindingObservable {
    override var mCallbacks: PropertyChangeRegistry? = null
    private val bgResults = realm.allObjects(PersistedRawCgmRecord::class.java)
    init {
        info("Adding listener")
        bgResults.addChangeListener { notifyPropertyChanged(BR.dataSeries); info("Change Detected") }
    }

    private fun getResultsFor(period: Period): RealmResults<PersistedRawCgmRecord> {
        val start = DateTime()
        info ("Records from ${(endTime - period).toDate()} to ${endTime.toDate()}")
        val results = realm.where<PersistedRawCgmRecord> {
            between("_date", (endTime - period).toDate(), endTime.toDate())
        }.findAll()
        val end = DateTime()
        info("Found ${results.count()} results in ${Duration(start, end)}")
        return results
    }

    @get:Bindable
    val dataSeries: List<SeriesItem> get() {
        var inset = 0f
        val start = DateTime()
        val results = periods.flatMap {
            val dayResults = getResultsFor(it)
            if (dayResults.count() == 0)
                return emptyList()
            val inRangeQuery = dayResults.where { between("_glucose", lowValue, highValue) }
            val inRangePercent = inRangeQuery.count().toFloat() / dayResults.count()
            val lowQuery = dayResults.where { lessThanOrEqualTo("_glucose", lowValue) }
            val lowPercent = lowQuery.count().toFloat() / dayResults.count()
            val highQuery = dayResults.where { greaterThanOrEqualTo("_glucose", highValue) }
            val highPercent = highQuery.count().toFloat() / dayResults.count()
            info("inRange: $inRangePercent low: $lowPercent high: $highPercent")
            info("results: ${dayResults.count()}")
            val max = inRangePercent + lowPercent + highPercent
            val result = listOf(
                    SeriesItem.Builder(Color.parseColor("#9575cd"))
                            .setRange(0f, max, inRangePercent + lowPercent + highPercent).setLineWidth(32f)
                            .setInitialVisibility(true)
                            .setInset(PointF(inset, inset))
                            .setCapRounded(false)
                            .build(),
                    SeriesItem.Builder(/*Color.parseColor("#9575cd")*/ Color.parseColor("#00BFA5"))
                            .setRange(0f, max, inRangePercent+highPercent).setLineWidth(32f)
                            .setInitialVisibility(true)
                            .setInset(PointF(inset, inset))
                            .setCapRounded(false)
                            .build(),
                    SeriesItem.Builder(/*Color.parseColor("#1DE9B6")*/ Color.parseColor("#d1c4e9"))
                            .setRange(0f, max, highPercent).setLineWidth(32f)
                            .setInitialVisibility(true)
                            .setInset(PointF(inset, inset))
                            .setCapRounded(false)
                            .build()
            )
            inset += 36f
            result
        }
        val end = DateTime()
        info ("Query completed in ${Duration(start, end)}")
        return results
    }
}

object BindingConversion : AnkoLogger {

    @JvmStatic
    @BindingConversion
    fun convertGlucoseValueToString(value: GlucoseValue?): String? {
        if (value == null)
            return null
        val format = if (value.unit == GlucoseUnit.MGDL) {
            DecimalFormat("###")
        } else if (value.unit == GlucoseUnit.MMOL) {
            DecimalFormat("###.0")
        } else
            null
        return format?.format(value.glucose)
    }

    @JvmStatic
    @BindingAdapter("app:series")
    fun addSeriesToDecoView(view: DecoView, series: List<SeriesItem>?) {
        info("adding series:$series")
        series?.forEach { view.addSeries(it) }
    }
}