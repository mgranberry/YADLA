package com.kludgenics.cgmlogger.extension

import com.kludgenics.cgmlogger.model.flatbuffers.path.BloodGlucose
import com.kludgenics.cgmlogger.model.flatbuffers.path.BloodGlucoseDay
import com.kludgenics.cgmlogger.model.flatbuffers.path.PathDataBuffer
import com.kludgenics.cgmlogger.model.flatbuffers.path.PathDataNodeBuffer

fun PathDataBuffer.nodeSequence(): Sequence<PathDataNodeBuffer> {
    var idx: Int = 0
    val max = this.nodesLength()
    val nb = PathDataNodeBuffer()
    return generateSequence({idx = 0; if (idx < max) nodes(nb, idx) else null }) {
        idx++
        if (idx < max)
            nodes(nb, idx)
        else null
    }
}

fun PathDataBuffer.nodeIterator() = nodeSequence().iterator()

fun BloodGlucoseDay.bloodGlucoseSequence(): Sequence<BloodGlucose> {
    var idx: Int = 0
    val max = this.valuesLength()
    val b = BloodGlucose()
    return generateSequence({idx = 0; if (idx < max) values(b, idx) else null }) {
        idx++
        if (idx < max)
            values(b, idx)
        else null
    }
}

fun BloodGlucoseDay.bloodGlucoseIterator() = bloodGlucoseSequence().iterator()
