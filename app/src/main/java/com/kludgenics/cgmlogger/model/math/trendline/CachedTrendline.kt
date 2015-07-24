package com.kludgenics.cgmlogger.model.math.trendline

import android.content.Context
import com.kludgenics.cgmlogger.extension.dateTime
import com.kludgenics.cgmlogger.extension.where
import com.kludgenics.cgmlogger.model.glucose.BloodGlucoseRecord
import com.kludgenics.cgmlogger.model.math.agp.CachedDatePeriodAgp
import com.kludgenics.cgmlogger.model.math.agp.DailyAgp
import com.kludgenics.cgmlogger.model.math.agp.dateTime
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.annotations.RealmClass
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.async
import org.jetbrains.anko.asyncResult
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Period
import java.io.Closeable
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.properties.Delegates

/**
 * Created by matthiasgranberry on 7/18/15.
 */

@RealmClass public open class CachedPeriod(public open var trendLine: String = "",
                                           public open var highCount: Int = 0,
                                           public open var lowCount: Int = 0,
                                           public open var inRangeCount: Int = 0,
                                           public open var totalCount: Int = highCount + inRangeCount + lowCount,
                                           public open var low: Double = 80.0,
                                           public open var high: Double = 180.0,
                                           public open var date: Date = Date(),
                                           public open var period: Int = 1) : RealmObject() {

}

public var CachedPeriod.dateTime: DateTime
    get() = DateTime(date)
    set(value) { date = value.toDate() }

public object PeriodUtil: AnkoLogger {
    val executor: ExecutorService =
            Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors()/2 + 1)

    fun getLatestCached(context: Context,
                        dateTime: DateTime = DateTime().withTimeAtStartOfDay(),
                        period: Period,
                        updated: ((Future<CachedPeriod>)->Unit)? = null): CachedPeriod {
        val realm = Realm.getDefaultInstance()
        realm.use {
            info("$period Querying cache")
            val result = realm.where<CachedPeriod> {
                equalTo("period", period.getDays())
                equalTo("date", dateTime.toDate())
            }.findAllSorted("date", false).firstOrNull()

            return if (result == null) {
                info("$period Result not cached, returning dummy")
                context.async() {
                    info("$period Async executor executing")
                    val f = context.asyncResult(executor) {
                        calculateAndCachePeriod(dateTime, period)
                    }
                    updated?.invoke(f)
                }
                CachedPeriod(date = dateTime.toDate(), period = period.getDays())
            } else if (result.dateTime != dateTime) {
                info("Result cached, but stale.  Calculating in background.")
                context.async() {
                    info("starting bg")
                    val f = context.asyncResult(executor) {
                        calculateAndCachePeriod(dateTime, period)
                    }
                    updated?.invoke(f)
                    info("ending bg")
                }
                CachedPeriod(date = result.date, trendLine = result.trendLine, high = result.high, highCount = result.highCount,
                        inRangeCount = result.inRangeCount, low = result.low,
                        lowCount = result.lowCount, totalCount = result.totalCount,
                        period = result.period)
            } else {
                info("Current result cache is valid.  Returning cache.")

                CachedPeriod(date = result.date, trendLine = result.trendLine, high = result.high,
                        highCount = result.highCount, inRangeCount = result.inRangeCount,
                        low = result.low, lowCount = result.lowCount,
                        totalCount = result.totalCount, period = result.period)
            }
        }
    }

    private fun calculateAndCachePeriod(dateTime: DateTime, period: Period): CachedPeriod {
        val result = DailyTrendline(dateTime, period, 80.0, 180.0)
        result.use {
            val cache = CachedPeriod(date = result.dateTime.toDate(), trendLine = result.trendLine,
                    high = result.high, highCount = result.highCount,
                    inRangeCount = result.inRangeCount, low = result.low, lowCount = result.lowCount,
                    totalCount = result.totalCount, period = result.period.getDays())
            result.realm.beginTransaction()
            result.realm.copyToRealm(cache)
            result.realm.commitTransaction()
            return cache
        }
    }
}

class DailyTrendline(val dateTime: DateTime, val period: Period, val low: Double, val high: Double): Closeable {
    companion object {
        val SPEC_HEIGHT = 400f
        val SPEC_WIDTH = 240f
    }

    val realm: Realm by Delegates.lazy { Realm.getDefaultInstance() }
    private val periodValues: RealmResults<BloodGlucoseRecord> by Delegates.lazy {
        realm.where<BloodGlucoseRecord> {
            greaterThanOrEqualTo("date", dateTime.getMillis())
            lessThan("date", (dateTime + period).getMillis())
        }.findAllSorted("date", false)
    }

    private val inRangeValues: RealmResults<BloodGlucoseRecord> by Delegates.lazy {
        periodValues.where {
            greaterThan("value", low)
            lessThan("value", high)
        }.findAllSorted("date", false)
    }

    private val lowValues: RealmResults<BloodGlucoseRecord> by Delegates.lazy {
        periodValues.where {
            lessThanOrEqualTo("value", low)
        }.findAllSorted("date", false)
    }

    private val highValues: RealmResults<BloodGlucoseRecord> by Delegates.lazy {
        periodValues.where {
            greaterThanOrEqualTo("value", high)
        }.findAllSorted("date", false)
    }

    override public fun close() {
        realm.close()
    }

    private fun stringFromValues (values: List<BloodGlucoseRecord>): String {
        return if (values.isNotEmpty()) StringBuilder {
            append("M0,${SPEC_HEIGHT - values.first().value}L")
            val timeLimit = dateTime + period
            val timeStart = dateTime
            val duration = Duration(timeStart, timeLimit)
            var lastTime = timeStart
            values.forEach {
                value ->
                val currentTime = value.dateTime
                val xScale = Duration(timeStart, currentTime).getMillis().toFloat() / duration.getMillis()
                if (Duration(lastTime, currentTime).getStandardMinutes() > 10) {
                    // Draw gaps where appropriate
                    append("M${xScale * SPEC_WIDTH},${SPEC_HEIGHT - value.value}L")
                }
                append("${xScale * SPEC_WIDTH},${SPEC_HEIGHT - value.value} ")
                lastTime = currentTime
            }
        }.toString() else ""
    }

    public val trendLine: String by Delegates.lazy { stringFromValues(periodValues) }
    public val highCount: Int by Delegates.lazy { highValues.count() }
    public val lowCount: Int by Delegates.lazy { lowValues.count() }
    public val inRangeCount: Int by Delegates.lazy { inRangeValues.count() }
    public val totalCount: Int by Delegates.lazy { highCount + inRangeCount + lowCount }

}
