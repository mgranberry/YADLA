package com.kludgenics.alrightypump.tandem

import com.kludgenics.alrightypump.*
import okio.BufferedSink
import okio.BufferedSource
import org.joda.time.Chronology
import org.joda.time.Instant
import org.joda.time.chrono.ISOChronology

/**
 * Created by matthias on 11/19/15.
 */
class TandemPump(private val source: BufferedSource, private val sink: BufferedSink) : InsulinPump, Glucometer {
    companion object {
        @JvmField final val EPOCH = Instant.parse("2008-01-01T00:00:00")
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

    public fun commandResponse(payload: TandemPayload): TandemResponse {
        val packet = TandemRequest(payload).frame
        //println("Sending: ${packet.snapshot().hex()}")
        sink.write(packet, packet.size())
        sink.emit()
        val response = TandemResponse(source)
        return response
    }

    public fun readResponse() : TandemResponse {
        return TandemResponse(source)
    }

    public fun readLogRecords(start: Int, end: Int): List<LogEvent> {
        val records = arrayListOf<LogEvent>()
        var nRead = 0
        var nRequested = 0
        for (j in start..end) {
            val packet = TandemRequest(LogEntrySeqReq(j.toLong())).frame
            sink.write(packet, packet.size())
            sink.emit()
            nRequested += 1
            // let the pipeline fill a little and then start reading as well as writing
            if (nRequested > nRead + 5 && source.request(1)) {
                val response = readResponse()
                if (response.parsedPayload is LogEvent) {
                    nRead += 1
                    records.add(response.parsedPayload)
                }
            }

        }
        while (nRead < nRequested) {
            val response = readResponse()
            if (response.parsedPayload is LogEvent) {
                nRead += 1
                records.add(response.parsedPayload)
            }
        }

        return records
    }
}