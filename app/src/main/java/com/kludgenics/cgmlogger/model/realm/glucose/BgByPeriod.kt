package com.kludgenics.cgmlogger.model.realm.glucose

import com.google.flatbuffers.FlatBufferBuilder
import com.kludgenics.cgmlogger.extension.transaction
import com.kludgenics.cgmlogger.extension.where
import com.kludgenics.cgmlogger.model.flatbuffers.path.*
import io.realm.*
import io.realm.annotations.PrimaryKey
import org.joda.time.DateTime
import org.joda.time.Duration

/**
 * Created by matthiasgranberry on 6/7/15.
 */
public open class BgByPeriod : RealmObject() {
    @PrimaryKey
    public open var start = 0L
    public open var duration = 86400000L
    public open var bgRecords: RealmList<BloodGlucoseRecord> = RealmList()
    public open var data: ByteArray = ByteArray(0)
}

public fun BgByPeriod.refreshRecords(): Boolean {
    val realm = Realm.getDefaultInstance()
    return realm.transaction {
        val newRecords = realm.where<BloodGlucoseRecord> {
            greaterThanOrEqualTo("date", start)
            lessThanOrEqualTo("date", duration)
        }.findAllSorted("date", RealmResults.SORT_ORDER_ASCENDING)
        val result: Boolean = if (newRecords.size == bgRecords.size && bgRecords.zip(
                newRecords).all { it.first == it.second }) {
            false
        } else {
            bgRecords.clear()
            bgRecords.addAll(newRecords)
            data = populateData()
            true
        }
        result
    }
}

public fun BgByPeriod.populateData(): ByteArray {
    val fb = FlatBufferBuilder(1024)
    val bgOffsets = IntArray(bgRecords.size)
    val dateTime = DateTime(start).withTimeAtStartOfDay()

    for (i in bgRecords.indices) {
        val record = bgRecords[i]

        bgOffsets[i] = BloodGlucose.createBloodGlucose(fb,
                record.value.toFloat(),
                record.date,
                BloodGlucoseType.SGV,
                BloodGlucoseUnit.MGDL)
    }

    val bgVector = BloodGlucoseDay.createValuesVector(fb, bgOffsets)
    val trendLine = bgRecords.createPathDataBuffer(fb, dateTime, dateTime.plusDays(1))
    val periodStatistics = bgRecords.createBloodGlucosePeriod(fb)
    val bgDay = BloodGlucoseDay.createBloodGlucoseDay(fb, periodStatistics, bgVector, trendLine)
    fb.finish(bgDay)
    return fb.sizedByteArray()
}
