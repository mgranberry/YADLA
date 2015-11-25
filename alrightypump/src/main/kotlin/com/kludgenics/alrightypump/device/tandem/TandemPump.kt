package com.kludgenics.alrightypump.device.tandem

import com.kludgenics.alrightypump.DateTimeChangeRecord
import com.kludgenics.alrightypump.DeviceFeature
import com.kludgenics.alrightypump.Glucometer
import com.kludgenics.alrightypump.InsulinPump
import com.kludgenics.alrightypump.therapy.SmbgRecord
import okio.BufferedSink
import okio.BufferedSource
import org.joda.time.Chronology
import org.joda.time.Instant
import org.joda.time.chrono.ISOChronology
import java.util.*

/**
 * Created by matthias on 11/19/15.
 */
class TandemPump(private val source: BufferedSource, private val sink: BufferedSink) : InsulinPump, Glucometer {
    companion object {
        @JvmField final val EPOCH = Instant.parse("2008-01-01T00:00:00")
        const val ITERATION_STEP = 200
    }

    val recordCache = TreeMap<Int, LogEvent>()
    val records: Sequence<LogEvent> get() = TslimLogSequence()


    override val chronology: Chronology
        get() = ISOChronology.getInstance()
    override val supportedFeatures: Set<DeviceFeature>
        get() = setOf()
    override val firmwareVersions: List<String>
        get() = throw UnsupportedOperationException()
    override val serialNumbers: List<String>
        get() = throw UnsupportedOperationException()
    override val outOfRangeLow: Double
        get() = 19.0
    override val outOfRangeHigh: Double
        get() = 601.0
    override val dateTimeChangeRecords: Sequence<DateTimeChangeRecord>
        get() = throw UnsupportedOperationException()
    override val smbgRecords: Sequence<SmbgRecord>
        get() = throw UnsupportedOperationException()

    val bolusRecords: Sequence<TandemBolus> get () = BolusWizardAssemblingSequence()

    private data class BolusRecordHolder (var record1: BolusRequest1? = null,
                                  var record2: BolusRequest2? = null,
                                  var record3: BolusRequest3? = null,
                                  var normalActivated: BolusActivated? = null,
                                  var extendedActivated: BolexActivated? = null,
                                  var normalCompleted: BolusCompleted? = null,
                                  var extendedCompleted: BolexCompleted? = null) {

        @Suppress("USELESS_CAST") // the compiler can't figure it out
        fun toRecord(): TandemBolus? {
            val wizard = if (record1 != null && record2 != null && record3 != null)
                TandemBolusWizard(record1!!, record2!!, record3!!)
            else null
            return if (wizard != null) {
                if (normalActivated != null && extendedActivated != null)
                    TandemComboBolus(normalActivated!!, extendedActivated!!, normalCompleted, extendedCompleted, wizard)
                else if (normalActivated != null)
                    TandemNormalBolus(normalActivated!!, normalCompleted, wizard)
                else if (extendedActivated != null)
                    TandemExtendedBolus(extendedActivated!!, extendedCompleted, wizard) as TandemBolus
                else null
            } else null
        }
    }

    val logRange: IntRange = LogSizeResp(commandResponse(LogSizeReq()).payload).range

    public fun commandResponse(payload: TandemPayload): TandemResponse {
        val packet = TandemRequest(payload).frame
        //println("Sending: ${packet.snapshot().hex()}")
        sink.write(packet, packet.size())
        sink.emit()
        val response = TandemResponse(source)
        return response
    }

    public fun readResponse(): TandemResponse {
        return TandemResponse(source)
    }

    public fun readLogRecords(start: Int, end: Int): Collection<LogEvent> {
        var nRead = 0
        var nRequested = 0
        for (seqNo in start..end) {
            if (!recordCache.containsKey(seqNo)) {
                val packet = TandemRequest(LogEntrySeqReq(seqNo.toLong())).frame
                sink.write(packet, packet.size())
                sink.emit()
                nRequested += 1
                // let the pipeline fill a little and then start reading as well as writing
                if (nRequested > nRead + 5 && source.request(1)) {
                    val response = readResponse()
                    if (response.parsedPayload is LogEvent) {
                        nRead += 1
                        recordCache[response.parsedPayload.seqNo] = response.parsedPayload
                    }
                }
            }
        }
        while (nRead < nRequested) {
            val response = readResponse()
            if (response.parsedPayload is LogEvent) {
                nRead += 1
                recordCache[response.parsedPayload.seqNo] = response.parsedPayload
            }
        }
        // println("$start, $end")
        return recordCache.subMap(start, true, end, true).values
    }

    private inner class TslimLogSequence : Sequence<LogEvent> {
        override fun iterator(): Iterator<LogEvent> {
            return object : Iterator<LogEvent> {
                var eventIterator: Iterator<LogEvent>?
                var idx: Int
                val first: Int
                val last: Int

                init {
                    val log = logRange
                    first = log.start
                    last = log.last
                    idx = log.last
                    eventIterator = updateIterator()
                }

                private fun updateIterator(): Iterator<LogEvent>? {
                    idx = Math.max(idx - ITERATION_STEP, first)
                    eventIterator = readLogRecords(idx, Math.min(idx + ITERATION_STEP - 1, last)).reversed().iterator()
                    return eventIterator
                }

                override fun next(): LogEvent {
                    if (eventIterator != null) {
                        if (eventIterator!!.hasNext()) {
                            return eventIterator!!.next()
                        } else {
                            updateIterator()
                            if (eventIterator != null)
                                return eventIterator!!.next()
                        }
                    }
                    throw UnsupportedOperationException()
                }

                override fun hasNext(): Boolean {
                    if (eventIterator?.hasNext() == true)
                        return true
                    else {
                        if (idx > first)
                            updateIterator()
                        else
                            eventIterator = null
                        return eventIterator != null
                    }
                }
            }
        }
    }

    private inner class BolusWizardAssemblingSequence : Sequence<TandemBolus> {
        override fun iterator() = object: Iterator<TandemBolus> {
            val bolusRecordMap = LinkedHashMap<Int, BolusRecordHolder>()
            val recordIterator: Iterator<TandemBolus>

            init {
                recordIterator = records.filterIsInstance<BolusEventRecord>()
                        .mapNotNull {
                            val recordHolder = bolusRecordMap.getOrPut(it.bolusId, { BolusRecordHolder() })
                            when (it) {
                                is BolusRequest1 -> {
                                    recordHolder.record1 = it
                                    bolusRecordMap.remove(it.bolusId)
                                    recordHolder.toRecord()
                                }
                                is BolusRequest2 -> {
                                    recordHolder.record2 = it
                                    null
                                }
                                is BolusRequest3 -> {
                                    recordHolder.record3 = it
                                    null
                                }
                                is BolusActivated -> {
                                    recordHolder.normalActivated = it
                                    null
                                }
                                is BolexActivated -> {
                                    recordHolder.extendedActivated = it
                                    null
                                }
                                is BolusCompleted -> {
                                    recordHolder.normalCompleted = it
                                    null
                                }
                                is BolexCompleted -> {
                                    recordHolder.extendedCompleted = it
                                    null
                                }
                                else -> null
                            }
                        }.iterator()
            }

            override fun next(): TandemBolus {
                if (bolusRecordMap.size > 2)
                    println(bolusRecordMap.size)
                return recordIterator.next()
            }

            override fun hasNext(): Boolean {
                return recordIterator.hasNext()
            }
        }
    }
}
