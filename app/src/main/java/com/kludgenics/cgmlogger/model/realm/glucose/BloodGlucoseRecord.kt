package com.kludgenics.cgmlogger.model.realm.glucose

import com.google.flatbuffers.FlatBufferBuilder
import com.kludgenics.cgmlogger.app.util.PathParser
import com.kludgenics.cgmlogger.model.flatbuffers.path.BloodGlucosePeriod
import com.kludgenics.cgmlogger.model.math.bgi.Bgi
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import org.joda.time.DateTime
import org.joda.time.Duration

/**
 * Created by matthiasgranberry on 5/24/15.
 */
public open class BloodGlucoseRecord( public open var value: Double = 0.0,
                                      public open var date: Long = 0,
                                      @Required
                                      public open var unit: String = "",
                                      @PrimaryKey public open var id: String = ""): RealmObject()


public fun RealmList<BloodGlucoseRecord>.createPathDataBuffer(builder: FlatBufferBuilder,
                                                              start: DateTime,
                                                              end: DateTime,
                                                              scaleX: Int = 240,
                                                              scaleY: Int = 400): Int {
    val periodLength = Duration(start, end).millis
    val startMillis = start.millis
    val pathString = if (size > 0) {
        buildString {
            val initial = this@createPathDataBuffer[0]
            val initialX = ((initial.date - startMillis).toFloat() / periodLength) * scaleX
            val initialY = scaleY - initial.value
            append("M$initialX,${initialY}L")
            this@createPathDataBuffer.forEach {
                val xOffset = ((it.date - startMillis).toFloat() / periodLength) * scaleX
                val yOffset = scaleY - it.value
                append("$xOffset,$yOffset ")
            }
        }
    } else {
        "M0,0"
    }
    return PathParser.populateFlatBufferFromPathData(builder, pathString)
}

public fun RealmList<BloodGlucoseRecord>.createBloodGlucosePeriod(builder: FlatBufferBuilder,
                                                                  lowThreshold: Double = 80.0,
                                                                  highThreshold: Double = 180.0): Int {
    val average: Float = where().average("value").toFloat()
    val median = this[size / 2].value.toFloat()
    val rhMax = Bgi.rh(where().findAllSorted("value", false).first().value).toFloat()
    val rlMax = Bgi.rl(where().findAllSorted("value", true).first().value).toFloat()
    val hbgi = Bgi.hbgi(this).toFloat()
    val lbgi = Bgi.lbgi(this).toFloat()
    val adrr = Bgi.adrr(this).toFloat()
    val stdDev = Math.sqrt(this.map { val d = it.value - average; d*d }.average()).toFloat()
    val countLow = where().lessThanOrEqualTo("value", lowThreshold).findAll().size
    val countHigh = where().greaterThanOrEqualTo("value", highThreshold).findAll().size

    return BloodGlucosePeriod.createBloodGlucosePeriod(builder, average, median, rhMax,
            rlMax, hbgi, lbgi, adrr, stdDev, countLow, countHigh, lowThreshold.toFloat(),
            highThreshold.toFloat())
}
