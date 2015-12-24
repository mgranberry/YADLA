package com.kludgenics.cgmlogger.model.math.agp

import android.content.Context
import com.google.flatbuffers.FlatBufferBuilder
import com.kludgenics.cgmlogger.app.util.PathParser
import com.kludgenics.cgmlogger.extension.where
import com.kludgenics.cgmlogger.model.flatbuffers.path.AgpPathBuffer
import com.kludgenics.cgmlogger.model.flatbuffers.path.PathDataBuffer
import io.realm.Realm
import io.realm.RealmObject
import io.realm.Sort
import io.realm.annotations.RealmClass
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.async
import org.jetbrains.anko.asyncResult
import org.jetbrains.anko.info
import org.joda.time.DateTime
import org.joda.time.Period
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

/**
 * Created by matthiasgranberry on 7/5/15.
 */

@RealmClass public open class CachedDatePeriodAgp(public open var outer: ByteArray = ByteArray(0),
                                                  public open var inner: ByteArray = ByteArray(0),
                                                  public open var median: ByteArray = ByteArray(0),
                                                  public open var date: Date = Date(),
                                                  public open var period: Int = 30): RealmObject() {
}

public var CachedDatePeriodAgp.dateTime: DateTime
    get() = DateTime(date)
    set(value) { date = value.toDate() }

public object AgpUtil: AnkoLogger {
    val executor: ExecutorService =
            Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors()/2 + 1)

    public fun invalidate(start: DateTime,
                          end: DateTime,
                          realm: Realm?) {
        if (realm == null)
            Realm.getDefaultInstance().use{
                realm ->
                doInvalidate(start, end, realm)
            }
        else
            doInvalidate(start, end, realm)
    }

    private fun doInvalidate(start: DateTime,
                             end: DateTime,
                             realm: Realm) {
        val cachedItems = realm.allObjects(CachedDatePeriodAgp::class.java)
        val removeList = arrayListOf<RealmObject>()
        cachedItems.forEach {
            val itemTime = it.dateTime
            val itemStart = it.dateTime.minus(Period.days(it.period))
            if ( (itemStart <= start && itemTime >= start && itemTime < end) || (itemStart >= start && itemTime < end))
                removeList.add(it)
        }
        removeList.forEach { it.removeFromRealm() }
    }

    inline fun getLatestCached(context: Context,
                        period: Period,
                        dateTime: DateTime = DateTime().withTimeAtStartOfDay(),
                        crossinline updated: ( (Future<CachedDatePeriodAgp>)->Unit)): CachedDatePeriodAgp {
        val realm = Realm.getDefaultInstance()
        realm.use {
            info("$period Querying cache")
            val result = realm.where<CachedDatePeriodAgp> {
                equalTo("period", period.days)
            }.findAllSorted("date", Sort.DESCENDING).firstOrNull()

            return if (result == null) {
                info("$period Result not cached, returning dummy")
                context.async() {
                    info("$period Async executor executing")
                    val f = context.asyncResult(executor) {
                        calculateAndCacheAgp(dateTime, period)
                    }
                    updated.invoke(f)
                }
                CachedDatePeriodAgp(date = dateTime.toDate(), period = period.days)
            } else if (result.dateTime != dateTime) {
                info("Result cached, but stale.  Calculating in background.")
                context.async() {
                    info("starting bg")
                    val f = context.asyncResult(executor) {
                        calculateAndCacheAgp(dateTime, period)
                    }
                    updated.invoke(f)
                    info("ending bg")
                }
                CachedDatePeriodAgp(result.outer, result.inner, result.median, result.date, result.period)
            } else {
                info("Current result cache is valid.  Returning cache.")
                CachedDatePeriodAgp(result.outer, result.inner, result.median, result.date, result.period)
            }
        }
    }

    fun calculateAndCacheAgp(dateTime: DateTime, period: Period): CachedDatePeriodAgp {
        info ("$period Calculating agp: $dateTime, $period")
        val currentAgp = DailyAgp(dateTime, period)
        info("$period Storing cached AGP")
        val buffer = ByteBuffer.wrap(ByteArray(500))
        var fb = FlatBufferBuilder(buffer)
        fb.finish(PathParser.populateFlatBufferFromPathData(fb, currentAgp.pathStrings[0]))
        val outer = fb.sizedByteArray()
        buffer.clear()
        fb = FlatBufferBuilder(buffer)
        fb.finish(PathParser.populateFlatBufferFromPathData(fb, currentAgp.pathStrings[1]))
        val inner = fb.sizedByteArray()
        buffer.clear()
        fb = FlatBufferBuilder(buffer)
        fb.finish(PathParser.populateFlatBufferFromPathData(fb, currentAgp.pathStrings[2]))
        val median = fb.sizedByteArray()
        val ro = CachedDatePeriodAgp(outer, inner, median, date = dateTime.toDate(), period = period.days)
        info("$period acquiring realm in caca")
        val realm = Realm.getDefaultInstance()
        info("$period acquired realm")
        realm.use {
            info("$period beginning transaction")
            realm.beginTransaction()
            info("$period in transaction")
            realm.copyToRealm(ro)
            realm.commitTransaction()
            info("$period Calculation completed")
        }
        return ro
    }
}
