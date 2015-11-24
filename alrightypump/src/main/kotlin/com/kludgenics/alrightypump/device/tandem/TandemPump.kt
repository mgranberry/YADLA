package com.kludgenics.alrightypump.device.tandem

import com.kludgenics.alrightypump.*
import com.kludgenics.alrightypump.device.dexcom.CalSetRecord
import com.kludgenics.alrightypump.device.dexcom.DexcomCgmRecord
import com.kludgenics.alrightypump.device.dexcom.EgvRecord
import com.kludgenics.alrightypump.device.dexcom.SgvRecord
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

    inner class TslimLogSequence() : Sequence<LogEvent> {
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
                        if (idx >= first)
                            updateIterator()
                        else
                            eventIterator = null
                        return eventIterator != null
                    }
                }
            }
        }
    }

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
}
