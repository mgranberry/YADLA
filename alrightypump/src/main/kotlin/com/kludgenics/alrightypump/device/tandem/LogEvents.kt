package com.kludgenics.alrightypump.device.tandem

import com.kludgenics.alrightypump.therapy.*
import okio.Buffer
import okio.BufferedSource
import org.joda.time.*

/**
 * Created by matthias on 11/21/15.
 */

interface IobEventRecord : LogEvent {
    val iob: Float
}

interface BolusEventRecord : LogEvent {
    val bolusId: Int
}

interface TempBasalEventRecord : LogEvent, TemporaryBasalRecord, TandemTherapyRecord {
    val tempRateId: Int
}

interface AlertRecord : LogEvent {
    val alertId: Int
}

interface AlarmRecord : LogEvent {
    val alarmId: Int
}

interface IdpRecord : LogEvent, TandemTherapyRecord {
    val idp: Int
}

interface SuspensionRecord : LogEvent, SuspendedBasalRecord, TandemTherapyRecord

interface TimeChangeEventRecord : LogEvent

interface TandemTherapyRecord : Record, LogEvent {
    override val time: LocalDateTime
        get() = timestamp
    override val source: String
        get() = TandemPump.source
    override val id: String? get() = seqNo.toString()
}

interface LogEvent : Payload {
    companion object {
        const val LOG_ERASED = 0
        const val TEMP_RATE_START = 2
        const val BASAL_RATE_CHANGE = 3
        const val ALERT_ACTIVATED = 4
        const val ALARM_ACTIVATED = 5
        const val ALARM_ACK = 8
        const val PUMPING_SUSPENDED = 11
        const val PUMPING_RESUMED = 12
        const val TIME_CHANGED = 13
        const val DATE_CHANGED = 14
        const val TEMP_RATE_COMPLETED = 15
        const val BG_READING_TAKEN = 16
        const val BOLUS_COMPLETED = 20
        const val BOLEX_COMPLETED = 21
        const val ALERT_CLEARED = 26
        const val ALERT_ACK = 27
        const val ALARM_CLEARED = 28
        const val CARTRIDGE_FILLED = 33
        const val USB_CONNECTED = 36
        const val USB_DISCONNECTED = 37
        const val CARB_ENTERED = 48
        const val USER_NOTIFICATON = 50
        const val BOLUS_ACTIVATED = 55
        const val IDP_MSG_2 = 57
        const val BOLEX_ACTIVATED = 59
        const val DATA_LOG_CORRUPTION = 60
        const val CONNULA_FILLED = 61
        const val TUBING_FILLED = 63
        const val BOLUS_REQ_1 = 64
        const val BOLUS_REQ_2 = 65
        const val BOLUS_REQ_3 = 66
        const val USB_ENUMERATED = 67
        const val IDP_TD_SEG = 68
        const val IDP = 69
        const val IDP_BOLUS = 70
        const val IDP_LIST = 71
        const val PARAM_PUMP_SETTINGS = 73
        const val PARAM_GLOBAL_SETTINGS = 74
        const val DAILY_BASAL = 81
        const val FACTORY_RESET = 82
        const val NEW_DAY = 90
        const val CORRECTION_DECLINED = 93
        const val PARAM_REMINDER = 96
        const val PARAM_REMINDER_SETTINGS = 97

        fun parse(source: BufferedSource): LogEvent {
            source.require(36)
            val index = source.readIntLe()
            val res = source.readShortLe().toInt() and 0xFFFF
            val logId = source.readShortLe().toInt() and 0xFFFF
            val timestamp = source.readIntLe().asLocalDateTime()
            val seqNo = source.readIntLe()
            val bogus = source.readIntLe()
            val data1 = source.readIntLe()
            val data2 = source.readIntLe()
            val data3 = source.readIntLe()
            val data4 = source.readIntLe()
            val event = BaseLogEvent(index, res, logId, timestamp, seqNo, bogus, data1, data2, data3, data4)
            return when (logId) {
                LOG_ERASED -> LogErased(event)
                TEMP_RATE_START -> TempRateStart(event)
                BASAL_RATE_CHANGE -> BasalRateChange(event)
                ALERT_ACTIVATED -> AlertActivated(event)
                ALARM_ACTIVATED -> AlarmActivated(event)
                ALARM_ACK -> AlarmAck(event)
                PUMPING_SUSPENDED -> PumpingSuspended(event)
                PUMPING_RESUMED -> PumpingResumed(event)
                TIME_CHANGED -> TimeChanged(event)
                DATE_CHANGED -> DateChanged(event)
                TEMP_RATE_COMPLETED -> TempRateCompleted(event)
                BG_READING_TAKEN -> BgReadingTaken(event)
                BOLUS_COMPLETED -> BolusCompleted(event)
                BOLEX_COMPLETED -> BolexCompleted(event)
                ALERT_CLEARED -> AlertCleared(event)
                ALERT_ACK -> AlertAck(event)
                ALARM_CLEARED -> AlarmCleared(event)
                CARTRIDGE_FILLED -> CartridgeFilled(event)
                USB_CONNECTED -> UsbConnected(event)
                USB_DISCONNECTED -> UsbDisconnected(event)
                CARB_ENTERED -> CarbEntered(event)
                USER_NOTIFICATON -> UserNotification(event)
                BOLUS_ACTIVATED -> BolusActivated(event)
                IDP_MSG_2 -> IdpMessage2(event)
                BOLEX_ACTIVATED -> BolexActivated(event)
                DATA_LOG_CORRUPTION -> DataLogCorruption(event)
                CONNULA_FILLED -> CannulaFilled(event)
                TUBING_FILLED -> TubingFilled(event)
                BOLUS_REQ_1 -> BolusRequest1(event)
                BOLUS_REQ_2 -> BolusRequest2(event)
                BOLUS_REQ_3 -> BolusRequest3(event)
                USB_ENUMERATED -> UsbEnumerated(event)
                IDP_TD_SEG -> IdpTdSeg(event)
                IDP -> Idp(event)
                IDP_BOLUS -> IdpBolus(event)
                IDP_LIST -> IdpList(event)
                PARAM_PUMP_SETTINGS -> // unimpl
                    ParamPumpSettings(event)
                PARAM_GLOBAL_SETTINGS -> // unimpl
                    ParamGlobalSettings(event)
                DAILY_BASAL -> DailyBasal(event)
                FACTORY_RESET -> FactoryReset(event)
                NEW_DAY -> NewDay(event)
                CORRECTION_DECLINED -> // unimpl
                    CorrectionDeclined(event)
                PARAM_REMINDER -> // unimpl
                    ParamReminder(event)
                PARAM_REMINDER_SETTINGS -> // unimpl
                    ParamReminderSettings(event)
                else -> UnknownLogEvent(event)
            }
        }
    }

    val index: Int
    val hours: Int // ushort
    val logId: Int // ushort

    val timestamp: LocalDateTime
    val seqNo: Int
    val bogus: Int

    val data1: Int
    val data2: Int
    val data3: Int
    val data4: Int
}

private fun LogEvent.fieldToString(field: Int): String {
    return "(int=$field, uint=${field.toLong() and 0xFFFFFFFF}, bytes=${field.asBytes()}, shorts=${field.asShorts()}, ushorts=(${field.asUnsignedShorts()}), float=${field.asFloat()}"
}

private fun Int.asLocalDateTime(): LocalDateTime = TandemPump.EPOCH.plusSeconds(this)
private fun Int.asLocalTime(): LocalTime = LocalTime(this.toLong() * 1000)
private fun Int.asLocalDate(): LocalDate = (TandemPump.EPOCH.toDateTime() + Period.days(this)).toLocalDate()

private fun Int.asString(): String {
    val buff = Buffer()
    buff.writeIntLe(this)
    return buff.snapshot().utf8().trimEnd(0.toChar())
}

private fun Int.asBytes(): List<Int> {
    val buff = Buffer()
    val list = arrayListOf<Int>()
    buff.writeIntLe(this)
    while (!buff.exhausted())
        list.add(buff.readByte().toInt() and 0xFF)
    return list
}

private fun Int.asShorts(): Pair<Short, Short> {
    val buff = Buffer()
    buff.writeIntLe(this)
    return buff.readShortLe() to buff.readShortLe()
}

private fun Int.asUnsignedShorts(): Pair<Int, Int> {
    val buff = Buffer()
    buff.writeIntLe(this)
    return (buff.readShortLe().toInt() and 0xFFFF) to (buff.readShortLe().toInt() and 0xFFFF)
}

private fun Int.asFloat(): Float {
    return java.lang.Float.intBitsToFloat(this)
}

private fun Int.asUnsigned(): Long = this.toLong() and 0xFFFFFFFF

private fun Int.toGlucoseValue(): GlucoseValue = BaseGlucoseValue(this.toDouble(), GlucoseUnit.MGDL)

data class UnknownLogEvent(val rawRecord: LogEvent) : LogEvent by rawRecord {
    override fun toString(): String {
        return "${this.javaClass.simpleName}(index=$index, hours=$hours, id=$logId, timestamp=$timestamp, seqNo=$seqNo, bogus=$bogus, data1=${fieldToString(data1)}, data2=${fieldToString(data2)}, data3=${fieldToString(data3)}, data4=${fieldToString(data4)})"
    }
}

data class BaseLogEvent(
        override val index: Int,
        override val hours: Int,
        override val logId: Int,
        override val timestamp: LocalDateTime,
        override val seqNo: Int,
        override val bogus: Int,
        override val data1: Int,
        override val data2: Int,
        override val data3: Int,
        override val data4: Int) : LogEvent {
}

data class LogErased(
        val erasedCount: Int,
        val rawRecord: LogEvent) : LogEvent by rawRecord {
    constructor (rawRecord: LogEvent) : this(erasedCount = rawRecord.data1, rawRecord = rawRecord)
}

data class TempRateStart(
        override val percent: Double,
        override val duration: Duration,
        override val tempRateId: Int,
        val rawRecord: LogEvent) : LogEvent by rawRecord, TempBasalEventRecord, TemporaryBasalStartRecord {
    override val rate: Double?
        get() = null

    constructor (rawRecord: LogEvent) : this(percent = rawRecord.data1.asFloat().toDouble(), duration = Duration(rawRecord.data2.asFloat().toLong()),
            tempRateId = rawRecord.data3.asUnsignedShorts().component2(), rawRecord = rawRecord)
}

data class BasalRateChange(
        override val rate: Double,
        val baseRate: Float,
        val maxRate: Float,
        val idp: Int,
        val changeType: Int,
        val rawRecord: LogEvent) : LogEvent by rawRecord, BasalRecord, TandemTherapyRecord {
    companion object {
        const final val MASK_SEGMENT_CHANGE = 1
        const final val MASK_PROFILE_CHANGE = 2
        const final val MASK_TEMP_START = 4
        const final val MASK_TEMP_END = 8
        const final val MASK_PUMP_SUSPEND = 16
        const final val MASK_PUMP_RESUME = 32
        const final val MASK_PUMP_SHUTDOWN = 64
    }
    constructor (rawRecord: LogEvent) : this(rate = rawRecord.data1.asFloat().toDouble(), baseRate = rawRecord.data2.asFloat(),
            maxRate = rawRecord.data3.asFloat(), idp = rawRecord.data4.asUnsignedShorts().component1(),
            changeType = rawRecord.data4.asBytes().component3(), rawRecord = rawRecord)
}

data class AlertActivated(
        override val alertId: Int,
        val rawRecord: LogEvent) : LogEvent by rawRecord, AlertRecord {
    constructor (rawRecord: LogEvent) : this(alertId = rawRecord.data1, rawRecord = rawRecord)
}

data class AlarmActivated(
        override val alarmId: Int,
        val rawRecord: LogEvent) : LogEvent by rawRecord, AlarmRecord {
    constructor (rawRecord: LogEvent) : this(alarmId = rawRecord.data1, rawRecord = rawRecord)
}

data class AlarmAck(
        override val alarmId: Int,
        val rawRecord: LogEvent) : LogEvent by rawRecord, AlarmRecord {
    constructor (rawRecord: LogEvent) : this(alarmId = rawRecord.data1, rawRecord = rawRecord)
}

data class PumpingSuspended(
        val unitsRemaining: Int,
        val rawRecord: LogEvent) : LogEvent by rawRecord, SuspensionRecord, TempBasalEventRecord {
    override val rate: Double?
        get() = 0.0
    override val percent: Double?
        get() = 0.0
    override val duration: Duration
        get() = Duration.standardDays(1)
    override val tempRateId: Int
        get() = -1

    constructor (rawRecord: LogEvent) : this(unitsRemaining = rawRecord.data2.asUnsignedShorts().component1(), rawRecord = rawRecord)
}

data class PumpingResumed(
        val unitsRemaining: Int,
        val rawRecord: LogEvent) : LogEvent by rawRecord, SuspensionRecord, TempBasalEventRecord {
    override val rate: Double?
        get() = null
    override val percent: Double
        get() = 100.0
    override val duration: Duration
        get() = Duration.ZERO
    override val tempRateId: Int
        get() = -1

    constructor (rawRecord: LogEvent) : this(unitsRemaining = rawRecord.data2.asUnsignedShorts().component1(), rawRecord = rawRecord)
}

data class TimeChanged(
        val timePrior: LocalTime,
        val timeAfter: LocalTime,
        val rawRecord: LogEvent) : LogEvent by rawRecord, TimeChangeEventRecord {
    constructor (rawRecord: LogEvent) : this(timePrior = rawRecord.data1.asLocalTime(),
            timeAfter = rawRecord.data2.asLocalTime(),
            rawRecord = rawRecord)
}

data class DateChanged(
        val datePrior: LocalDate,
        val dateAfter: LocalDate,
        val rawRecord: LogEvent) : LogEvent by rawRecord, TimeChangeEventRecord {
    constructor (rawRecord: LogEvent) : this(rawRecord = rawRecord,
            datePrior = rawRecord.data1.asLocalDate(),
            dateAfter = rawRecord.data2.asLocalDate())
}

data class TempRateCompleted(
        override val tempRateId: Int,
        val timeLeft: Duration,
        val rawRecord: LogEvent) : LogEvent by rawRecord, TempBasalEventRecord, TemporaryBasalEndRecord {
    override val percent: Double?
        get() = 100.0
    override val duration: Duration
        get() = Duration.ZERO
    override val rate: Double?
        get() = null

    constructor (rawRecord: LogEvent) : this(rawRecord = rawRecord,
            tempRateId = rawRecord.data1.asUnsignedShorts().component2(),
            timeLeft = Duration(rawRecord.data2.asUnsigned()))
}

data class BgReadingTaken(
        val bg: GlucoseValue,
        override val iob: Float,
        val targetBg: GlucoseValue,
        val isf: Int,
        val rawRecord: LogEvent) : LogEvent by rawRecord, IobEventRecord, SmbgRecord, TandemTherapyRecord {
    override val value: GlucoseValue
        get() = bg
    override val manual: Boolean
        get() = true

    constructor (rawRecord: LogEvent) : this(rawRecord = rawRecord,
            bg = rawRecord.data1.asUnsignedShorts().component1().toGlucoseValue(),
            iob = rawRecord.data2.asFloat(),
            targetBg = rawRecord.data3.asUnsignedShorts().component1().toGlucoseValue(),
            isf = rawRecord.data3.asUnsignedShorts().component2())
}

data class BolusCompleted(
        override val bolusId: Int,
        override val iob: Float,
        val insulinDelivered: Float,
        val insulinRequested: Float,
        val rawRecord: LogEvent) : LogEvent by rawRecord, IobEventRecord, BolusEventRecord {
    constructor (rawRecord: LogEvent) : this(rawRecord = rawRecord,
            bolusId = rawRecord.data1.asUnsignedShorts().component2(),
            iob = rawRecord.data2.asFloat(),
            insulinDelivered = rawRecord.data3.asFloat(),
            insulinRequested = rawRecord.data4.asFloat())
}

data class BolexCompleted(
        override val bolusId: Int,
        override val iob: Float,
        val insulinDelivered: Float,
        val insulinRequested: Float,
        val rawRecord: LogEvent) : LogEvent by rawRecord, IobEventRecord, BolusEventRecord {
    constructor (rawRecord: LogEvent) : this(rawRecord = rawRecord,
            bolusId = rawRecord.data1.asUnsignedShorts().component2(),
            iob = rawRecord.data2.asFloat(), insulinDelivered = rawRecord.data3.asFloat(),
            insulinRequested = rawRecord.data4.asFloat())
}

data class AlertCleared(
        override val alertId: Int,
        val rawRecord: LogEvent) : LogEvent by rawRecord, AlertRecord {
    constructor (rawRecord: LogEvent) : this(rawRecord = rawRecord,
            alertId = rawRecord.data1)
}

data class AlertAck(
        override val alertId: Int,
        val rawRecord: LogEvent) : LogEvent by rawRecord, AlertRecord {
    constructor (rawRecord: LogEvent) : this(rawRecord = rawRecord,
            alertId = rawRecord.data1)
}

data class AlarmCleared(
        override val alarmId: Int,
        val rawRecord: LogEvent) : LogEvent by rawRecord, AlarmRecord {
    constructor (rawRecord: LogEvent) : this(rawRecord = rawRecord,
            alarmId = rawRecord.data1)
}

data class CartridgeFilled(
        val insulinVolume: Int,
        val floatVolume: Float,
        val rawRecord: LogEvent) : LogEvent by rawRecord, CartridgeChangeRecord, TandemTherapyRecord {
    constructor (rawRecord: LogEvent) : this(rawRecord = rawRecord,
            insulinVolume = rawRecord.data1, floatVolume = rawRecord.data2.asFloat())
}

data class UsbConnected(
        val current: Float,
        val rawRecord: LogEvent) : LogEvent by rawRecord {
    constructor (rawRecord: LogEvent) : this(rawRecord = rawRecord,
            current = rawRecord.data1.asFloat())
}

data class UsbDisconnected(
        val current: Float,
        val rawRecord: LogEvent) : LogEvent by rawRecord {
    constructor (rawRecord: LogEvent) : this(rawRecord = rawRecord,
            current = rawRecord.data1.asFloat())
}

data class CarbEntered(
        val carbs: Float,
        val rawRecord: LogEvent) : LogEvent by rawRecord {
    constructor (rawRecord: LogEvent) : this(rawRecord = rawRecord,
            carbs = rawRecord.data1.asFloat())
}

data class UserNotification(
        val notificationId: Int,
        val notificationType: Int,
        val requested: Float,
        val limit: Float,
        val rawRecord: LogEvent) : LogEvent by rawRecord {
    constructor (rawRecord: LogEvent) : this(rawRecord = rawRecord,
            notificationId = rawRecord.data1.asBytes().component1(),
            notificationType = rawRecord.data1.asBytes().component2(),
            requested = rawRecord.data2.asFloat(),
            limit = rawRecord.data3.asFloat())
}

data class BolusActivated(override val bolusId: Int,
                          override val iob: Float,
                          val bolusSize: Float,
                          val rawRecord: LogEvent) : LogEvent by rawRecord, IobEventRecord, BolusEventRecord {
    constructor (rawRecord: LogEvent) : this(rawRecord = rawRecord,
            bolusId = rawRecord.data1.asUnsignedShorts().component1(),
            iob = rawRecord.data2.asFloat(),
            bolusSize = rawRecord.data3.asFloat())
}

data class IdpMessage2(
        override val idp: Int,
        val nameCont: String,
        val rawRecord: LogEvent) : LogEvent by rawRecord, IdpRecord {
    constructor (rawRecord: LogEvent) : this(rawRecord = rawRecord,
            idp = rawRecord.data1.asBytes().component1(),
            nameCont = "${rawRecord.data3.asString()}${rawRecord.data4.asString()}")
}

data class BolexActivated(
        override val bolusId: Int,
        override val iob: Float,
        val bolusSize: Float,
        val rawRecord: LogEvent) : LogEvent by rawRecord, IobEventRecord, BolusEventRecord {
    constructor (rawRecord: LogEvent) : this(rawRecord = rawRecord,
            bolusId = rawRecord.data1.asUnsignedShorts().component1(),
            iob = rawRecord.data2.asFloat(),
            bolusSize = rawRecord.data3.asFloat())
}

data class DataLogCorruption(val rawRecord: LogEvent) : LogEvent by rawRecord

data class CannulaFilled(
        val primed: Float,
        val rawRecord: LogEvent) : LogEvent by rawRecord, CannulaChangedRecord, TandemTherapyRecord {
    constructor (rawRecord: LogEvent) : this(rawRecord = rawRecord,
            primed = rawRecord.data1.asFloat())
}

data class TubingFilled(
        val primed: Float,
        val rawRecord: LogEvent) : LogEvent by rawRecord, CannulaChangedRecord, TandemTherapyRecord {
    constructor (rawRecord: LogEvent) : this(rawRecord = rawRecord,
            primed = rawRecord.data1.asFloat())
}

data class BolusRequest1(
        override val bolusId: Int,
        val bolusType: Int,
        val correction: Boolean,
        val carbs: Int,
        val bg: GlucoseValue,
        override val iob: Float,
        val carbRatio: Double,
        val rawRecord: LogEvent
) : LogEvent by rawRecord, IobEventRecord, BolusEventRecord {
    constructor (rawRecord: LogEvent) : this(rawRecord = rawRecord,
            bolusId = rawRecord.data1.asUnsignedShorts().component1(),
            bolusType = rawRecord.data1.asBytes().component3(),
            correction = rawRecord.data1.asBytes().component4() == 1,
            carbs = rawRecord.data2.asUnsignedShorts().component1(),
            bg = rawRecord.data2.asUnsignedShorts().component2().toGlucoseValue(),
            iob = rawRecord.data3.asFloat(),
            carbRatio = rawRecord.data4.asUnsigned() / 1000.0)
}

data class BolusRequest2(
        override val bolusId: Int,
        val options: Int,
        val standardPercent: Int,
        val duration: Duration,
        val isf: Int,
        val targetBg: GlucoseValue,
        val userOverride: Boolean,
        val declinedCorrection: Boolean,
        val rawRecord: LogEvent) : LogEvent by rawRecord, BolusEventRecord {
    constructor (rawRecord: LogEvent) : this(
            rawRecord = rawRecord,
            bolusId = rawRecord.data1.asUnsignedShorts().component1(),
            options = rawRecord.data1.asBytes().component3(),
            standardPercent = rawRecord.data1.asBytes().component4(),
            duration = Duration(60 * 1000 * rawRecord.data2.asUnsignedShorts().component1().toLong()),
            isf = rawRecord.data3.asUnsignedShorts().component1(),
            targetBg = rawRecord.data3.asUnsignedShorts().component2().toGlucoseValue(),
            userOverride = rawRecord.data4.asBytes().component1() == 1,
            declinedCorrection = rawRecord.data4.asBytes().component2() == 2)
}

data class BolusRequest3(
        override val bolusId: Int,
        val foodBolusRecommendation: Float,
        val correctionBolusRecommendation: Float,
        val totalBolus: Float,
        val rawRecord: LogEvent) : LogEvent by rawRecord, BolusEventRecord {
    constructor (rawRecord: LogEvent) : this(rawRecord = rawRecord,
            bolusId = rawRecord.data1.asUnsignedShorts().component1(),
            foodBolusRecommendation = rawRecord.data2.asFloat(),
            correctionBolusRecommendation = rawRecord.data3.asFloat(),
            totalBolus = rawRecord.data4.asFloat())
}

data class UsbEnumerated(
        val current: Float,
        val rawRecord: LogEvent) : LogEvent by rawRecord {
    constructor (rawRecord: LogEvent) : this(rawRecord = rawRecord,
            current = rawRecord.data1.asFloat())
}

data class IdpTdSeg(
        override val idp: Int,
        val segmentIndex: Int,
        val modificationType: Int,
        val startTime: LocalTime,
        val basalRate: Double?,
        val isf: Int?,
        val targetBg: GlucoseValue?,
        val carbRatio: Double?,
        val rawRecord: LogEvent) : LogEvent by rawRecord, IdpRecord {
    constructor (rawRecord: LogEvent) : this(rawRecord = rawRecord,
            idp = rawRecord.data1.asBytes().component1(),
            segmentIndex = rawRecord.data1.asBytes().component3(),
            modificationType = rawRecord.data1.asBytes().component4(),
            startTime = (rawRecord.data2.asUnsignedShorts().component1() * 60).asLocalTime(),
            basalRate = if (rawRecord.data1.asBytes().component2() and 1 != 0)
                rawRecord.data2.asUnsignedShorts().component2()/1000.0 else null,
            isf = if (rawRecord.data1.asBytes().component2() and 2 != 0)
                rawRecord.data3.asUnsignedShorts().component1() else null,
            targetBg = if (rawRecord.data1.asBytes().component2() and 4 != 0)
                rawRecord.data3.asUnsignedShorts().component2().toGlucoseValue() else null,
            carbRatio = if (rawRecord.data1.asBytes().component2() and 8 != 0)
                rawRecord.data4/1000.0 else null)
}

data class Idp(
        override val idp: Int,
        val op: Int,
        val sourceIdp: Int,
        val name: String,
        val rawRecord: LogEvent) : LogEvent by rawRecord, IdpRecord {

    companion object {
        const final val OP_NEW = 0
        const final val OP_COPY = 1
        const final val OP_DELETE = 2
        const final val OP_SELECT = 3
        const final val OP_RENAME = 4
        fun opToString(value: Int) = when(value) {
            OP_NEW -> "new"
            OP_COPY -> "copy"
            OP_DELETE -> "delete"
            OP_SELECT -> "select"
            OP_RENAME -> "rename"
            else -> "unknown"
        }
    }

    constructor (rawRecord: LogEvent) : this(
            idp = rawRecord.data1.asBytes().component1(),
            op = rawRecord.data1.asBytes().component2(),
            sourceIdp = rawRecord.data1.asBytes().component3(),
            name = "${rawRecord.data3.asString()}${rawRecord.data4.asString()}",
            rawRecord = rawRecord)
}

data class IdpBolus(
        override val idp: Int,
        val op: Int,
        val insulinDuration: Duration?,
        val maxBolus: Int?,
        val useCarbs: Boolean?,
        val rawRecord: LogEvent) : LogEvent by rawRecord, IdpRecord {
    constructor (rawRecord: LogEvent) : this(
            idp = rawRecord.data1.asBytes().component1(),
            op = rawRecord.data1.asBytes().component2(),
            insulinDuration = if (rawRecord.data1.asBytes().component3() and 1 != 0)
                Duration(rawRecord.data2.asUnsignedShorts().component1().toLong() * 60000) else null,
            maxBolus = if (rawRecord.data1.asBytes().component3() and 2 != 0)
                rawRecord.data2.asUnsignedShorts().component2() else null,
            useCarbs = if (rawRecord.data1.asBytes().component3() and 3 != 0)
                rawRecord.data3.asBytes().component1() == 1 else null,
            rawRecord = rawRecord
    )
}

data class IdpList(
        val numProfiles: Int,
        val slots: List<Int>,
        val rawRecord: LogEvent) : LogEvent by rawRecord, TandemTherapyRecord {
    constructor (rawRecord: LogEvent) : this(
            numProfiles = rawRecord.data1.asBytes().component1(),
            slots = listOf(*(rawRecord.data2.asBytes().toTypedArray()), *(rawRecord.data3.asBytes().toTypedArray())),
            rawRecord = rawRecord)
}

data class ParamPumpSettings(val rawRecord: LogEvent) : LogEvent by rawRecord
data class ParamGlobalSettings(val rawRecord: LogEvent) : LogEvent by rawRecord

data class DailyBasal(
        val totalDailyBasal: Float,
        val lastBasalRate: Float,
        override val iob: Float,
        val batteryPct: Int,
        val batteryMv: Int,
        val rawRecord: LogEvent) : LogEvent by rawRecord, IobEventRecord {
    constructor (rawRecord: LogEvent) : this(
            totalDailyBasal = rawRecord.data1.asFloat(),
            lastBasalRate = rawRecord.data2.asFloat(),
            iob = rawRecord.data3.asFloat(),
            batteryPct = rawRecord.data4.asBytes().component2(),
            batteryMv = rawRecord.data4.asUnsignedShorts().component2(), rawRecord = rawRecord)
}

data class FactoryReset(val rawRecord: LogEvent) : LogEvent by rawRecord

data class NewDay(
        val currentBasalRate: Float,
        val rawRecord: LogEvent) : LogEvent by rawRecord {
    constructor (rawRecord: LogEvent) : this(currentBasalRate = rawRecord.data1.asFloat(), rawRecord = rawRecord)
}

data class CorrectionDeclined(val rawRecord: LogEvent) : LogEvent by rawRecord
data class ParamReminder(val rawRecord: LogEvent) : LogEvent by rawRecord
data class ParamReminderSettings(val rawRecord: LogEvent) : LogEvent by rawRecord
