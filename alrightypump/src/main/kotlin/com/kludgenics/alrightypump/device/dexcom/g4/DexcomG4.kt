package com.kludgenics.alrightypump.device.dexcom.g4

import com.kludgenics.alrightypump.device.ContinuousGlucoseMonitor
import com.kludgenics.alrightypump.DateTimeChangeRecord
import com.kludgenics.alrightypump.therapy.SmbgRecord
import okio.BufferedSink
import okio.BufferedSource
import org.joda.time.*
import org.joda.time.chrono.ISOChronology

/**
 * Created by matthias on 11/5/15.
 */

open class DexcomG4(private val source: BufferedSource,
                    private val sink: BufferedSink) : ContinuousGlucoseMonitor {

    companion object {
        val source: String get() = "alrightypump-Dexcom-G4-$serial"
        private var _serial = ""
        val serial: String get() = _serial
    }

    override val timeCorrectionOffset: Duration? get() {
        val receiverTime = requestTime()
        println("receiverTime:$receiverTime localTime:${LocalDateTime.now()}")
        return if (receiverTime != null)
            Duration(receiverTime.toDateTime(), DateTime.now())
        else
            null
    }

    var bleEnabled = false
    var rawEnabled = true
    override val serialNumber: String by lazy { requestSerialNumber() }

    override val cgmRecords: Sequence<DexcomCgmRecord>
        get() =
        if (rawEnabled) {
            val cals = calibrationRecords
            val egvs = egvRecords
            val sgvs = sgvRecords
            DexcomCgmSequence(egvs, sgvs, cals)
        } else
            DexcomCgmSequence(egvRecords, null, null)


    override val smbgRecords: Sequence<SmbgRecord>
        get() = DataPageIterator<MeterRecord>(RecordPage.METER_DATA).asSequence()

    override val dateTimeChangeRecords: Sequence<DateTimeChangeRecord>
        get() = throw UnsupportedOperationException()

    override val chronology: Chronology
        get() = ISOChronology.getInstance() // this should actually be constructed from time change records

    override val outOfRangeHigh: Double
        get() = 401.0

    override val outOfRangeLow: Double
        get() = 39.0

    val egvRecords: Sequence<EgvRecord> get() = DataPageIterator<EgvRecord>(RecordPage.EGV_DATA).asSequence()
    val sgvRecords: Sequence<SgvRecord> get() = DataPageIterator<SgvRecord>(RecordPage.SENSOR_DATA).asSequence()
    val eventRecords: Sequence<EventRecord> get() = DataPageIterator<EventRecord>(RecordPage.USER_EVENT_DATA).asSequence()
    val settingsRecords: Sequence<UserSettingsRecord> get() = DataPageIterator<UserSettingsRecord>(RecordPage.USER_SETTING_DATA).asSequence()
    val calibrationRecords: Sequence<CalSetRecord> get() = DataPageIterator<CalSetRecord>(RecordPage.CAL_SET).asSequence()
    override val consumableRecords: Sequence<InsertionRecord> get() = DataPageIterator<InsertionRecord>(RecordPage.INSERTION_TIME).asSequence()

    val version: String? by lazy { requestVersion() }
    val databasePartitionInfo: String? by lazy { readDatabasePartitionInfo() }

    private inner class DexcomCgmSequence(private val egvs: Sequence<EgvRecord>,
                                          private val sgvs: Sequence<SgvRecord>?,
                                          private val cals: Sequence<CalSetRecord>?) : Sequence<DexcomCgmRecord> {

        override fun iterator() = object : Iterator<DexcomCgmRecord> {
            var currentCal: CalSetRecord?
            val calIterator = cals?.iterator()
            val bgIterator: Iterator<Pair<EgvRecord, SgvRecord?>>

            init {
                val bgs: Sequence<Pair<EgvRecord, SgvRecord?>>
                if (sgvs != null && calIterator != null) {
                    val currentTime = LocalDateTime()
                    currentCal = calIterator.next()
                    while (currentCal!!.displayTime > currentTime && calIterator.hasNext())
                        currentCal = calIterator.next()
                    if (currentCal!!.displayTime > currentTime)
                        currentCal = null
                    bgs = egvs.filterNot { it.skipped }.zip(sgvs)
                } else {
                    currentCal = null
                    bgs = egvs.filterNot { it.skipped }.map { it to null }
                }
                bgIterator = bgs.iterator()
            }

            override fun next(): DexcomCgmRecord {
                val bg = bgIterator.next()
                val cal = currentCal
                if (cal != null && bg.first.displayTime < cal.displayTime && calIterator != null) {
                    while (bg.first.displayTime < cal.displayTime && calIterator.hasNext())
                        currentCal = calIterator.next()
                    val finalCal = currentCal
                    if (finalCal != null && bg.first.displayTime < finalCal.displayTime)
                        currentCal = null
                }
                return DexcomCgmRecord(bg.first, bg.second, currentCal)
            }

            override fun hasNext(): Boolean = bgIterator.hasNext()
        }
    }

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
            updateIterator()
        }

        private fun updateIterator() {
            val records = @Suppress("UNCHECKED_CAST")(readDataPages(type, pageIndex).flatMap { it.records } as List<T>)
            pageIterator = records.reversed().iterator()
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

    fun ping(): Boolean {
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

    private fun requestVersion(): String {
        val command = ReadFirmwareHeader()
        val response = commandResponse(command)
        response as XmlDexcomResponse
        return response.payloadString.utf8()
    }

    fun requestTime(): LocalDateTime? {
        val offsetResponse = commandResponse(ReadDisplayTimeOffset()) as? ReadDisplayTimeOffsetResponse
        return if (offsetResponse != null) {
            val timeResponse = commandResponse(ReadSystemTime()) as? ReadSystemTimeResponse
            if (timeResponse != null) {
                RecordPage.EPOCH + Duration((timeResponse.time.toLong()+offsetResponse.offset.toLong())*1000)
            }
            else
                null
        } else
            null
    }

    fun requestSerialNumber(): String {
        val page = readDataPageRange(RecordPage.MANUFACTURING_DATA)
        if (page != null) {
            val pages = readDataPages(RecordPage.MANUFACTURING_DATA, page.first)
            pages.filterIsInstance<ManufacturingData>().flatMap { it.records }.forEach {
                _serial = it.xml.substringAfter("SerialNumber=\"").substringBefore('"')

            }
        }
        return _serial
    }

    fun readDataPages(recordType: Int, start: Int, count: Int = 1): List<RecordPage> {
        val response = commandResponse(ReadDataPages(recordType, start, count))
        return if (response is ReadDataPagesResponse)
            response.pages
        else
            emptyList()
    }

    fun readDataPageRange(recordType: Int): Pair<Int, Int>? {
        val response = commandResponse(ReadDataPageRange(recordType))
        return if (response is ReadDataPageRangeResponse)
            response.start to response.end
        else null
    }

    fun commandResponse(command: DexcomCommand): ResponsePayload {
        val packet = DexcomG4Request(command.command, command).frame
        if (bleEnabled)
            sink.write(byteArrayOf(1, 1))
        sink.write(packet, packet.size())
        sink.emit()
        val response = DexcomG4Response(source)
        val payload = DexcomG4Response.parsePayload(command.command, response.command, response.payload)
        return payload
    }
}