package com.kludgenics.alrightypump.dexcom

import okio.Buffer
import java.util.*

/**
 * Created by matthias on 11/15/15.
 */


interface RecordPage<E: Record> {
    companion object {
        const val MANUFACTURING_DATA = 0
        const val FIRMWARE_PARAMETER_DATA = 1
        const val PC_SOFTWARE_PARAMETER = 2
        const val SENSOR_DATA = 3
        const val EGV_DATA = 4
        const val CAL_SET = 5
        const val DEVIATION = 6
        const val INSERTION_TIME = 7
        const val RECEIVER_LOG_DATA = 8
        const val RECEIVER_ERROR_DATA = 9
        const val METER_DATA = 10
        const val USER_EVENT_DATA = 11
        const val USER_SETTING_DATA = 12

        public fun parse (buffer: Buffer): RecordPage<*>? {
            buffer.require(9)
            val type = buffer.getByte(8).toInt() and 0xFF
            return when (type) {
                // MANUFACTURING_DATA -> ManufacturingData.parse(buffer)
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
    val records: List<E>
}

interface XMLPage {
    val header: PageHeader
    val xml: String
}

interface Record {
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
            val index = buffer.readIntLe().toLong() and 0xFFFFFFFF
            val size = buffer.readIntLe().toLong() and 0xFFFFFFFF
            val recordType = buffer.readByte().toInt() and 0xFF
            val revision = buffer.readByte().toInt() and 0xFF
            val pageNumber = buffer.readIntLe()
            val r1 = buffer.readIntLe()
            val r2 = buffer.readIntLe()
            val r3 = buffer.readIntLe()
            val j1 = buffer.readByte().toInt() and 0xFF
            val j2 = buffer.readByte().toInt() and 0xFF
            return PageHeader(index, size, recordType, revision, pageNumber, r1, r2, r3, j1, j2)
        }
    }
}

public data class ManufacturingData(public override val header: PageHeader,
                                    public override val xml: String): XMLPage {
    companion object {
        public fun parse(buffer: Buffer): ManufacturingData {
            buffer.require(35)
            val header = PageHeader.parse(buffer)
            buffer.skip(7)
            val xml = buffer.readUtf8()
            return ManufacturingData(header, xml)
        }
    }
}

public data class EgvRecord(public val systemSeconds: Long, public val displaySeconds: Long,
                            public val glucose: Int, public val trendArrow: Int,
                            public val crc: Int, public val skipped: Boolean): Record {
    companion object {
        public fun parse (buffer: Buffer) : EgvRecord {
            buffer.require(13)
            val systemSeconds = buffer.readIntLe().toLong() and 0xFFFFFFFFF
            val displaySeconds = buffer.readIntLe().toLong() and 0xFFFFFFFF
            val glucose = buffer.readShortLe().toInt() and 0xFFFF
            val skipped = glucose and 0x0800 != 0 || (glucose and 0x3FF < 20)
            val trendArrow = buffer.readByte().toInt() and 0x0F
            val crc = buffer.readShortLe().toInt() and 0xFFFF
            return EgvRecord(systemSeconds, displaySeconds, glucose, trendArrow, crc, skipped)
        }
    }
}

public data class EgvData(public override val header: PageHeader,
                          public override val records: List<EgvRecord>): RecordPage<EgvRecord> {
    companion object {
        public fun parse(buffer: Buffer): EgvData {
            val header = PageHeader.parse(buffer)
            val records = ArrayList<EgvRecord>(header.size.toInt())
            for (i in 1 .. header.size)
                records.add(EgvRecord.parse(buffer))
            return EgvData(header, records)
        }
    }
}

public data class CalRecord(public val systemSeconds: Long, public val displaySeconds: Long,
                            public val slope: Double, public val intercept: Double,
                            public val scale: Double, public val decay: Double,
                            public val nRecs: Int, public val subRecords: List<CalRecord.CalSubRecord>) : Record {
    public data class CalSubRecord(public val systemSecondsEntered: Long,
                                   public val systemSecondsApplied: Long,
                                   public val calBg: Int, public val calRaw: Int) {
        companion object {
            public fun parse (buffer: Buffer): CalSubRecord {
                val systemSecondsEntered = buffer.readIntLe().toLong() and 0xFFFFFFFF
                val calBg = buffer.readIntLe()
                val calRaw = buffer.readIntLe()
                val systemSecondsApplied = buffer.readIntLe().toLong() and 0xFFFFFFFF
                return CalSubRecord(systemSecondsEntered, systemSecondsApplied, calBg, calRaw)
            }
        }
    }

    companion object {
        public fun parse (buffer: Buffer): CalRecord {
            val systemSeconds = buffer.readIntLe().toLong() and 0xFFFFFFFF
            val displaySeconds = buffer.readIntLe().toLong() and 0xFFFFFFFF
            val slope = java.lang.Double.longBitsToDouble(buffer.readLongLe())
            val intercept = java.lang.Double.longBitsToDouble(buffer.readLongLe())
            val scale = java.lang.Double.longBitsToDouble(buffer.readLongLe())
            buffer.skip(3)
            val decay = java.lang.Double.longBitsToDouble(buffer.readLongLe())
            val nRecs = buffer.readByte().toInt() and 0xFF
            val subRecords = ArrayList<CalSubRecord>(nRecs)
            for (i in 1 .. nRecs)
                subRecords.add(CalSubRecord.parse(buffer))
            return CalRecord(systemSeconds, displaySeconds, slope, intercept, scale, decay, nRecs, subRecords)
        }
    }
}

public data class CalData(public override val header: PageHeader,
                          public override val records: List<CalRecord>): RecordPage<CalRecord> {
    companion object {
        public fun parse(buffer: Buffer): CalData {
            val header = PageHeader.parse(buffer)
            val records = ArrayList<CalRecord>(header.size.toInt())
            for (i in 1 .. header.size)
                records.add(CalRecord.parse(buffer))
            return CalData(header, records)
        }
    }
}

public data class InsertionRecord(public val systemSeconds: Long, public val displaySeconds: Long,
                                  public val insertionSeconds: Long, public val insertionState: Int,
                                  public val crc: Int): Record {
    companion object {
        const val REMOVED = 1
        const val EXPIRED = 2
        const val RESIDUAL_DEVIATION = 3
        const val COUNTS_DEVIATION = 4
        const val SECOND_SESSION = 5
        const val OFF_TIME_LOSS = 6
        const val STARTED = 7
        const val BAD_TRANSMITTER = 8
        const val MANUFACTURING_MODE = 9

        public fun parse (buffer: Buffer): InsertionRecord {
            val systemSeconds = buffer.readIntLe().toLong() and 0xFFFFFFFF
            val displaySeconds = buffer.readIntLe().toLong() and 0xFFFFFFFF
            val insertionSeconds = buffer.readIntLe().toLong() and 0xFFFFFFFF
            val insertionState = buffer.readByte().toInt() and 0xFF
            val crc = buffer.readShortLe().toInt() and 0xFFFF
            return InsertionRecord(systemSeconds, displaySeconds, insertionSeconds, insertionState,
                    crc)
        }
    }
}

public data class InsertionData(public override val header: PageHeader,
                          public override val records: List<InsertionRecord>): RecordPage<InsertionRecord> {
    companion object {
        public fun parse(buffer: Buffer): InsertionData {
            val header = PageHeader.parse(buffer)
            val records = ArrayList<InsertionRecord>(header.size.toInt())
            for (i in 1 .. header.size)
                records.add(InsertionRecord.parse(buffer))
            return InsertionData(header, records)
        }
    }
}

public data class SgvRecord(public val systemSeconds: Int, public val displaySeconds: Int,
                            public val unfiltered: Int, public val filtered: Int,
                            public val rssi: Int, public val crc: Int) : Record {
    companion object {
        public fun parse (buffer: Buffer): SgvRecord {
            buffer.require(16)
            val systemSeconds = buffer.readIntLe()
            val displaySeconds = buffer.readIntLe()
            val unfiltered = buffer.readIntLe()
            val filtered = buffer.readIntLe()
            val rssi = buffer.readShortLe().toInt() and 0xFFFF
            val crc = buffer.readShortLe().toInt() and 0xFFFF
            return SgvRecord(systemSeconds, displaySeconds, unfiltered, filtered, rssi, crc)
        }
    }
}

public data class SgvData(public override val header: PageHeader,
                          public override val records: List<SgvRecord>): RecordPage<SgvRecord> {
    companion object {
        public fun parse (buffer: Buffer): SgvData {
            val header = PageHeader.parse(buffer)
            val records = ArrayList<SgvRecord>(header.size.toInt())
            for (i in 1 .. header.size)
                records.add(SgvRecord.parse(buffer))
            return SgvData(header, records)
        }
    }
}

public data class MeterRecord(public val systemSeconds: Int, public val displaySeconds: Int,
                              public val meterValue: Int, public val meterSeconds: Int,
                              public val crc: Int): Record {
    companion object {
        public fun parse(buffer: Buffer): MeterRecord {
            buffer.require(16)
            val systemSeconds = buffer.readIntLe()
            val displaySeconds = buffer.readIntLe()
            val meterValue = buffer.readShortLe().toInt() and 0xFFFF
            val meterSeconds = buffer.readIntLe()
            val crc = buffer.readShortLe().toInt() and 0xFFFF
            return MeterRecord(systemSeconds, displaySeconds, meterValue, meterSeconds, crc)
        }
    }
}

public data class MeterData(public override val header: PageHeader,
                            public override val records: List<MeterRecord>): RecordPage<MeterRecord> {
    companion object {
        public fun parse (buffer: Buffer): MeterData {
            val header = PageHeader.parse(buffer)
            val records = ArrayList<MeterRecord>(header.size.toInt())
            for (i in 1 .. header.size)
                records.add(MeterRecord.parse(buffer))
            return MeterData(header, records)
        }
    }
}

public data class UserEventRecord(public val systemSeconds: Long, public val displaySeconds: Long,
                                  public val eventType: Int, public val eventSubtype: Int,
                                  public val eventSeconds: Long, public val eventValue: Int,
                                  public val crc: Int): Record {
    companion object {
        const val EVENT_TYPE_CARBS = 1
        const val EVENT_TYPE_INSULIN = 2
        const val EVENT_TYPE_HEALTH = 3
        const val EVENT_TYPE_EXERCISE = 4

        const val EVENT_SUBTYPE_HEALTH_ILLNESS = 1
        const val EVENT_SUBTYPE_HEALTH_STRESS = 2
        const val EVENT_SUBTYPE_HEALTH_HIGH_SYMPTOMS = 3
        const val EVENT_SUBTYPE_HEALTH_LOW_SYMPTOMS = 4
        const val EVENT_SUBTYPE_HEALTH_CYCLE = 5
        const val EVENT_SUBTYPE_HEALTH_ALCOHOL = 6

        const val EVENT_SUBTYPE_EXERCISE_LIGHT = 1
        const val EVENT_SUBTYPE_EXERCISE_MEDIUM = 2
        const val EVENT_SUBTYPE_EXERCISE_HEAVY = 3

        public fun parse (buffer: Buffer): UserEventRecord {
            buffer.require(16)
            val systemSeconds = buffer.readIntLe().toLong() and 0xFFFFFFFF
            val displaySeconds = buffer.readIntLe().toLong() and 0xFFFFFFFF
            val eventType = buffer.readByte().toInt() and 0xFF
            val eventSubtype = buffer.readByte().toInt() and 0xFF
            val eventSeconds = buffer.readIntLe().toLong() and 0xFFFFFFFF
            val eventValue = buffer.readIntLe()
            val crc = buffer.readShortLe().toInt() and 0xFFFF
            return UserEventRecord(systemSeconds, displaySeconds, eventType, eventSubtype,
                    eventSeconds, eventValue, crc)
        }
    }
}

public data class UserEventData(public override val header: PageHeader,
                                public override val records: List<UserEventRecord>):
        RecordPage<UserEventRecord> {
    companion object {
        public fun parse (buffer: Buffer): UserEventData {
            val header = PageHeader.parse(buffer)
            val records = ArrayList<UserEventRecord>(header.size.toInt())
            for (i in 1 .. header.size)
                records.add(UserEventRecord.parse(buffer))
            return UserEventData(header, records)
        }
    }
}
public data class UserSettingsRecord(public val systemSeconds: Long, public val displaySeconds: Long,
                                     public val systemOffset: Long, public val displayOffset: Long,
                                     public val transmitterId: Int, public val enableFlags: Int,
                                     public val highAlarmValue: Int, public val highAlarmSnooze: Int,
                                     public val lowAlarmValue: Int, public val lowAlarmSnooze: Int,
                                     public val riseRateValue: Int, public val fallRateValue: Int,
                                     public val outOfRangeSnooze: Int, public val language: Int,
                                     public val alarmProfile: Int, public val setUpState: Int,
                                     public val reserved: Int, public val crc: Int): Record {
    companion object {
        public fun parse (buffer: Buffer): UserSettingsRecord {
            buffer.require(48)
            val systemSeconds = buffer.readIntLe().toLong() and 0xFFFFFFFFF
            val displaySeconds = buffer.readIntLe().toLong() and 0xFFFFFFFF
            val systemOffset = buffer.readIntLe().toLong() and 0xFFFFFFFF
            val displayOffset = buffer.readIntLe().toLong() and 0xFFFFFFFF
            val transmitterId =  buffer.readIntLe()
            val enableFlags = buffer.readShortLe().toInt() and 0xFFFF
            val highAlarmValue = buffer.readShortLe().toInt() and 0xFFFF
            val highAlarmSnooze = buffer.readShortLe().toInt() and 0xFFFF
            val lowAlarmValue = buffer.readShortLe().toInt() and 0xFFFF
            val lowAlarmSnooze = buffer.readShortLe().toInt() and 0xFFFF
            val riseRateValue = buffer.readShortLe().toInt() and 0xFFFF
            val fallRateValue = buffer.readShortLe().toInt() and 0xFFFF
            val outOfRangeSnooze = buffer.readShortLe().toInt() and 0xFFFF
            val language = buffer.readShortLe().toInt() and 0xFFFF
            val alarmProfile = buffer.readByte().toInt() and 0xFF
            val setupState = buffer.readByte().toInt() and 0xFF
            val reserved = buffer.readIntLe()
            val crc = buffer.readShortLe().toInt() and 0xFFFF
            return UserSettingsRecord(systemSeconds, displaySeconds, systemOffset, displayOffset,
                    transmitterId, enableFlags, highAlarmValue, highAlarmSnooze, lowAlarmValue,
                    lowAlarmSnooze, riseRateValue, fallRateValue, outOfRangeSnooze, language,
                    alarmProfile, setupState, reserved, crc)
        }
    }
}

public data class UserSettingsData(public override val header: PageHeader,
                                   public override val records: List<UserSettingsRecord>):
        RecordPage<UserSettingsRecord> {
    companion object {
        public fun parse (buffer: Buffer): UserSettingsData {
            val header = PageHeader.parse(buffer)
            val records = ArrayList<UserSettingsRecord>(header.size.toInt())
            for (i in 1 .. header.size)
                records.add(UserSettingsRecord.parse(buffer))
            return UserSettingsData(header, records)
        }
    }
}
