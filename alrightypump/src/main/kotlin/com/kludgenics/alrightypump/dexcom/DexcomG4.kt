package com.kludgenics.alrightypump.dexcom

import com.kludgenics.alrightypump.ContinuousGlucoseMonitor
import okio.BufferedSink
import okio.BufferedSource
import kotlin.Iterator

/**
 * Created by matthias on 11/5/15.
 */

open class DexcomG4(private val source: BufferedSource,
               private val sink: BufferedSink): ContinuousGlucoseMonitor {

    public val version: String by lazy { requestVersion() }

    private fun requestVersion(): String {
        val command = ReadFirmwareHeader()
        val response = commandResponse<XmlDexcomResponse>(command)
        return response.payloadString.utf8()
    }

    private inner class DataPageIterator<T: Record>(private val type: Int) : Iterator<T> {
        var pageIterator: Iterator<T>? = null
        var pageIndex: Int
        val start: Int
        val end: Int
        init {
            val (startIdx, endIdx) = readDataPageRange(type)
            start = endIdx
            end = startIdx
            pageIndex = start
        }

        private fun updateIterator() {
            pageIterator = readDataPage<T>(type, pageIndex)?.records?.reversed()?.iterator()
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

    public val egvs: Iterator<EgvRecord> get() = DataPageIterator(RecordPage.EGV_DATA)
    public val sgvs: Iterator<SgvRecord> get() = DataPageIterator(RecordPage.SENSOR_DATA)
    public val events: Iterator<UserEventRecord> get() = DataPageIterator(RecordPage.USER_EVENT_DATA)
    public val settings: Iterator<UserSettingsRecord> get() = DataPageIterator(RecordPage.USER_SETTING_DATA)
    public val calibrations: Iterator<CalRecord> get() = DataPageIterator(RecordPage.CAL_SET)
    public val meters: Iterator<MeterRecord> get() = DataPageIterator(RecordPage.METER_DATA)
    public val insertions: Iterator<InsertionRecord> get() = DataPageIterator(RecordPage.INSERTION_TIME)


    public fun <R: Record> readDataPage(recordType: Int, start: Int): RecordPage<R>? {
        val result = commandResponse<ReadDataPagesResponse>(ReadDataPages(recordType, start, 1))
        val page = result.pages.first()
        // println(page.header)
        if (page.header.recordType == recordType)
            @Suppress("UNCHECKED_CAST")
            return page as RecordPage<R>
        else
            return null
    }

    public fun readDataPageRange(recordType: Int): Pair<Int, Int> {
        val range = commandResponse<ReadDataPageRangeResponse>(ReadDataPageRange(recordType))
        return range.start to range.end
    }

    public fun <R: ResponsePayload>commandResponse(command: DexcomCommand): R {
        val packet = DexcomG4Packet(command.command, command).packet.snapshot()
        // println("Writing ${packet.hex()}")
        sink.write(packet)
        val response = DexcomG4Response.parse(source, command.command)
        assert(response.valid)
        // println(response.payload)
        return response.payload as R
    }
}

