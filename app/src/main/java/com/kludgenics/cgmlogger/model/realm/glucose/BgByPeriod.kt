package com.kludgenics.cgmlogger.model.realm.glucose

import com.google.flatbuffers.FlatBufferBuilder
import com.kludgenics.cgmlogger.extension.transaction
import com.kludgenics.cgmlogger.extension.where
import com.kludgenics.cgmlogger.model.flatbuffers.path.*
import io.realm.*
import io.realm.annotations.PrimaryKey
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.joda.time.DateTime
import org.joda.time.Duration

/**
 * Created by matthiasgranberry on 6/7/15.
 */
public open class BgByPeriod : RealmObject() {
    public open var start = 0L
    public open var duration = 86400000L
    public open var bgRecords: RealmList<BloodGlucoseRecord> = RealmList()
    public open var data: ByteArray = ByteArray(0)
}

public fun BgByPeriod.populateData(): ByteArray {
    val fb = FlatBufferBuilder(1024)
    val bgOffsets = IntArray(bgRecords.size)
    val dateTime = DateTime(start).withTimeAtStartOfDay()

    for (i in bgRecords.indices) {
        val record = bgRecords[i]

        bgOffsets[i] = BloodGlucose.createBloodGlucose(fb,
                record.value.toFloat(),
                record.date.time,
                BloodGlucoseType.SGV,
                BloodGlucoseUnit.MGDL)
    }
    val periodStatistics = bgRecords.createBloodGlucosePeriod(fb)
    val bgVector = BloodGlucoseDay.createValuesVector(fb, bgOffsets)
    val trendLine = bgRecords.createPathDataBuffer(fb, dateTime, dateTime.plusDays(1))
    val bgDay = BloodGlucoseDay.createBloodGlucoseDay(fb, periodStatistics, bgVector, trendLine)
    fb.finish(bgDay)
    data = fb.sizedByteArray()
    return data
}
