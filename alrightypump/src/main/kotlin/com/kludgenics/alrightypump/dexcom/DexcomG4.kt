package com.kludgenics.alrightypump.dexcom

import com.kludgenics.alrightypump.ContinuousGlucoseMonitor
import okio.BufferedSink
import okio.BufferedSource
import kotlin.Iterator

/**
 * Created by matthias on 11/5/15.
 */

open class DexcomG4(private val source: BufferedSource,
                    private val sink: BufferedSink) : ContinuousGlucoseMonitor {

    public val egvs: Iterator<EgvRecord> get() = DataPageIterator(RecordPage.EGV_DATA)
    public val sgvs: Iterator<SgvRecord> get() = DataPageIterator(RecordPage.SENSOR_DATA)
    public val events: Iterator<UserEventRecord> get() = DataPageIterator(RecordPage.USER_EVENT_DATA)
    public val settings: Iterator<UserSettingsRecord> get() = DataPageIterator(RecordPage.USER_SETTING_DATA)
    public val calibrations: Iterator<CalSetRecord> get() = DataPageIterator(RecordPage.CAL_SET)
    public val meters: Iterator<MeterRecord> get() = DataPageIterator(RecordPage.METER_DATA)
    public val insertions: Iterator<InsertionRecord> get() = DataPageIterator(RecordPage.INSERTION_TIME)

    public val version: String? by lazy { requestVersion() }
    public val databasePartitionInfo: String? by lazy { readDatabasePartitionInfo() }

    private inner class DataPageIterator<T>(private val type: Int) : Iterator<T> {
        var pageIterator: Iterator<T>? = null
        var pageIndex: Int
        val start: Int
        val end: Int

        init {
            val (startIdx, endIdx) = readDataPageRange(type) ?: 0 to 0
            start = endIdx
            end = startIdx
            pageIndex = start
        }

        private fun updateIterator() {
            val records = @Suppress("UNCHECKED_CAST")(readDataPages(type, pageIndex).flatMap { it.records } as List<T>)
            pageIterator =  records.reversed().iterator()
            pageIndex -= 1
        }

        override fun next(): T {
            if (pageIterator != null) {
                if (pageIterator!!.hasNext()) {
                    return pageIterator!!.next()
                } else {
                    updateIterator()
                    if (pageIterator != null)
                        return pageIterator!!.next()
                }
            }
            throw UnsupportedOperationException()
        }

        override fun hasNext(): Boolean {
            if (pageIterator?.hasNext() == true)
                return true
            else {
                if (pageIndex >= end)
                    updateIterator()
                else
                    pageIterator = null
                return pageIterator != null
            }
        }
    }

    public fun ping(): Boolean {
        val command = Ping()
        val response = commandResponse(command)
        return response is Ping
    }

    private fun readDatabasePartitionInfo(): String? {
        val command = ReadDatabasePartitionInfo()
        val response = commandResponse(command)
        return if (response is XmlDexcomResponse) {
            response.payloadString.utf8()
        } else null
    }

    private fun requestVersion(): String? {
        val command = ReadFirmwareHeader()
        val response = commandResponse(command)
        return if (response is XmlDexcomResponse)
            response.payloadString.utf8()
        else null
    }

    public fun readDataPages(recordType: Int, start: Int, count: Int = 1): List<RecordPage> {
        val response = commandResponse(ReadDataPages(recordType, start, count))
        return if (response is ReadDataPagesResponse)
            response.pages
        else
            emptyList()
    }

    public fun readDataPageRange(recordType: Int): Pair<Int, Int>? {
        val response = commandResponse(ReadDataPageRange(recordType))
        return if (response is ReadDataPageRangeResponse)
            response.start to response.end
        else null
    }

    public fun commandResponse(command: DexcomCommand): ResponsePayload {
        val packet = DexcomG4Command(command.command, command).frame
        sink.write(packet, packet.size())
        val response = DexcomG4Response(source)
        val payload = DexcomG4Response.parsePayload(command.command, response.command, response.payload)
        return payload
    }
}

