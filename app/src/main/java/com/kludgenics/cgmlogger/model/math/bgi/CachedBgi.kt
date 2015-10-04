package com.kludgenics.cgmlogger.model.math.bgi

import com.kludgenics.cgmlogger.extension.where
import com.kludgenics.cgmlogger.model.glucose.BloodGlucoseRecord
import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.RealmClass
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.joda.time.DateTime
import org.joda.time.Period
import java.util.*

/**
 * Created by matthiasgranberry on 7/18/15.
 */

@RealmClass public open class CachedBgi(public open var hbg: String = "",
                                        public open var lbg: String = "",
                                        public open var lbgi: Float = 0f,
                                        public open var hbgi: Float = 0f,
                                        public open var adrr: Float = 0f,
                                        public open var percentile975: Float = 0f,
                                        public open var percentile025: Float = 0f,
                                        public open var date: Date = Date(),
                                        public open var period: Int = 1): RealmObject() {
    @Ignore var svgHeight = BgiUtil.SPEC_HEIGHT
    @Ignore var svgWidth = BgiUtil.SPEC_WIDTH
}

public var CachedBgi.dateTime: DateTime
    get() = DateTime(date)
    set(dateTime) { date = dateTime.toDate() }

public val CachedBgi.svg: String
    get() =
    """
<?xml version="1.0" encoding="utf-8" standalone="no"?>
<!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">
<svg height="${svgHeight}pt" version="1.1" viewBox="0 0 ${BgiUtil.SPEC_WIDTH} ${BgiUtil.SPEC_HEIGHT}" width="${svgWidth}pt" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink">
    <g id="agp">
        <path d="$hbg" stroke="yellow" fill-opacity="25.0" fill="yellow" />
        <path d="$lbg" stroke="red" fill-opacity="25.0" fill="red"/>
 </g>
</svg>
"""

object BgiUtil: AnkoLogger {
    val SPEC_HEIGHT = 200f
    val SPEC_WIDTH = 240f

    fun getLatestCached(dateTime: DateTime, period: Period): CachedBgi? {
        val start = dateTime.withTimeAtStartOfDay()
        info("getting cache for! $start $period")
        val realm = Realm.getDefaultInstance()
        realm.use {
            info("querying cache")
            val result = realm.where<CachedBgi> {
                equalTo("period", period.days)
                equalTo("date", start.toDate())
            }.findAll().firstOrNull()
            return if (result != null) {
                info("cached BGIs found")
                CachedBgi(result.hbg, result.lbg, result.lbgi, result.hbgi, result.adrr, result.percentile975, result.percentile025, result.date, result.period)
            } else {
                info("No cache, calculating")
                val records = realm.where<BloodGlucoseRecord> {
                    greaterThanOrEqualTo("date", (start.minus(period)).millis)
                    lessThanOrEqualTo("date", (start).millis)
                }.findAll()
                val riskIndexes = Bgi.bgRiByTimeBucket(records)
                val (lbgi, hbgi) = Bgi.bgRiskIndices(records)
                val date = start.withTimeAtStartOfDay().toDate()
                val hb = StringBuilder("M0,${SPEC_HEIGHT / 2}L")
                val lb = StringBuilder("M0,${SPEC_HEIGHT / 2}L")
                riskIndexes.forEach {
                    index ->
                    val x = index.first * 5
                    val values = index.second
                    val lbg = values[0]
                    val hbg = values[1]
                    lb.append(" $x,${SPEC_HEIGHT / 2 - lbg}")
                    hb.append(" $x,${SPEC_HEIGHT / 2 - hbg}")
                }
                lb.append("$SPEC_WIDTH,${SPEC_HEIGHT / 2}Z")
                hb.append("$SPEC_WIDTH,${SPEC_HEIGHT / 2}Z")
                info ("calculated, storing")
                val cbgi = CachedBgi(hbg = hb.toString(), lbg = lb.toString(), lbgi = lbgi.toFloat(), hbgi = hbgi.toFloat(), date = date, period = period.days)
                realm.beginTransaction()
                realm.copyToRealm(cbgi)
                realm.commitTransaction()
                info ("stored")
                cbgi
            }
        }
    }
}

