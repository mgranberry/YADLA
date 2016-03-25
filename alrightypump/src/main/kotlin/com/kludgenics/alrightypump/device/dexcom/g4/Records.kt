package com.kludgenics.alrightypump.device.dexcom.g4

import com.kludgenics.alrightypump.therapy.*
import okio.Buffer
import org.joda.time.*
import java.util.*

/**
 * Created by matthias on 11/15/15.
 */

private val Buffer.doubleLe: Double get() = java.lang.Double.longBitsToDouble(this.readLongLe())
private val Buffer.uint32Le: Long get() = this.readIntLe().toLong() and 0xFFFFFFFF
private val Buffer.int32Le: Int get() = this.readIntLe()
private val Buffer.uint16Le: Int get() = this.readShortLe().toInt() and 0xFFFF
private val Buffer.uint8: Int get() = this.readByte().toInt() and 0xFF

interface RecordPage {
    companion object {
        const final val MANUFACTURING_DATA = 0
        const final val FIRMWARE_PARAMETER_DATA = 1
        const final val PC_SOFTWARE_PARAMETER = 2
        const final val SENSOR_DATA = 3
        const final val EGV_DATA = 4
        const final val CAL_SET = 5
        const final val ABERRATION = 6
        const final val INSERTION_TIME = 7
        const final val RECEIVER_LOG_DATA = 8
        const final val RECEIVER_ERROR_DATA = 9
        const final val METER_DATA = 10
        const final val USER_EVENT_DATA = 11
        const final val USER_SETTING_DATA = 12
        final val EPOCH = LocalDateTime.parse("2009-01-01T00:00:00")

        fun parse(buffer: Buffer): RecordPage? {
            buffer.require(9)
            val type = buffer.getByte(8).toInt() and 0xFF
            return when (type) {
                MANUFACTURING_DATA -> ManufacturingData.parse(buffer)
                SENSOR_DATA -> SgvData.parse(buffer)
                EGV_DATA -> EgvData.parse(buffer)
                CAL_SET -> CalData.parse(buffer)
                INSERTION_TIME -> InsertionData.parse(buffer)
                METER_DATA -> MeterData.parse(buffer)
                USER_EVENT_DATA -> UserEventData.parse(buffer)
                USER_SETTING_DATA -> UserSettingsData.parse(buffer)
                else -> null
            }
        }
    }

    val header: PageHeader
    val records: List<DexcomRecord>
}

interface DexcomRecord {
    companion object {
        fun toLocalDateTime(dexcomTimestamp: Long): LocalDateTime {
            return RecordPage.EPOCH + Duration(dexcomTimestamp * 1000)
        }

    }

    val systemSeconds: Long
    val displaySeconds: Long
    val systemTime: LocalDateTime get() = toLocalDateTime(systemSeconds)
    val displayTime: LocalDateTime get() = toLocalDateTime(displaySeconds)
}

data class PageHeader(val index: Long,
                      val size: Long,
                      val recordType: Int,
                      val revision: Int,
                      val pageNumber: Int,
                      val r1: Int = 0,
                      val r2: Int = 0,
                      val r3: Int = 0,
                      val j1: Int = 0,
                      val j2: Int = 0) {
    companion object {
        fun parse(buffer: Buffer): PageHeader {
            val index = buffer.uint32Le
            val size = buffer.uint32Le
            val recordType = buffer.uint8
            val revision = buffer.uint8
            val pageNumber = buffer.int32Le
            val r1 = buffer.int32Le
            val r2 = buffer.int32Le
            val r3 = buffer.int32Le
            val j1 = buffer.uint8
            val j2 = buffer.uint8
            return PageHeader(index, size, recordType, revision, pageNumber, r1, r2, r3, j1, j2)
        }
    }
}

data class ManufacturingPage(override val systemSeconds: Long,
                             override val displaySeconds: Long,
                             val xml: String) : DexcomRecord {
    companion object {
        fun parse(buffer: Buffer): ManufacturingPage {
            buffer.require(35)
            val header = PageHeader.parse(buffer)
            val systemSeconds = 0L
            val displaySeconds = 0L
            val xml = buffer.readUtf8()
            return ManufacturingPage(systemSeconds, displaySeconds, xml)
        }
    }
}

data class ManufacturingData(override val header: PageHeader,
                             override val records: List<ManufacturingPage>) : RecordPage {
    companion object {
        fun parse(buffer: Buffer): ManufacturingData {
            val header = PageHeader.parse(buffer)
            val records = listOf(ManufacturingPage.parse(buffer))
            return ManufacturingData(header, records)
        }
    }
}

data class EgvRecord(val id: String,
                     override val systemSeconds: Long,
                     override val displaySeconds: Long,
                     val glucose: Int,
                     val rawGlucose: Int,
                     val trendArrow: Int,
                     val noise: Int,
                     val crc: Int, val skipped: Boolean) : DexcomRecord {
    companion object {
        fun parse(id: String, buffer: Buffer): EgvRecord {
            buffer.require(13)
            val systemSeconds = buffer.uint32Le
            val displaySeconds = buffer.uint32Le
            val rawGlucose = buffer.uint16Le
            val glucose = rawGlucose and 0x3FF
            val skipped = (rawGlucose and 0x8000 != 0)
            val trendNoise = buffer.readByte().toInt()
            val trendArrow = trendNoise and 0x0F
            val noise = (trendNoise and 0x70) shr 4
            val crc = buffer.uint16Le
            return EgvRecord(id, systemSeconds, displaySeconds, glucose, rawGlucose, trendArrow, noise, crc, skipped)
        }
    }
}

data class EgvData(override val header: PageHeader,
                   override val records: List<EgvRecord>) : RecordPage {
    companion object {
        fun parse(buffer: Buffer): EgvData {
            val header = PageHeader.parse(buffer)
            val records = ArrayList<EgvRecord>(header.size.toInt())
            for (i in 1..header.size)
                records.add(EgvRecord.parse("${header.recordType}-${header.index + i}", buffer))
            return EgvData(header, records)
        }
    }
}

data class CalSetRecord(override val id: String,
                        override val systemSeconds: Long,
                        override val displaySeconds: Long,
                        override val slope: Double,
                        override val intercept: Double,
                        override val scale: Double,
                        val unk1: Int,
                        val unk2: Int,
                        val unk3: Int,
                        override val decay: Double,
                        val nRecs: Int,
                        val subRecords: List<CalSetRecord.CalSubRecord>) : DexcomRecord, Calibration, CalibrationRecord {
    override val time: LocalDateTime
        get() = displayTime
    override val source: String
        get() = DexcomG4.source

    data class CalSubRecord(val systemSecondsEntered: Long,
                            val systemSecondsApplied: Long,
                            val calBg: Int, val calRaw: Int, val unknown: Int) {
        companion object {
            fun parse(buffer: Buffer): CalSubRecord {
                val systemSecondsEntered = buffer.uint32Le
                val calBg = buffer.int32Le
                val calRaw = buffer.int32Le
                val systemSecondsApplied = buffer.uint32Le
                val unknown = buffer.uint8
                return CalSubRecord(systemSecondsEntered, systemSecondsApplied, calBg, calRaw, unknown)
            }
        }
    }

    companion object {
        fun parse(id: String, buffer: Buffer, recordSize: Long): CalSetRecord {
            val calBuffer = Buffer()
            calBuffer.write(buffer, recordSize)
            val systemSeconds = calBuffer.uint32Le
            val displaySeconds = calBuffer.uint32Le
            val slope = calBuffer.doubleLe
            val intercept = calBuffer.doubleLe
            val scale = calBuffer.doubleLe // 32
            val unk1 = calBuffer.uint8
            val unk2 = calBuffer.uint8
            val unk3 = calBuffer.uint8
            val decay = calBuffer.doubleLe // 42
            val nRecs = calBuffer.uint8 // 43
            val subRecords = ArrayList<CalSubRecord>(nRecs)
            for (i in 1..nRecs)
                subRecords.add(CalSubRecord.parse(calBuffer))
            return CalSetRecord(id, systemSeconds, displaySeconds, slope, intercept, scale, unk1, unk2, unk3, decay, nRecs, subRecords)
        }
    }
}

data class CalData(override val header: PageHeader,
                   override val records: List<CalSetRecord>) : RecordPage {
    companion object {
        fun parse(buffer: Buffer): CalData {
            val header = PageHeader.parse(buffer)
            val records = ArrayList<CalSetRecord>(header.size.toInt())
            for (i in 1..header.size) {
                val record = CalSetRecord.parse("${header.recordType}-${header.index + i}", buffer, if (header.revision == 3) 249 else 148)
                records.add(record)
            }
            return CalData(header, records)
        }
    }
}

data class InsertionRecord(override val id: String,
                           override val systemSeconds: Long,
                           override val displaySeconds: Long,
                           val insertionSeconds: Int, val insertionState: Int,
                           val crc: Int) : DexcomRecord, CgmInsertionRecord {
    override val time: LocalDateTime
        get() = displayTime
    override val source: String
        get() = DexcomG4.source
    override val removed: Boolean
        get() = insertionSeconds == -1
    val insertionTime: LocalDateTime get() = DexcomRecord.toLocalDateTime(insertionSeconds.toLong())

    companion object {
        const final val REMOVED = 1
        const final val EXPIRED = 2
        const final val RESIDUAL_DEVIATION = 3
        const final val COUNTS_DEVIATION = 4
        const final val SECOND_SESSION = 5
        const final val OFF_TIME_LOSS = 6
        const final val STARTED = 7
        const final val BAD_TRANSMITTER = 8
        const final val MANUFACTURING_MODE = 9

        fun parse(id: String, buffer: Buffer): InsertionRecord {
            val systemSeconds = buffer.uint32Le
            val displaySeconds = buffer.uint32Le
            val insertionSeconds = buffer.int32Le
            val insertionState = buffer.uint8
            val crc = buffer.uint16Le
            return InsertionRecord(id, systemSeconds, displaySeconds, insertionSeconds, insertionState,
                    crc)
        }
    }
}

data class InsertionData(override val header: PageHeader,
                         override val records: List<InsertionRecord>) : RecordPage {
    companion object {
        fun parse(buffer: Buffer): InsertionData {
            val header = PageHeader.parse(buffer)
            val records = ArrayList<InsertionRecord>(header.size.toInt())
            for (i in 1..header.size)
                records.add(InsertionRecord.parse("${header.recordType}-${header.index + i}".toString(), buffer))
            return InsertionData(header, records)
        }
    }
}

data class SgvRecord(override val systemSeconds: Long,
                     override val displaySeconds: Long,
                     val unfiltered: Int, val filtered: Int,
                     val rssi: Int, val crc: Int) : DexcomRecord {
    companion object {
        fun parse(buffer: Buffer): SgvRecord {
            buffer.require(16)
            val systemSeconds = buffer.uint32Le
            val displaySeconds = buffer.uint32Le
            val unfiltered = buffer.int32Le
            val filtered = buffer.int32Le
            val rssi = buffer.uint16Le
            val crc = buffer.uint16Le
            return SgvRecord(systemSeconds, displaySeconds, unfiltered, filtered, rssi, crc)
        }
    }
}

data class SgvData(override val header: PageHeader,
                   override val records: List<SgvRecord>) : RecordPage {
    companion object {
        fun parse(buffer: Buffer): SgvData {
            val header = PageHeader.parse(buffer)
            val records = ArrayList<SgvRecord>(header.size.toInt())
            for (i in 1..header.size)
                records.add(SgvRecord.parse(buffer))
            return SgvData(header, records)
        }
    }
}

data class MeterRecord(override val id: String,
                       override val systemSeconds: Long,
                       override val displaySeconds: Long,
                       val meterValue: Int, val meterSeconds: Long,
                       val crc: Int) : SmbgRecord, DexcomRecord {
    override val time: LocalDateTime
        get() = displayTime
    override val value: GlucoseValue
        get() = BaseGlucoseValue(meterValue.toDouble(), GlucoseUnit.MGDL)
    override val manual: Boolean
        get() = true
    override val source: String
        get() = DexcomG4.source

    val meterTime: LocalDateTime get() = DexcomRecord.toLocalDateTime(meterSeconds)

    companion object {
        fun parse(id: String, buffer: Buffer): MeterRecord {
            buffer.require(16)
            val systemSeconds = buffer.uint32Le
            val displaySeconds = buffer.uint32Le
            val meterValue = buffer.uint16Le
            val meterSeconds = buffer.uint32Le
            val crc = buffer.uint16Le
            return MeterRecord(id, systemSeconds, displaySeconds, meterValue, meterSeconds, crc)
        }
    }
}

data class MeterData(override val header: PageHeader,
                     override val records: List<MeterRecord>) : RecordPage {
    companion object {
        fun parse(buffer: Buffer): MeterData {
            val header = PageHeader.parse(buffer)
            val records = ArrayList<MeterRecord>(header.size.toInt())
            for (i in 1..header.size)
                records.add(MeterRecord.parse("${header.recordType}-${header.index + i}", buffer))
            return MeterData(header, records)
        }
    }
}

interface EventRecord : Record, DexcomRecord {
    val rawRecord: UserEventRecord
    override val source: String
        get() = DexcomG4.source
}

data class FoodEventRecord(override val id: String,
                           override val time: LocalDateTime,
                           override val carbohydrateGrams: Int,
                           override val rawRecord: UserEventRecord) : EventRecord, FoodRecord, DexcomRecord by rawRecord {
    constructor(rawRecord: UserEventRecord) : this(rawRecord.id,
            DexcomRecord.toLocalDateTime(rawRecord.eventSeconds),
            rawRecord.eventValue,
            rawRecord)
}

data class InsulinEventRecord(override val id: String,
                              override val time: LocalDateTime,
                              override val deliveredNormal: Double,
                              override val rawRecord: UserEventRecord) : NormalBolusRecord, EventRecord,
        DexcomRecord by rawRecord {
    override val bolusWizard: BolusWizardRecord?
        get() = null

    constructor(rawRecord: UserEventRecord) : this(rawRecord.id,
            DexcomRecord.toLocalDateTime(rawRecord.eventSeconds),
            rawRecord.eventValue / 100.0,
            rawRecord)

    override val manual: Boolean
        get() = true
    override val requestedNormal: Double
        get() = deliveredNormal
    override val requestedExtended: Double?
        get() = null
    override val extendedDuration: Duration?
        get() = null
    override val expectedExtendedDuration: Duration?
        get() = null
    override val deliveredExtended: Double?
        get() = null
}

data class UserEventRecord(override val id: String,
                           override val systemSeconds: Long,
                           override val displaySeconds: Long,
                           val eventType: Int, val eventSubtype: Int,
                           val eventSeconds: Long, val eventValue: Int,
                           val crc: Int) : DexcomRecord, EventRecord {
    companion object {
        const final val EVENT_TYPE_CARBS = 1
        const final val EVENT_TYPE_INSULIN = 2
        const final val EVENT_TYPE_HEALTH = 3
        const final val EVENT_TYPE_EXERCISE = 4

        const final val EVENT_SUBTYPE_HEALTH_ILLNESS = 1
        const final val EVENT_SUBTYPE_HEALTH_STRESS = 2
        const final val EVENT_SUBTYPE_HEALTH_HIGH_SYMPTOMS = 3
        const final val EVENT_SUBTYPE_HEALTH_LOW_SYMPTOMS = 4
        const final val EVENT_SUBTYPE_HEALTH_CYCLE = 5
        const final val EVENT_SUBTYPE_HEALTH_ALCOHOL = 6

        const final val EVENT_SUBTYPE_EXERCISE_LIGHT = 1
        const final val EVENT_SUBTYPE_EXERCISE_MEDIUM = 2
        const final val EVENT_SUBTYPE_EXERCISE_HEAVY = 3

        fun parse(id: String, buffer: Buffer): EventRecord {
            buffer.require(16)
            val systemSeconds = buffer.uint32Le
            val displaySeconds = buffer.uint32Le
            val eventType = buffer.uint8
            val eventSubtype = buffer.uint8
            val eventSeconds = buffer.uint32Le
            val eventValue = buffer.int32Le
            val crc = buffer.uint16Le
            val record = UserEventRecord(id, systemSeconds, displaySeconds, eventType, eventSubtype,
                    eventSeconds, eventValue, crc)
            return when (record.eventType) {
                EVENT_TYPE_CARBS -> FoodEventRecord(record)
                EVENT_TYPE_INSULIN -> InsulinEventRecord(record)
                else -> record
            }
        }
    }

    override val time: LocalDateTime get() = DexcomRecord.toLocalDateTime(eventSeconds)
    override val rawRecord: UserEventRecord
        get() = this
}

data class UserEventData(override val header: PageHeader,
                         override val records: List<EventRecord>) :
        RecordPage {
    companion object {
        fun parse(buffer: Buffer): UserEventData {
            val header = PageHeader.parse(buffer)
            val records = ArrayList<EventRecord>(header.size.toInt())
            for (i in 1..header.size)
                records.add(UserEventRecord.parse("${header.recordType}-${header.index + i}", buffer))
            return UserEventData(header, records)
        }
    }
}

data class UserSettingsRecord(override val systemSeconds: Long,
                              override val displaySeconds: Long,
                              val systemOffset: Long, val displayOffset: Int,
                              val transmitterId: Long, val enableFlags: Long,
                              val highAlarmValue: Int, val highAlarmSnooze: Int,
                              val lowAlarmValue: Int, val lowAlarmSnooze: Int,
                              val riseRateValue: Int, val fallRateValue: Int,
                              val outOfRangeSnooze: Int, val language: Int,
                              val alarmProfile: Int, val setUpState: Int,
                              val reserved: Int, val crc: Int) : DexcomRecord {
    companion object {
        @JvmStatic fun parse(buffer: Buffer): UserSettingsRecord {
            buffer.require(48)
            val systemSeconds = buffer.uint32Le
            val displaySeconds = buffer.uint32Le
            val systemOffset = buffer.uint32Le
            val displayOffset = buffer.int32Le
            val transmitterId = buffer.uint32Le
            val enableFlags = buffer.uint32Le
            val highAlarmValue = buffer.uint16Le
            val highAlarmSnooze = buffer.uint16Le
            val lowAlarmValue = buffer.uint16Le
            val lowAlarmSnooze = buffer.uint16Le
            val riseRateValue = buffer.uint16Le
            val fallRateValue = buffer.uint16Le
            val outOfRangeSnooze = buffer.uint16Le
            val language = buffer.uint16Le
            val alarmProfile = buffer.uint8
            val setupState = buffer.uint8
            val reserved = buffer.int32Le
            val crc = buffer.uint16Le
            return UserSettingsRecord(systemSeconds, displaySeconds, systemOffset, displayOffset,
                    transmitterId, enableFlags, highAlarmValue, highAlarmSnooze, lowAlarmValue,
                    lowAlarmSnooze, riseRateValue, fallRateValue, outOfRangeSnooze, language,
                    alarmProfile, setupState, reserved, crc)
        }
    }
}

data class UserSettingsData(override val header: PageHeader,
                            override val records: List<UserSettingsRecord>) :
        RecordPage {
    companion object {
        fun parse(buffer: Buffer): UserSettingsData {
            val header = PageHeader.parse(buffer)
            val records = ArrayList<UserSettingsRecord>(header.size.toInt())
            for (i in 1..header.size)
                records.add(UserSettingsRecord.parse(buffer))
            return UserSettingsData(header, records)
        }
    }
}
