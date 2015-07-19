package com.kludgenics.cgmlogger.model.math.bgi

import android.content.Context
import com.kludgenics.cgmlogger.extension.where
import com.kludgenics.cgmlogger.model.glucose.BgByDay
import com.kludgenics.cgmlogger.model.math.agp.CachedDatePeriodAgp
import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.RealmClass
import org.jetbrains.anko.AnkoLogger
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
<svg height="${svgHeight}pt" version="1.1" viewBox="0 0 240 400" width="${svgWidth}pt" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink">
    <g id="agp">
        <path d="${hbg}" stroke="yellow"/>
        <path d="${lbg}" stroke="red"/>
 </g>
</svg>
"""

object BgiUtil: AnkoLogger {
    val SPEC_HEIGHT=200f
    val SPEC_WIDTH=240f

    fun getLatestCached (dateTime: DateTime, period: Period): CachedBgi? {
        info("getting cache for ${dateTime} ${period}")
        val realm = Realm.getDefaultInstance()
        if (period.getDays() != 1)
            throw UnsupportedOperationException("Days > 1 not implemented yet")
        realm.use {
            info("querying cache")
            val result = realm.where<CachedBgi> {
                equalTo("period", period.getDays())
                equalTo("date", dateTime.toDate())
            }.findAll().firstOrNull()
            return if (result != null) {
                info("cached BGIs found")
                result
            } else {
                val bgDay = realm.where<BgByDay> {
                    equalTo("day", dateTime.withTimeAtStartOfDay().getMillis())
                }.findAll().firstOrNull()
                if (bgDay != null) {
                    val records = bgDay.getBgRecords()
                    val riskIndexes = Bgi.bgRiByTimeBucket(records)
                    val (lbgi, hbgi) = Bgi.bgRiskIndices(records)
                    val date = dateTime.withTimeAtStartOfDay().toDate()
                    val hb = StringBuilder("M0,${SPEC_HEIGHT/2}L")
                    val lb = StringBuilder("M0,${SPEC_HEIGHT/2}L")
                    riskIndexes.forEach {
                        index ->
                        val x = index.first * 5
                        val values = index.second
                        val lbg = values[0]
                        val hbg = values[1]
                        lb.append(" ${x},${SPEC_HEIGHT/2 - lbg}")
                        hb.append(" ${x},${SPEC_HEIGHT/2 + hbg}")
                    }
                    val cbgi = CachedBgi(hbg = hb.toString(), lbg = lb.toString(), lbgi = lbgi.toFloat(), hbgi = hbgi.toFloat(), date = date, period = period.getDays())
                    realm.beginTransaction()
                    realm.copyToRealm(cbgi)
                    realm.commitTransaction()
                    cbgi
                } else
                    null
            }

        }
    }
}

