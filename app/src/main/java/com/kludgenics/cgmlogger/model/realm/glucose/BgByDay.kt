package com.kludgenics.cgmlogger.model.realm.glucose

import com.google.flatbuffers.FlatBufferBuilder
import com.kludgenics.cgmlogger.extension.where
import com.kludgenics.cgmlogger.model.flatbuffers.path.*
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.annotations.PrimaryKey
import org.joda.time.DateTime
import org.joda.time.Duration

/**
 * Created by matthiasgranberry on 6/7/15.
 */
public open class BgByDay : RealmObject() {
    @PrimaryKey
    public open var day: Long = 0
    public open var bgRecords: RealmList<BloodGlucoseRecord> = RealmList()
    public open var data: ByteArray = ByteArray(0)
}

public fun BgByDay.populateData(period: Int): ByteArray {
    val fb = FlatBufferBuilder(1024)
    val bgOffsets = IntArray(bgRecords.size)
    val dateTime = DateTime(day).withTimeAtStartOfDay()

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

    val bgDay = BloodGlucoseDay.createBloodGlucoseDay(fb, 0 /*period*/, bgVector, trendLine)
    fb.finish(bgDay)
    return fb.sizedByteArray()
}
