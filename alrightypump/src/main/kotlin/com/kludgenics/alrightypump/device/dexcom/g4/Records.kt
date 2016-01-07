package com.kludgenics.alrightypump.device.dexcom.g4

import com.kludgenics.alrightypump.*
import com.kludgenics.alrightypump.therapy.*
import okio.Buffer
import org.joda.time.Duration
import org.joda.time.Instant
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
        final val EPOCH = Instant.parse("2009-01-01T00:00:00")

        public fun parse(buffer: Buffer): RecordPage? {
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
        fun toInstant(dexcomTimestamp: Long): Instant {
            return RecordPage.EPOCH + Duration(dexcomTimestamp * 1000)
        }

    }

    val systemSeconds: Long
    val displaySeconds: Long
    val systemTime: Instant get() = toInstant(systemSeconds)
    val displayTime: Instant get() = toInstant(displaySeconds)
}

public data class PageHeader(public val index: Long,
                             public val size: Long,
                             public val recordType: Int,
                             public val revision: Int,
                             public val pageNumber: Int,
                             public val r1: Int = 0,
                             public val r2: Int = 0,
                             public val r3: Int = 0,
                             public val j1: Int = 0,
                             public val j2: Int = 0) {
    companion object {
        public fun parse(buffer: Buffer): PageHeader {
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

public data class ManufacturingPage(public override val systemSeconds: Long,
                                    public override val displaySeconds: Long,
                                    val xml: String) : DexcomRecord {
    companion object {
        public fun parse(buffer: Buffer): ManufacturingPage {
            buffer.require(35)
            val header = PageHeader.parse(buffer)
            val systemSeconds = 0L
            val displaySeconds = 0L
            val xml = buffer.readUtf8()
            return ManufacturingPage(systemSeconds, displaySeconds, xml)
        }
    }
}

public data class ManufacturingData(public override val header: PageHeader,
                                    public override val records: List<ManufacturingPage>) : RecordPage {
    companion object {
        public fun parse(buffer: Buffer): ManufacturingData {
            val header = PageHeader.parse(buffer)
            val records = listOf(ManufacturingPage.parse(buffer))
            return ManufacturingData(header, records)
        }
    }
}

public data class EgvRecord(public val id: String,
                            public override val systemSeconds: Long,
                            public override val displaySeconds: Long,
                            public val glucose: Int,
                            public val rawGlucose: Int,
                            public val trendArrow: Int,
                            public val noise: Int,
                            public val crc: Int, public val skipped: Boolean) : DexcomRecord {
    companion object {
        public fun parse(id: String, buffer: Buffer): EgvRecord {
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

public data class EgvData(public override val header: PageHeader,
                          public override val records: List<EgvRecord>) : RecordPage {
    companion object {
        public fun parse(buffer: Buffer): EgvData {
            val header = PageHeader.parse(buffer)
            val records = ArrayList<EgvRecord>(header.size.toInt())
            for (i in 1..header.size)
                records.add(EgvRecord.parse("${header.recordType}-${header.index + i}", buffer))
            return EgvData(header, records)
        }
    }
}

public data class CalSetRecord(public override val id: String,
                               public override val systemSeconds: Long,
                               public override val displaySeconds: Long,
                               override public val slope: Double,
                               override public val intercept: Double,
                               override public val scale: Double,
                               public val unk1: Int,
                               public val unk2: Int,
                               public val unk3: Int,
                               override public val decay: Double,
                               public val nRecs: Int,
                               public val subRecords: List<CalSetRecord.CalSubRecord>) : DexcomRecord, Calibration, CalibrationRecord {
    override val time: Instant
        get() = displayTime
    override val source: String
        get() = DexcomG4.source

    public data class CalSubRecord(public val systemSecondsEntered: Long,
                                   public val systemSecondsApplied: Long,
                                   public val calBg: Int, public val calRaw: Int, public val unknown: Int) {
        companion object {
            public fun parse(buffer: Buffer): CalSubRecord {
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
        public fun parse(id: String, buffer: Buffer, recordSize: Long): CalSetRecord {
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

public data class CalData(public override val header: PageHeader,
                          public override val records: List<CalSetRecord>) : RecordPage {
    companion object {
        public fun parse(buffer: Buffer): CalData {
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

public data class InsertionRecord(public override val id: String,
                                  public override val systemSeconds: Long,
                                  public override val displaySeconds: Long,
                                  public val insertionSeconds: Int, public val insertionState: Int,
                                  public val crc: Int) : DexcomRecord, CgmInsertionRecord {
    override val time: Instant
        get() = displayTime
    override val source: String
        get() = DexcomG4.source
    override val removed: Boolean
        get() = insertionSeconds == -1
    val insertionTime: Instant get() = DexcomRecord.toInstant(insertionSeconds.toLong())

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

        public fun parse(id: String, buffer: Buffer): InsertionRecord {
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

public data class InsertionData(public override val header: PageHeader,
                                public override val records: List<InsertionRecord>) : RecordPage {
    companion object {
        public fun parse(buffer: Buffer): InsertionData {
            val header = PageHeader.parse(buffer)
            val records = ArrayList<InsertionRecord>(header.size.toInt())
            for (i in 1..header.size)
                records.add(InsertionRecord.parse("${header.recordType}-${header.index + i}".toString(), buffer))
            return InsertionData(header, records)
        }
    }
}

public data class SgvRecord(public override val systemSeconds: Long,
                            public override val displaySeconds: Long,
                            public val unfiltered: Int, public val filtered: Int,
                            public val rssi: Int, public val crc: Int) : DexcomRecord {
    companion object {
        public fun parse(buffer: Buffer): SgvRecord {
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

public data class SgvData(public override val header: PageHeader,
                          public override val records: List<SgvRecord>) : RecordPage {
    companion object {
        public fun parse(buffer: Buffer): SgvData {
            val header = PageHeader.parse(buffer)
            val records = ArrayList<SgvRecord>(header.size.toInt())
            for (i in 1..header.size)
                records.add(SgvRecord.parse(buffer))
            return SgvData(header, records)
        }
    }
}

public data class MeterRecord(public override val id: String,
                              public override val systemSeconds: Long,
                              public override val displaySeconds: Long,
                              public val meterValue: Int, public val meterSeconds: Long,
                              public val crc: Int) : SmbgRecord, DexcomRecord {
    override val time: Instant
        get() = displayTime
    override val value: GlucoseValue
        get() = BaseGlucoseValue(meterValue.toDouble(), GlucoseUnit.MGDL)
    override val manual: Boolean
        get() = true
    override val source: String
        get() = DexcomG4.source

    val meterTime: Instant get() = DexcomRecord.toInstant(meterSeconds)

    companion object {
        public fun parse(id: String, buffer: Buffer): MeterRecord {
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

public data class MeterData(public override val header: PageHeader,
                            public override val records: List<MeterRecord>) : RecordPage {
    companion object {
        public fun parse(buffer: Buffer): MeterData {
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

public data class FoodEventRecord(public override val id: String,
                                  public override val time: Instant,
                                  public override val carbohydrateGrams: Int,
                                  override val rawRecord: UserEventRecord) : EventRecord, FoodRecord, DexcomRecord by rawRecord {
    constructor(rawRecord: UserEventRecord) : this(rawRecord.id,
            DexcomRecord.toInstant(rawRecord.eventSeconds),
            rawRecord.eventValue,
            rawRecord)
}

public data class InsulinEventRecord(public override val id: String,
                                     public override val time: Instant,
                                     public override val deliveredNormal: Double,
                                     public override val rawRecord: UserEventRecord) : NormalBolusRecord, EventRecord,
        DexcomRecord by rawRecord {
    override val bolusWizard: BolusWizardRecord?
        get() = null

    constructor(rawRecord: UserEventRecord) : this(rawRecord.id,
            DexcomRecord.toInstant(rawRecord.eventSeconds),
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

public data class UserEventRecord(public override val id: String,
                                  public override val systemSeconds: Long,
                                  public override val displaySeconds: Long,
                                  public val eventType: Int, public val eventSubtype: Int,
                                  public val eventSeconds: Long, public val eventValue: Int,
                                  public val crc: Int) : DexcomRecord, EventRecord {
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

        public fun parse(id: String, buffer: Buffer): EventRecord {
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

    override val time: Instant get() = DexcomRecord.toInstant(eventSeconds)
    override val rawRecord: UserEventRecord
        get() = this
}

public data class UserEventData(public override val header: PageHeader,
                                public override val records: List<EventRecord>) :
        RecordPage {
    companion object {
        public fun parse(buffer: Buffer): UserEventData {
            val header = PageHeader.parse(buffer)
            val records = ArrayList<EventRecord>(header.size.toInt())
            for (i in 1..header.size)
                records.add(UserEventRecord.parse("${header.recordType}-${header.index + i}", buffer))
            return UserEventData(header, records)
        }
    }
}

public data class UserSettingsRecord(public override val systemSeconds: Long,
                                     public override val displaySeconds: Long,
                                     public val systemOffset: Long, public val displayOffset: Int,
                                     public val transmitterId: Long, public val enableFlags: Long,
                                     public val highAlarmValue: Int, public val highAlarmSnooze: Int,
                                     public val lowAlarmValue: Int, public val lowAlarmSnooze: Int,
                                     public val riseRateValue: Int, public val fallRateValue: Int,
                                     public val outOfRangeSnooze: Int, public val language: Int,
                                     public val alarmProfile: Int, public val setUpState: Int,
                                     public val reserved: Int, public val crc: Int) : DexcomRecord {
    companion object {
        @JvmStatic
        public fun parse(buffer: Buffer): UserSettingsRecord {
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

public data class UserSettingsData(public override val header: PageHeader,
                                   public override val records: List<UserSettingsRecord>) :
        RecordPage {
    companion object {
        public fun parse(buffer: Buffer): UserSettingsData {
            val header = PageHeader.parse(buffer)
            val records = ArrayList<UserSettingsRecord>(header.size.toInt())
            for (i in 1..header.size)
                records.add(UserSettingsRecord.parse(buffer))
            return UserSettingsData(header, records)
        }
    }
}
