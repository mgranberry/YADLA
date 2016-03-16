package com.kludgenics.cgmlogger.model.glucose

import com.kludgenics.cgmlogger.extension.where
import com.kludgenics.cgmlogger.model.math.agp.AgpUtil
import com.kludgenics.cgmlogger.model.math.bgi.BgiUtil
import com.kludgenics.cgmlogger.model.math.trendline.PeriodUtil
import com.kludgenics.cgmlogger.model.realm.glucose.BgByPeriod
import com.kludgenics.cgmlogger.model.realm.glucose.BloodGlucoseRecord
import com.kludgenics.cgmlogger.model.realm.glucose.populateData
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.joda.time.DateTime
import org.joda.time.Duration

/**
 * Created by matthiasgranberry on 6/7/15.
 */

object BgPostprocessor: AnkoLogger {

    fun invalidateCaches(realm: Realm, start: DateTime, end: DateTime) {
        AgpUtil.invalidate(start, end, realm)
        PeriodUtil.invalidate(start, end, realm)
        BgiUtil.invalidate(start, end, realm)
    }

    fun updatePeriods(realm: Realm, start: Long, end: Long) {
        info("start: ${DateTime(start)}, ${DateTime(end)}")
        val bgPeriods = realm.where<BgByPeriod> {
            equalTo("duration", 86400000L)
            between("start", start, end)
        }.findAll().groupBy { DateTime(it.start).withTimeAtStartOfDay().millis }
        bgPeriods.forEach { info("Preexisting period: ${DateTime(it.key)}") }
        val BGs = sortedMapOf<Long, MutableList<BloodGlucoseRecord>>()
        realm.where<BloodGlucoseRecord> {
            between("date", start, end)
        }.findAllSorted("date", Sort.ASCENDING)
                .groupByTo(BGs) { DateTime(it.date).withTimeAtStartOfDay().millis }
        BGs.filter { it.key !in bgPeriods }
                .forEach {
                    info("Creating ${DateTime(it.key)}")
                    val record = BgByPeriod()
                    record.start = it.key
                    val item = realm.copyToRealm(record)
                    item.bgRecords.addAll(it.value)
                    if (item.bgRecords.size > 0)
                        item.populateData()
                }
        bgPeriods.flatMap { it.value }.filter {
            val periodEnd = it.start + it.duration
            (it.start <= end) && (periodEnd >= start)
        }.forEach {
            info("Refreshing ${DateTime(it.start)} to ${DateTime(it.start) + Duration(it.duration)}")
            it.bgRecords.clear()
            it.bgRecords.addAll(BGs.getOrElse(it.start, {emptyList<BloodGlucoseRecord>()}))
            it.data = it.populateData()
            info("Refreshed")
        }
        info("end")
    }

}
