package com.kludgenics.cgmlogger.app.viewmodel

import android.databinding.Bindable
import android.databinding.BindingAdapter
import android.databinding.BindingConversion
import android.databinding.PropertyChangeRegistry
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import com.hookedonplay.decoviewlib.DecoView
import com.hookedonplay.decoviewlib.charts.SeriesItem
import com.hookedonplay.decoviewlib.events.DecoEvent
import com.kludgenics.alrightypump.therapy.BaseGlucoseValue
import com.kludgenics.alrightypump.therapy.GlucoseUnit
import com.kludgenics.alrightypump.therapy.GlucoseValue
import com.kludgenics.cgmlogger.app.BR
import com.kludgenics.cgmlogger.app.R
import com.kludgenics.cgmlogger.app.model.PersistedRawCgmRecord
import com.kludgenics.cgmlogger.extension.where
import com.kludgenics.justgivemeachart.BasicLine
import com.kludgenics.justgivemeachart.ChartView
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.async
import org.jetbrains.anko.dip
import org.jetbrains.anko.info
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Period
import java.text.DecimalFormat
import java.util.*

/**
 * Created by matthias on 4/15/16.
 */
class DailyOverview(val realm: Realm,
                    val endTime: DateTime,
                    val periods: List<Period>,
                    val lowValue: Int,
                    val highValue: Int): AnkoLogger, DataBindingObservable {
    override var mCallbacks: PropertyChangeRegistry? = null
    private val bgResults = realm.allObjects(PersistedRawCgmRecord::class.java)
    init {
        info("Adding listener")
        bgResults.addChangeListener {
            notifyPropertyChanged(BR.dataSeries)
            notifyPropertyChanged(BR.currentBg)
        }
    }

    private fun getResultsFor(period: Period): RealmResults<PersistedRawCgmRecord> {
        val start = DateTime()
        info ("Records from ${(endTime - period).toDate()} to ${endTime.toDate()}")
        val results = realm.where<PersistedRawCgmRecord> {
            between("_date", (endTime - period).toDate(), endTime.toDate())
            greaterThanOrEqualTo("_glucose", 39)
        }.findAll()
        val end = DateTime()
        info("Found ${results.count()} results in ${Duration(start, end)}")
        return results
    }

    @get:Bindable
    val currentBg: GlucoseInfo? get() {
        val realm = Realm.getDefaultInstance()
        realm.use {
            val results = realm.where<PersistedRawCgmRecord> {
                isNotNull("_glucose")
            }.findAllSorted("_date", Sort.DESCENDING).take(2)
            return when(results.size) {
                0 -> null
                else -> return GlucoseInfo(BaseGlucoseValue(results.component1().value.glucose,
                                                            results.component1().value.unit),
                                           results.component1()._date,
                                GlucoseInfo(BaseGlucoseValue(results.component2().value.glucose,
                                                             results.component2().value.unit),
                                            results.component2()._date, null))
            }
        }
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

data class GlucoseInfo(val glucose: GlucoseValue, val date: Date, val previous: GlucoseInfo?) {
    fun format(): String {
        val glucoseFormat = if (glucose.unit == GlucoseUnit.MGDL) DecimalFormat("###") else DecimalFormat("###.0")
        val delta = if (glucose.glucose != null && previous?.glucose?.glucose != null)
            glucose.glucose?.minus(previous?.glucose?.glucose ?: 0.0)
        else null
        val currentString = if (glucose.glucose != null) glucoseFormat.format(glucose!!.glucose) else "—"
        val deltaString = if (delta != null) glucoseFormat.format(delta) else "—"
        val glucoseString = """$currentString ($deltaString)"""
        return glucoseString
    }
}

object BindingConversion : AnkoLogger {

    @JvmStatic
    @BindingConversion
    fun convertGlucoseInfoToString(value: GlucoseInfo?): String? {
        return value?.format()?:"—"
    }

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
    @BindingAdapter("app:period")
    fun addLineToChartView(view: ChartView, period: Int?) {
        if (period != null)
            async() {
                val realm = Realm.getDefaultInstance()
                realm.use {
                    realm.executeTransaction {
                        info("Drawing chart")
                        val results = realm.where<PersistedRawCgmRecord> {
                            between("_date", Date(System.currentTimeMillis() - period), Date())
                        }.findAllSorted("_date")
                        val adapter = BloodGlucoseValueAdapter(results, minMillis = results.first()._date.time, maxMillis = Date().time)
                        val paint = Paint()
                        val dips = view.dip(1.75f).toFloat()
                        paint.strokeWidth = dips
                        paint.color = view.resources.getColor(R.color.color_primary, null)
                        val line = BasicLine(valueAdapter = adapter, primaryPaint = paint, drawPoints = results.size <= 50)
                        view.setLines(line)
                        info("Done drawing chart")
                    }
                }
            }
    }

    @JvmStatic
    @BindingAdapter("app:series")
    fun addSeriesToDecoView(view: DecoView, series: List<SeriesItem>?) {
        info("adding series:$series")
        series?.forEach { view.addSeries(it) }
    }
}