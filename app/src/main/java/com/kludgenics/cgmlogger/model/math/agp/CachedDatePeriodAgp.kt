package com.kludgenics.cgmlogger.model.math.agp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.RealmClass
import org.joda.time.DateTime
import org.joda.time.Period
import java.util.*
import com.kludgenics.cgmlogger.extension.*
import org.jetbrains.anko.*
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.properties.Delegates

/**
 * Created by matthiasgranberry on 7/5/15.
 */

@RealmClass public open class CachedDatePeriodAgp(public open var outer: String = "",
                                                  public open var inner: String = "",
                                                  public open var median: String = "",
                                                  public open var date: Date = Date(),
                                                  public open var period: Int = 30): RealmObject() {
    @Ignore
    public var svgHeight: Float = DailyAgp.SPEC_HEIGHT
    @Ignore
    public var svgWidth: Float = DailyAgp.SPEC_WIDTH
    @Ignore
    public var target: Float = 100f
    @Ignore
    public var high: Float = 180f
    @Ignore
    public var low: Float = 80f
}

public object AgpUtil: AnkoLogger {
    fun getLatestCached(context: Context, period: Period,
                        updated: ((Future<CachedDatePeriodAgp>)->Unit)? = null,
                        dateTime: DateTime = DateTime().withTimeAtStartOfDay()): CachedDatePeriodAgp {
        val realm = Realm.getDefaultInstance()
        realm.use {
            info("$period Querying cache")
            val result = realm.where<CachedDatePeriodAgp> {
                equalTo("period", period.getDays())
            }.findAllSorted("date", false).firstOrNull()

            return if (result == null) {
                info("$period Result not cached, returning dummy")
                context.async() {
                    info("$period Async executor executing")
                    val f = context.asyncResult {
                        calculateAndCacheAgp(dateTime, period)
                    }
                    updated?.invoke(f)
                }
                CachedDatePeriodAgp(date = dateTime.toDate(), period = period.getDays())
            } else if (result.dateTime != dateTime) {
                info("Result cached, but stale.  Calculating in background.")
                context.async() {
                    info("starting bg")
                    val f = context.asyncResult {
                        calculateAndCacheAgp(dateTime, period)
                    }
                    updated?.invoke(f)
                    info("ending bg")
                }
                CachedDatePeriodAgp(result.outer, result.inner, result.median, result.date, result.period)
            } else {
                info("Current result cache is valid.  Returning cache.")
                CachedDatePeriodAgp(result.outer, result.inner, result.median, result.date, result.period)
            }
        }
    }

    private fun calculateAndCacheAgp(dateTime: DateTime, period: Period): CachedDatePeriodAgp {
        info ("$period Calculating agp: ${dateTime}, ${period}")
        val currentAgp = DailyAgp(dateTime, period)
        info("$period Storing cached AGP")
        val ro = CachedDatePeriodAgp(currentAgp.pathStrings[0], currentAgp.pathStrings[1],
                currentAgp.pathStrings[2], date = dateTime.toDate(), period = period.getDays())
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
public var CachedDatePeriodAgp.dateTime: DateTime
    get() = DateTime(date)
    set(value) { date = value.toDate() }


public val CachedDatePeriodAgp.svg: String
    get() =
            """
<?xml version="1.0" encoding="utf-8" standalone="no"?>
<!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">
<svg height="${svgHeight}pt" version="1.1" viewBox="0 0 240 400" width="${svgWidth}pt" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink">
    <g id="agp">
        <path d="${outer}" fill="#2d95c2"/>
        <path d="${inner}" fill="#005882"/>
        <path d="${median}" stroke="#bce6ff" fill-opacity="0.0" stroke-with="3"/>

        <line x1="0" y1="${400 - target}" y2="${400 - target}" x2="360" stroke="green"/>
        <line x1="0" y1="${400 - high}" y2="${400 - high}" x2="360" stroke="yellow"/>
        <line x1="0" y1="${400 - low}" y2="${400 - low}" x2="360" stroke="red"/>
    </g>
</svg>
"""
