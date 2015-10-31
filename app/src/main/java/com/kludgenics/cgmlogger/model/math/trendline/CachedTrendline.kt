package com.kludgenics.cgmlogger.model.math.trendline

import android.content.Context
import com.google.flatbuffers.FlatBufferBuilder
import com.kludgenics.cgmlogger.app.util.PathParser
import com.kludgenics.cgmlogger.extension.dateTime
import com.kludgenics.cgmlogger.extension.where
import com.kludgenics.cgmlogger.model.flatbuffers.path.Entry
import com.kludgenics.cgmlogger.model.realm.glucose.BloodGlucoseRecord
import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.annotations.RealmClass
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.async
import org.jetbrains.anko.asyncResult
import org.jetbrains.anko.info
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Period
import java.io.Closeable
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

/**
 * Created by matthiasgranberry on 7/18/15.
 */

@RealmClass public open class CachedPeriod(public open var trendPath: ByteArray = ByteArray(0),
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

    public fun invalidate(start: DateTime,
                   end: DateTime,
                   realm: Realm?) {
        if (realm == null)
            Realm.getDefaultInstance().use{
                realm ->
                realm.beginTransaction()
                doInvalidate(start, end, realm)
                realm.commitTransaction()
            }
        else
            doInvalidate(start, end, realm)
    }

    private fun doInvalidate(start: DateTime,
                             end: DateTime,
                             realm: Realm) {
        val cachedItems = realm.allObjects(CachedPeriod::class.java)
        val removeList = arrayListOf<RealmObject>()
        cachedItems.forEach {
            val itemTime = it.dateTime
            val itemEnd = itemTime.plus(Period.days(it.period))
            if (itemTime > start && itemTime < end || (itemEnd > start && itemTime < end))
                removeList.add(it)
        }
        //realm.beginTransaction()
        removeList.forEach { it.removeFromRealm() }
        //realm.commitTransaction()
    }

    public fun getLatestCached(context: Context,
                        dateTime: DateTime = DateTime().withTimeAtStartOfDay(),
                        period: Period,
                        updated: ((Future<CachedPeriod>)->Unit)? = null): CachedPeriod {
        val realm = Realm.getDefaultInstance()
        realm.use {
            info("$period Querying cache")
            val result = realm.where<CachedPeriod> {
                equalTo("period", period.days)
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
                CachedPeriod(date = dateTime.toDate(), period = period.days)
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
                CachedPeriod(date = result.date, trendPath = result.trendPath, high = result.high, highCount = result.highCount,
                        inRangeCount = result.inRangeCount, low = result.low,
                        lowCount = result.lowCount, totalCount = result.totalCount,
                        period = result.period)
            } else {
                info("Current result cache is valid.  Returning cache.")

                CachedPeriod(date = result.date, trendPath = result.trendPath, high = result.high,
                        highCount = result.highCount, inRangeCount = result.inRangeCount,
                        low = result.low, lowCount = result.lowCount,
                        totalCount = result.totalCount, period = result.period)
            }
        }
    }

    private fun calculateAndCachePeriod(dateTime: DateTime, period: Period): CachedPeriod {
        val result = DailyTrendline(dateTime, period, 80.0, 180.0)
        result.use {
            val cache = CachedPeriod(date = result.dateTime.toDate(), trendPath = result.trendPath,
                    high = result.high, highCount = result.highCount,
                    inRangeCount = result.inRangeCount, low = result.low, lowCount = result.lowCount,
                    totalCount = result.totalCount, period = result.period.days)
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

    val realm: Realm by lazy(LazyThreadSafetyMode.NONE) { Realm.getDefaultInstance() }
    private val periodValues: RealmResults<BloodGlucoseRecord> by lazy(LazyThreadSafetyMode.NONE) {
        realm.where<BloodGlucoseRecord> {
            greaterThanOrEqualTo("date", dateTime.millis)
            lessThan("date", (dateTime.plus(period)).millis)
        }.findAllSorted("date", false)
    }

    private val inRangeValues: RealmResults<BloodGlucoseRecord> by lazy(LazyThreadSafetyMode.NONE) {
        periodValues.where {
            greaterThan("value", low)
            lessThan("value", high)
        }.findAllSorted("date", false)
    }

    private val lowValues: RealmResults<BloodGlucoseRecord> by lazy(LazyThreadSafetyMode.NONE) {
        periodValues.where {
            lessThanOrEqualTo("value", low)
        }.findAllSorted("date", false)
    }

    private val highValues: RealmResults<BloodGlucoseRecord> by lazy(LazyThreadSafetyMode.NONE) {
        periodValues.where {
            greaterThanOrEqualTo("value", high)
        }.findAllSorted("date", false)
    }

    override public fun close() {
        realm.close()
    }

    private fun pathDataBufferFromValues(values: List<BloodGlucoseRecord>): ByteArray {
        val pathString = if (values.isNotEmpty()) buildString {
            append("M0,${SPEC_HEIGHT - values.first().value}L")
            val timeLimit = dateTime.plus(period)
            val timeStart = dateTime
            val duration = Duration(timeStart, timeLimit)
            var lastTime = timeStart
            values.forEach {
                value ->
                val currentTime = value.dateTime
                val xScale = Duration(timeStart, currentTime).millis.toFloat() / duration.millis
                if (Duration(lastTime, currentTime).standardMinutes > 10) {
                    // Draw gaps where appropriate
                    append("M${xScale * SPEC_WIDTH},${SPEC_HEIGHT - value.value}L")
                }
                append("${xScale * SPEC_WIDTH},${SPEC_HEIGHT - value.value} ")
                lastTime = currentTime
            }
        } else ""
        val fbb = FlatBufferBuilder(500)
        fbb.finish(PathParser.populateFlatBufferFromPathData(fbb, pathString))
        return fbb.sizedByteArray()
    }

    public val trendPath: ByteArray by lazy(LazyThreadSafetyMode.NONE) { pathDataBufferFromValues(periodValues) }
    public val highCount: Int by lazy(LazyThreadSafetyMode.NONE) { highValues.count() }
    public val lowCount: Int by lazy(LazyThreadSafetyMode.NONE) { lowValues.count() }
    public val inRangeCount: Int by lazy(LazyThreadSafetyMode.NONE) { inRangeValues.count() }
    public val totalCount: Int by lazy(LazyThreadSafetyMode.NONE) { highCount + inRangeCount + lowCount }

}
