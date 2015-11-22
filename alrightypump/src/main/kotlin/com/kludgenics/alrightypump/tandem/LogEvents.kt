package com.kludgenics.alrightypump.tandem

import com.kludgenics.alrightypump.BaseGlucoseValue
import com.kludgenics.alrightypump.GlucoseUnit
import com.kludgenics.alrightypump.GlucoseValue
import okio.Buffer
import okio.BufferedSource
import org.joda.time.*

/**
 * Created by matthias on 11/21/15.
 */

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
            val id = source.readShortLe().toInt() and 0xFFFF
            val timestamp = source.readIntLe().asInstant()
            val seqNo = source.readIntLe()
            val bogus = source.readIntLe()
            val data1 = source.readIntLe()
            val data2 = source.readIntLe()
            val data3 = source.readIntLe()
            val data4 = source.readIntLe()

            return when (id) {
                LOG_ERASED -> LogErased(index, res, id, timestamp, seqNo, bogus, data1, data2, data3, data4)
                TEMP_RATE_START -> TempRateStart(index, res, id, timestamp, seqNo, bogus, data1, data2, data3, data4)
                BASAL_RATE_CHANGE -> BasalRateChange(index, res, id, timestamp, seqNo, bogus, data1, data2, data3, data4)
                ALERT_ACTIVATED -> AlertActivated(index, res, id, timestamp, seqNo, bogus, data1, data2, data3, data4)
                ALARM_ACTIVATED -> AlarmActivated(index, res, id, timestamp, seqNo, bogus, data1, data2, data3, data4)
                ALARM_ACK -> AlarmAck(index, res, id, timestamp, seqNo, bogus, data1, data2, data3, data4)
                PUMPING_SUSPENDED -> PumpingSuspended(index, res, id, timestamp, seqNo, bogus, data1, data2, data3, data4)
                PUMPING_RESUMED -> PumpingResumed(index, res, id, timestamp, seqNo, bogus, data1, data2, data3, data4)
                TIME_CHANGED -> TimeChanged(index, res, id, timestamp, seqNo, bogus, data1, data2, data3, data4)
                DATE_CHANGED -> DateChanged(index, res, id, timestamp, seqNo, bogus, data1, data2, data3, data4)
                TEMP_RATE_COMPLETED -> TempRateCompleted(index, res, id, timestamp, seqNo, bogus, data1, data2, data3, data4)
                BG_READING_TAKEN -> BgReadingTaken(index, res, id, timestamp, seqNo, bogus, data1, data2, data3, data4)
                BOLUS_COMPLETED -> BolusCompleted(index, res, id, timestamp, seqNo, bogus, data1, data2, data3, data4)
                BOLEX_COMPLETED -> BolexCompleted(index, res, id, timestamp, seqNo, bogus, data1, data2, data3, data4)
                ALERT_CLEARED -> AlertCleared(index, res, id, timestamp, seqNo, bogus, data1, data2, data3, data4)
                ALERT_ACK -> AlertAck(index, res, id, timestamp, seqNo, bogus, data1, data2, data3, data4)
                ALARM_CLEARED -> AlarmCleared(index, res, id, timestamp, seqNo, bogus, data1, data2, data3, data4)
                CARTRIDGE_FILLED -> CartridgeFilled(index, res, id, timestamp, seqNo, bogus, data1, data2, data3, data4)
                USB_CONNECTED -> UsbConnected(index, res, id, timestamp, seqNo, bogus, data1, data2, data3, data4)
                USB_DISCONNECTED -> UsbDisconnected(index, res, id, timestamp, seqNo, bogus, data1, data2, data3, data4)
                CARB_ENTERED -> CarbEntered(index, res, id, timestamp, seqNo, bogus, data1, data2, data3, data4)
                USER_NOTIFICATON -> UserNotification(index, res, id, timestamp, seqNo, bogus, data1, data2, data3, data4)
                BOLUS_ACTIVATED -> BolusActivated(index, res, id, timestamp, seqNo, bogus, data1, data2, data3, data4)
                IDP_MSG_2 -> IdpMessage2(index, res, id, timestamp, seqNo, bogus, data1, data2, data3, data4)
                BOLEX_ACTIVATED -> BolexActivated(index, res, id, timestamp, seqNo, bogus, data1, data2, data3, data4)
                DATA_LOG_CORRUPTION -> DataLogCorruption(index, res, id, timestamp, seqNo, bogus, data1, data2, data3, data4)
                CONNULA_FILLED -> CannulaFilled(index, res, id, timestamp, seqNo, bogus, data1, data2, data3, data4)
                TUBING_FILLED -> TubingFilled(index, res, id, timestamp, seqNo, bogus, data1, data2, data3, data4)
                BOLUS_REQ_1 -> BolusRequest1(index, res, id, timestamp, seqNo, bogus, data1, data2, data3, data4)
                BOLUS_REQ_2 -> BolusRequest2(index, res, id, timestamp, seqNo, bogus, data1, data2, data3, data4)
                BOLUS_REQ_3 -> BolusRequest3(index, res, id, timestamp, seqNo, bogus, data1, data2, data3, data4)
                USB_ENUMERATED -> UsbEnumerated(index, res, id, timestamp, seqNo, bogus, data1, data2, data3, data4)
                IDP_TD_SEG -> IdpTdSeg(index, res, id, timestamp, seqNo, bogus, data1, data2, data3, data4)
                IDP -> Idp(index, res, id, timestamp, seqNo, bogus, data1, data2, data3, data4)
                IDP_BOLUS -> IdpBolus(index, res, id, timestamp, seqNo, bogus, data1, data2, data3, data4)
                IDP_LIST -> IdpList(index, res, id, timestamp, seqNo, bogus, data1, data2, data3, data4)
                PARAM_PUMP_SETTINGS -> // unimpl
                    ParamPumpSettings(index, res, id, timestamp, seqNo, bogus, data1, data2, data3, data4)
                PARAM_GLOBAL_SETTINGS -> // unimpl
                    ParamGlobalSettings(index, res, id, timestamp, seqNo, bogus, data1, data2, data3, data4)
                DAILY_BASAL -> DailyBasal(index, res, id, timestamp, seqNo, bogus, data1, data2, data3, data4)
                FACTORY_RESET -> FactoryReset(index, res, id, timestamp, seqNo, bogus, data1, data2, data3, data4)
                NEW_DAY -> NewDay(index, res, id, timestamp, seqNo, bogus, data1, data2, data3, data4)
                CORRECTION_DECLINED -> // unimpl
                    CorrectionDeclined(index, res, id, timestamp, seqNo, bogus, data1, data2, data3, data4)
                PARAM_REMINDER -> // unimpl
                    ParamReminder(index, res, id, timestamp, seqNo, bogus, data1, data2, data3, data4)
                PARAM_REMINDER_SETTINGS -> // unimpl
                    ParamReminderSettings(index, res, id, timestamp, seqNo, bogus, data1, data2, data3, data4)
                else -> UnknownLogEvent(id, index, res, id, timestamp, seqNo, bogus, data1, data2, data3, data4)
            }
        }
    }

    val index: Int
    val hours: Int // ushort
    val id: Int // ushort

    val timestamp: Instant
    val seqNo: Int
    val bogus: Int

    val data1: Int
    val data2: Int
    val data3: Int
    val data4: Int
}

interface IobRecord : LogEvent {
    val iob: Float
}

private fun LogEvent.fieldToString(field: Int): String {
    return "(int=$field, uint=${field.toLong() and 0xFFFFFFFF}, bytes=${field.asBytes()}, shorts=${field.asShorts()}, ushorts=(${field.asUnsignedShorts()}), float=${field.asFloat()}"
}

private fun Int.asInstant(): Instant = TandemPump.EPOCH + this.toLong() * 1000
private fun Int.asLocalTime(): LocalTime = LocalTime(this.toLong() * 1000)
private fun Int.asLocalDate(): LocalDate = (TandemPump.EPOCH.toDateTime() + Period.days(this)).toLocalDate()

private fun Int.asString(): String {
    val buff = Buffer()
    buff.writeIntLe(this)
    return buff.snapshot().utf8()
}

private fun Int.asBytes(): List<Int> {
    val buff = Buffer()
    val list = arrayListOf<Int>()
    buff.writeIntLe(this)
    while (!buff.exhausted())
        list.add(buff.readByte().toInt() and 0xFF)
    return list.reversed()
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

data class UnknownLogEvent(
        public val eventType: Int,
        public override val index: Int,
        public override val hours: Int,
        public override val id: Int,
        public override val timestamp: Instant,
        public override val seqNo: Int,
        public override val bogus: Int,
        public override val data1: Int,
        public override val data2: Int,
        public override val data3: Int,
        public override val data4: Int) : LogEvent {

    override fun toString(): String {
        return "${this.javaClass.simpleName}(index=$index, eventType=$eventType, hours=$hours, id=$id, timestamp=$timestamp, seqNo=$seqNo, bogus=$bogus, data1=${fieldToString(data1)}, data2=${fieldToString(data2)}, data3=${fieldToString(data3)}, data4=${fieldToString(data4)})"
    }
}

data class LogErased(
        public override val index: Int,
        public override val hours: Int,
        public override val id: Int,
        public override val timestamp: Instant,
        public override val seqNo: Int,
        public override val bogus: Int,
        public val erasedCount: Int,
        public override val data2: Int,
        public override val data3: Int,
        public override val data4: Int) : LogEvent {
    public override val data1: Int
        get() = erasedCount
}

data class TempRateStart(
        public override val index: Int,
        public override val hours: Int,
        public override val id: Int,
        public override val timestamp: Instant,
        public override val seqNo: Int,
        public override val bogus: Int,
        public override val data1: Int,
        public override val data2: Int,
        public override val data3: Int,
        public override val data4: Int,
        public val percent: Float,
        public val duration: Float,
        public val tempRateId: Int) : LogEvent {
    constructor (index: Int, hours: Int, id: Int, timestamp: Instant, seqNo: Int, bogus: Int, data1: Int,
                 data2: Int, data3: Int, data4: Int) : this(index, hours, id, timestamp, seqNo, bogus,
            data1, data2, data3, data4, percent = data1.asFloat(), duration = data2.asFloat(),
            tempRateId = data3.asUnsignedShorts().component2())
}

data class BasalRateChange(
        public override val index: Int,
        public override val hours: Int,
        public override val id: Int,
        public override val timestamp: Instant,
        public override val seqNo: Int,
        public override val bogus: Int,
        public override val data1: Int,
        public override val data2: Int,
        public override val data3: Int,
        public override val data4: Int,
        public val rate: Float,
        public val baseRate: Float,
        public val maxRate: Float,
        public val idp: Int,
        public val changeType: Int) : LogEvent {
    constructor (index: Int, hours: Int, id: Int, timestamp: Instant, seqNo: Int, bogus: Int, data1: Int,
                 data2: Int, data3: Int, data4: Int) : this(index, hours, id, timestamp, seqNo, bogus,
            data1, data2, data3, data4, rate = data1.asFloat(), baseRate = data2.asFloat(),
            maxRate = data3.asFloat(), idp = data4.asUnsignedShorts().component1(),
            changeType = data4.asBytes().component3())
}

data class AlertActivated(
        public override val index: Int,
        public override val hours: Int,
        public override val id: Int,
        public override val timestamp: Instant,
        public override val seqNo: Int,
        public override val bogus: Int,
        public override val data1: Int,
        public override val data2: Int,
        public override val data3: Int,
        public override val data4: Int,
        public val alertId: Long) : LogEvent {
    constructor (index: Int, hours: Int, id: Int, timestamp: Instant, seqNo: Int, bogus: Int, data1: Int,
                 data2: Int, data3: Int, data4: Int) : this(index, hours, id, timestamp, seqNo, bogus,
            data1, data2, data3, data4, alertId = data1.asUnsigned())
}

data class AlarmActivated(
        public override val index: Int, public override val hours: Int,
        public override val id: Int,
        public override val timestamp: Instant,
        public override val seqNo: Int,
        public override val bogus: Int,
        public override val data1: Int,
        public override val data2: Int,
        public override val data3: Int,
        public override val data4: Int,
        public val alarmId: Long) : LogEvent {
    constructor (index: Int, hours: Int, id: Int, timestamp: Instant, seqNo: Int, bogus: Int, data1: Int,
                 data2: Int, data3: Int, data4: Int) : this(index, hours, id, timestamp, seqNo, bogus,
            data1, data2, data3, data4, alarmId = data1.asUnsigned())
}

data class AlarmAck(
        public override val index: Int, public override val hours: Int,
        public override val id: Int,
        public override val timestamp: Instant,
        public override val seqNo: Int,
        public override val bogus: Int,
        public override val data1: Int,
        public override val data2: Int,
        public override val data3: Int,
        public override val data4: Int,
        public val alarmId: Long) : LogEvent {
    constructor (index: Int, hours: Int, id: Int, timestamp: Instant, seqNo: Int, bogus: Int, data1: Int,
                 data2: Int, data3: Int, data4: Int) : this(index, hours, id, timestamp, seqNo, bogus,
            data1, data2, data3, data4, alarmId = data1.asUnsigned())
}

data class PumpingSuspended(
        public override val index: Int, public override val hours: Int,
        public override val id: Int,
        public override val timestamp: Instant,
        public override val seqNo: Int,
        public override val bogus: Int,
        public override val data1: Int,
        public override val data2: Int,
        public override val data3: Int,
        public override val data4: Int,
        public val unitsRemaining: Int) : LogEvent {
    constructor (index: Int, hours: Int, id: Int, timestamp: Instant, seqNo: Int, bogus: Int, data1: Int,
                 data2: Int, data3: Int, data4: Int) : this(index, hours, id, timestamp, seqNo, bogus,
            data1, data2, data3, data4, unitsRemaining = data2.asUnsignedShorts().component1())
}

data class PumpingResumed(
        public override val index: Int, public override val hours: Int,
        public override val id: Int,
        public override val timestamp: Instant,
        public override val seqNo: Int,
        public override val bogus: Int,
        public override val data1: Int,
        public override val data2: Int,
        public override val data3: Int,
        public override val data4: Int,
        public val unitsRemaining: Int) : LogEvent {
    constructor (index: Int, hours: Int, id: Int, timestamp: Instant, seqNo: Int, bogus: Int, data1: Int,
                 data2: Int, data3: Int, data4: Int) : this(index, hours, id, timestamp, seqNo, bogus,
            data1, data2, data3, data4, unitsRemaining = data2.asUnsignedShorts().component1())
}

data class TimeChanged(
        public override val index: Int, public override val hours: Int,
        public override val id: Int,
        public override val timestamp: Instant,
        public override val seqNo: Int,
        public override val bogus: Int,
        public override val data1: Int,
        public override val data2: Int,
        public override val data3: Int,
        public override val data4: Int,
        public val timePrior: LocalTime,
        public val timeAfter: LocalTime) : LogEvent {
    constructor (index: Int, hours: Int, id: Int, timestamp: Instant, seqNo: Int, bogus: Int, data1: Int,
                 data2: Int, data3: Int, data4: Int) : this(index, hours, id, timestamp, seqNo, bogus,
            data1, data2, data3, data4, timePrior = data1.asLocalTime(),
            timeAfter = data2.asLocalTime())
}

data class DateChanged(
        public override val index: Int, public override val hours: Int,
        public override val id: Int,
        public override val timestamp: Instant,
        public override val seqNo: Int,
        public override val bogus: Int,
        public override val data1: Int,
        public override val data2: Int,
        public override val data3: Int,
        public override val data4: Int,
        public val datePrior: LocalDate,
        public val dateAfter: LocalDate) : LogEvent {
    constructor (index: Int, hours: Int, id: Int, timestamp: Instant, seqNo: Int, bogus: Int, data1: Int,
                 data2: Int, data3: Int, data4: Int) : this(index, hours, id, timestamp, seqNo, bogus,
            data1, data2, data3, data4, datePrior = data1.asLocalDate(), dateAfter = data2.asLocalDate())
}

data class TempRateCompleted(
        public override val index: Int, public override val hours: Int,
        public override val id: Int,
        public override val timestamp: Instant,
        public override val seqNo: Int,
        public override val bogus: Int,
        public override val data1: Int,
        public override val data2: Int,
        public override val data3: Int,
        public override val data4: Int,
        public val tempRateId: Int,
        public val timeLeft: Duration) : LogEvent {
    constructor (index: Int, hours: Int, id: Int, timestamp: Instant, seqNo: Int, bogus: Int, data1: Int,
                 data2: Int, data3: Int, data4: Int) : this(index, hours, id, timestamp, seqNo, bogus,
            data1, data2, data3, data4, tempRateId = data1.asUnsignedShorts().component2(),
            timeLeft = Duration(data2.asUnsigned()))
}

data class BgReadingTaken(
        public override val index: Int, public override val hours: Int,
        public override val id: Int,
        public override val timestamp: Instant,
        public override val seqNo: Int,
        public override val bogus: Int,
        public override val data1: Int,
        public override val data2: Int,
        public override val data3: Int,
        public override val data4: Int,
        public val bg: GlucoseValue,
        public override val iob: Float,
        public val targetBg: GlucoseValue,
        public val isf: Int) : LogEvent, IobRecord {
    constructor (index: Int, hours: Int, id: Int, timestamp: Instant, seqNo: Int, bogus: Int, data1: Int,
                 data2: Int, data3: Int, data4: Int) : this(index, hours, id, timestamp, seqNo, bogus,
            data1, data2, data3, data4, bg = data1.asUnsignedShorts().component1().toGlucoseValue(),
            iob = data2.asFloat(),
            targetBg = data3.asUnsignedShorts().component1().toGlucoseValue(),
            isf = data3.asUnsignedShorts().component2())
}

data class BolusCompleted(
        public override val index: Int, public override val hours: Int,
        public override val id: Int,
        public override val timestamp: Instant,
        public override val seqNo: Int,
        public override val bogus: Int,
        public override val data1: Int,
        public override val data2: Int,
        public override val data3: Int,
        public override val data4: Int,
        public val bolusId: Int,
        public override val iob: Float,
        public val insulinDelivered: Float,
        public val insulinRequested: Float) : LogEvent, IobRecord {
    constructor (index: Int, hours: Int, id: Int, timestamp: Instant, seqNo: Int, bogus: Int, data1: Int,
                 data2: Int, data3: Int, data4: Int) : this(index, hours, id, timestamp, seqNo, bogus,
            data1, data2, data3, data4, bolusId = data1.asUnsignedShorts().component2(),
            iob = data2.asFloat(), insulinDelivered = data3.asFloat(),
            insulinRequested = data4.asFloat())
}

data class BolexCompleted(
        public override val index: Int, public override val hours: Int,
        public override val id: Int,
        public override val timestamp: Instant,
        public override val seqNo: Int,
        public override val bogus: Int,
        public override val data1: Int,
        public override val data2: Int,
        public override val data3: Int,
        public override val data4: Int,
        public val bolusId: Int,
        public override val iob: Float,
        public val insulinDelivered: Float,
        public val insulinRequested: Float) : LogEvent, IobRecord  {
    constructor (index: Int, hours: Int, id: Int, timestamp: Instant, seqNo: Int, bogus: Int, data1: Int,
                 data2: Int, data3: Int, data4: Int) : this(index, hours, id, timestamp, seqNo, bogus,
            data1, data2, data3, data4, bolusId = data1.asUnsignedShorts().component2(),
            iob = data2.asFloat(), insulinDelivered = data3.asFloat(),
            insulinRequested = data4.asFloat())
}

data class AlertCleared(
        public override val index: Int, public override val hours: Int,
        public override val id: Int,
        public override val timestamp: Instant,
        public override val seqNo: Int,
        public override val bogus: Int,
        public override val data1: Int,
        public override val data2: Int,
        public override val data3: Int,
        public override val data4: Int,
        public val alertId: Long) : LogEvent {
    constructor (index: Int, hours: Int, id: Int, timestamp: Instant, seqNo: Int, bogus: Int, data1: Int,
                 data2: Int, data3: Int, data4: Int) : this(index, hours, id, timestamp, seqNo, bogus,
            data1, data2, data3, data4, alertId = data1.asUnsigned())
}

data class AlertAck(
        public override val index: Int, public override val hours: Int,
        public override val id: Int,
        public override val timestamp: Instant,
        public override val seqNo: Int,
        public override val bogus: Int,
        public override val data1: Int,
        public override val data2: Int,
        public override val data3: Int,
        public override val data4: Int,
        public val alertId: Long) : LogEvent {
    constructor (index: Int, hours: Int, id: Int, timestamp: Instant, seqNo: Int, bogus: Int, data1: Int,
                 data2: Int, data3: Int, data4: Int) : this(index, hours, id, timestamp, seqNo, bogus,
            data1, data2, data3, data4, alertId = data1.asUnsigned())
}

data class AlarmCleared(
        public override val index: Int, public override val hours: Int,
        public override val id: Int,
        public override val timestamp: Instant,
        public override val seqNo: Int,
        public override val bogus: Int,
        public override val data1: Int,
        public override val data2: Int,
        public override val data3: Int,
        public override val data4: Int,
        public val alarmId: Long) : LogEvent {
    constructor (index: Int, hours: Int, id: Int, timestamp: Instant, seqNo: Int, bogus: Int, data1: Int,
                 data2: Int, data3: Int, data4: Int) : this(index, hours, id, timestamp, seqNo, bogus,
            data1, data2, data3, data4, alarmId = data1.asUnsigned())
}

data class CartridgeFilled(
        public override val index: Int, public override val hours: Int,
        public override val id: Int,
        public override val timestamp: Instant,
        public override val seqNo: Int,
        public override val bogus: Int,
        public override val data1: Int,
        public override val data2: Int,
        public override val data3: Int,
        public override val data4: Int,
        public val insulinVolume: Long,
        public val floatVolume: Float) : LogEvent {
    constructor (index: Int, hours: Int, id: Int, timestamp: Instant, seqNo: Int, bogus: Int, data1: Int,
                 data2: Int, data3: Int, data4: Int) : this(index, hours, id, timestamp, seqNo, bogus,
            data1, data2, data3, data4, insulinVolume = data1.asUnsigned(), floatVolume = data2.asFloat())
}


data class UsbConnected(
        public override val index: Int, public override val hours: Int,
        public override val id: Int,
        public override val timestamp: Instant,
        public override val seqNo: Int,
        public override val bogus: Int,
        public override val data1: Int,
        public override val data2: Int,
        public override val data3: Int,
        public override val data4: Int,
        public val current: Float) : LogEvent {
    constructor (index: Int, hours: Int, id: Int, timestamp: Instant, seqNo: Int, bogus: Int, data1: Int,
                 data2: Int, data3: Int, data4: Int) : this(index, hours, id, timestamp, seqNo, bogus,
            data1, data2, data3, data4, current = data1.asFloat())
}

data class UsbDisconnected(
        public override val index: Int, public override val hours: Int,
        public override val id: Int,
        public override val timestamp: Instant,
        public override val seqNo: Int,
        public override val bogus: Int,
        public override val data1: Int,
        public override val data2: Int,
        public override val data3: Int,
        public override val data4: Int,
        public val current: Float) : LogEvent {
    constructor (index: Int, hours: Int, id: Int, timestamp: Instant, seqNo: Int, bogus: Int, data1: Int,
                 data2: Int, data3: Int, data4: Int) : this(index, hours, id, timestamp, seqNo, bogus,
            data1, data2, data3, data4, current = data1.asFloat())
}

data class CarbEntered(
        public override val index: Int, public override val hours: Int,
        public override val id: Int,
        public override val timestamp: Instant,
        public override val seqNo: Int,
        public override val bogus: Int,
        public override val data1: Int,
        public override val data2: Int,
        public override val data3: Int,
        public override val data4: Int,
        public val carbs: Float) : LogEvent {
    constructor (index: Int, hours: Int, id: Int, timestamp: Instant, seqNo: Int, bogus: Int, data1: Int,
                 data2: Int, data3: Int, data4: Int) : this(index, hours, id, timestamp, seqNo, bogus,
            data1, data2, data3, data4, carbs = data1.asFloat())
}

data class UserNotification(
        public override val index: Int, public override val hours: Int,
        public override val id: Int,
        public override val timestamp: Instant,
        public override val seqNo: Int,
        public override val bogus: Int,
        public override val data1: Int,
        public override val data2: Int,
        public override val data3: Int,
        public override val data4: Int,
        public val notificationId: Int,
        public val notificationType: Int,
        public val requested: Float,
        public val limit: Float) : LogEvent {
    constructor (index: Int, hours: Int, id: Int, timestamp: Instant, seqNo: Int, bogus: Int, data1: Int,
                 data2: Int, data3: Int, data4: Int) : this(index, hours, id, timestamp, seqNo, bogus,
            data1, data2, data3, data4, notificationId = data1.asBytes().component1(),
            notificationType = data1.asBytes().component2(), requested = data2.asFloat(),
            limit = data3.asFloat())
}

data class BolusActivated(
        public override val index: Int, public override val hours: Int,
        public override val id: Int,
        public override val timestamp: Instant,
        public override val seqNo: Int,
        public override val bogus: Int,
        public override val data1: Int,
        public override val data2: Int,
        public override val data3: Int,
        public override val data4: Int,
        public val bolusId: Int,
        public override val iob: Float,
        public val bolusSize: Float) : LogEvent, IobRecord  {
    constructor (index: Int, hours: Int, id: Int, timestamp: Instant, seqNo: Int, bogus: Int, data1: Int,
                 data2: Int, data3: Int, data4: Int) : this(index, hours, id, timestamp, seqNo, bogus,
            data1, data2, data3, data4, bolusId = data1.asUnsignedShorts().component1(),
            iob = data2.asFloat(), bolusSize = data3.asFloat())
}

data class IdpMessage2(
        public override val index: Int, public override val hours: Int,
        public override val id: Int,
        public override val timestamp: Instant,
        public override val seqNo: Int,
        public override val bogus: Int,
        public override val data1: Int,
        public override val data2: Int,
        public override val data3: Int,
        public override val data4: Int,
        public val idp: Int,
        public val nameCont: String) : LogEvent {
    constructor (index: Int, hours: Int, id: Int, timestamp: Instant, seqNo: Int, bogus: Int, data1: Int,
                 data2: Int, data3: Int, data4: Int) : this(index, hours, id, timestamp, seqNo, bogus,
            data1, data2, data3, data4, idp = data1.asBytes().component1(),
            nameCont = "${data3.asString()}${data4.asString()}")
}

data class BolexActivated(
        public override val index: Int, public override val hours: Int,
        public override val id: Int,
        public override val timestamp: Instant,
        public override val seqNo: Int,
        public override val bogus: Int,
        public override val data1: Int,
        public override val data2: Int,
        public override val data3: Int,
        public override val data4: Int,
        public val bolusId: Int,
        public override val iob: Float,
        public val bolusSize: Float) : LogEvent, IobRecord  {
    constructor (index: Int, hours: Int, id: Int, timestamp: Instant, seqNo: Int, bogus: Int, data1: Int,
                 data2: Int, data3: Int, data4: Int) : this(index, hours, id, timestamp, seqNo, bogus,
            data1, data2, data3, data4, bolusId = data1.asUnsignedShorts().component1(),
            iob = data2.asFloat(), bolusSize = data3.asFloat())
}


data class DataLogCorruption(
        public override val index: Int, public override val hours: Int,
        public override val id: Int,
        public override val timestamp: Instant,
        public override val seqNo: Int,
        public override val bogus: Int,
        public override val data1: Int,
        public override val data2: Int,
        public override val data3: Int,
        public override val data4: Int) : LogEvent {
}

data class CannulaFilled(
        public override val index: Int, public override val hours: Int,
        public override val id: Int,
        public override val timestamp: Instant,
        public override val seqNo: Int,
        public override val bogus: Int,
        public override val data1: Int,
        public override val data2: Int,
        public override val data3: Int,
        public override val data4: Int,
        public val primed: Float) : LogEvent {
    constructor (index: Int, hours: Int, id: Int, timestamp: Instant, seqNo: Int, bogus: Int, data1: Int,
                 data2: Int, data3: Int, data4: Int) : this(index, hours, id, timestamp, seqNo, bogus,
            data1, data2, data3, data4, primed = data1.asFloat())
}

data class TubingFilled(
        public override val index: Int, public override val hours: Int,
        public override val id: Int,
        public override val timestamp: Instant,
        public override val seqNo: Int,
        public override val bogus: Int,
        public override val data1: Int,
        public override val data2: Int,
        public override val data3: Int,
        public override val data4: Int,
        public val primed: Float) : LogEvent {
    constructor (index: Int, hours: Int, id: Int, timestamp: Instant, seqNo: Int, bogus: Int, data1: Int,
                 data2: Int, data3: Int, data4: Int) : this(index, hours, id, timestamp, seqNo, bogus,
            data1, data2, data3, data4, primed = data1.asFloat())
}

data class BolusRequest1(
        public override val index: Int, public override val hours: Int,
        public override val id: Int,
        public override val timestamp: Instant,
        public override val seqNo: Int,
        public override val bogus: Int,
        public override val data1: Int,
        public override val data2: Int,
        public override val data3: Int,
        public override val data4: Int,
        public val bolusId: Int,
        public val bolusType: Int,
        public val correction: Boolean,
        public val carbs: Int,
        public val bg: GlucoseValue,
        public override val iob: Float,
        public val carbRatio: Long
) : LogEvent, IobRecord {
    constructor (index: Int, hours: Int, id: Int, timestamp: Instant, seqNo: Int, bogus: Int, data1: Int,
                 data2: Int, data3: Int, data4: Int) : this(index, hours, id, timestamp, seqNo, bogus,
            data1, data2, data3, data4,
            bolusId = data1.asUnsignedShorts().component1(),
            bolusType = data1.asBytes().component3(),
            correction = data1.asBytes().component4() == 1,
            carbs = data2.asUnsignedShorts().component1(),
            bg = data2.asUnsignedShorts().component2().toGlucoseValue(),
            iob = data3.asFloat(),
            carbRatio = data4.asUnsigned())
}

data class BolusRequest2(
        public override val index: Int, public override val hours: Int,
        public override val id: Int,
        public override val timestamp: Instant,
        public override val seqNo: Int,
        public override val bogus: Int,
        public override val data1: Int,
        public override val data2: Int,
        public override val data3: Int,
        public override val data4: Int,
        public val bolusId: Int,
        public val options: Int,
        public val standardPercent: Int,
        public val duration: Duration,
        public val isf: Int,
        public val targetBg: GlucoseValue,
        public val userOverride: Boolean,
        public val declinedCorrection: Boolean) : LogEvent {
    constructor (index: Int, hours: Int, id: Int, timestamp: Instant, seqNo: Int, bogus: Int, data1: Int,
                 data2: Int, data3: Int, data4: Int) : this(index, hours, id, timestamp, seqNo, bogus,
            data1, data2, data3, data4,
            bolusId = data1.asUnsignedShorts().component1(),
            options = data1.asBytes().component3(),
            standardPercent = data1.asBytes().component4(),
            duration = Duration(60 * 1000 * data2.asUnsignedShorts().component1().toLong()),
            isf = data3.asUnsignedShorts().component1(),
            targetBg = data3.asUnsignedShorts().component2().toGlucoseValue(),
            userOverride = data4.asBytes().component1() == 1,
            declinedCorrection = data4.asBytes().component2() == 2
    )
}

data class BolusRequest3(
        public override val index: Int, public override val hours: Int,
        public override val id: Int,
        public override val timestamp: Instant,
        public override val seqNo: Int,
        public override val bogus: Int,
        public override val data1: Int,
        public override val data2: Int,
        public override val data3: Int,
        public override val data4: Int,
        public val bolusId: Int,
        public val foodBolusRecommendation: Float,
        public val correctionBolusRecommendation: Float,
        public val totalBolus: Float) : LogEvent {
    constructor (index: Int, hours: Int, id: Int, timestamp: Instant, seqNo: Int, bogus: Int, data1: Int,
                 data2: Int, data3: Int, data4: Int) : this(index, hours, id, timestamp, seqNo, bogus,
            data1, data2, data3, data4,
            bolusId = data1.asUnsignedShorts().component1(),
            foodBolusRecommendation = data2.asFloat(),
            correctionBolusRecommendation = data3.asFloat(),
            totalBolus = data4.asFloat())
}

data class UsbEnumerated(
        public override val index: Int, public override val hours: Int,
        public override val id: Int,
        public override val timestamp: Instant,
        public override val seqNo: Int,
        public override val bogus: Int,
        public override val data1: Int,
        public override val data2: Int,
        public override val data3: Int,
        public override val data4: Int,
        public val current: Float) : LogEvent {
    constructor (index: Int, hours: Int, id: Int, timestamp: Instant, seqNo: Int, bogus: Int, data1: Int,
                 data2: Int, data3: Int, data4: Int) : this(index, hours, id, timestamp, seqNo, bogus,
            data1, data2, data3, data4, current = data1.asFloat())
}

data class IdpTdSeg(
        public override val index: Int, public override val hours: Int,
        public override val id: Int,
        public override val timestamp: Instant,
        public override val seqNo: Int,
        public override val bogus: Int,
        public override val data1: Int,
        public override val data2: Int,
        public override val data3: Int,
        public override val data4: Int,
        public val idp: Int,
        public val segmentIndex: Int,
        public val modificationType: Int,
        public val startTime: LocalTime,
        public val basalRate: Int?,
        public val isf: Int?,
        public val targetBg: GlucoseValue?,
        public val carbRatio: Int?
) : LogEvent {
    constructor (index: Int, hours: Int, id: Int, timestamp: Instant, seqNo: Int, bogus: Int, data1: Int,
                 data2: Int, data3: Int, data4: Int) : this(index, hours, id, timestamp, seqNo, bogus,
            data1, data2, data3, data4,
            idp = data1.asBytes().component1(),
            segmentIndex = data1.asBytes().component3(),
            modificationType = data1.asBytes().component4(),
            startTime = (data2.asUnsignedShorts().component1() * 60).asLocalTime(),
            basalRate = if (data1.asBytes().component2() and 1 != 0)
                data2.asUnsignedShorts().component2() else null,
            isf = if (data1.asBytes().component2() and 2 != 0)
                data3.asUnsignedShorts().component1() else null,
            targetBg = if (data1.asBytes().component2() and 4 != 0)
                data3.asUnsignedShorts().component2().toGlucoseValue() else null,
            carbRatio = if (data1.asBytes().component2() and 8 != 0)
                data4 else null)
}

data class Idp(
        public override val index: Int, public override val hours: Int,
        public override val id: Int,
        public override val timestamp: Instant,
        public override val seqNo: Int,
        public override val bogus: Int,
        public override val data1: Int,
        public override val data2: Int,
        public override val data3: Int,
        public override val data4: Int,
        public val idp: Int,
        public val op: Int,
        public val sourceIdp: Int,
        public val name: String) : LogEvent {
    constructor (index: Int, hours: Int, id: Int, timestamp: Instant, seqNo: Int, bogus: Int, data1: Int,
                 data2: Int, data3: Int, data4: Int) : this(index, hours, id, timestamp, seqNo, bogus,
            data1, data2, data3, data4,
            idp = data1.asBytes().component1(),
            op = data1.asBytes().component2(),
            sourceIdp = data1.asBytes().component3(),
            name = "${data3.asString()}${data4.asString()}")
}

data class IdpBolus(
        public override val index: Int, public override val hours: Int,
        public override val id: Int,
        public override val timestamp: Instant,
        public override val seqNo: Int,
        public override val bogus: Int,
        public override val data1: Int,
        public override val data2: Int,
        public override val data3: Int,
        public override val data4: Int,
        public val idp: Int,
        public val op: Int,
        public val insulinDuration: Duration?,
        public val maxBolus: Int?,
        public val useCarbs: Boolean?) : LogEvent {
    constructor (index: Int, hours: Int, id: Int, timestamp: Instant, seqNo: Int, bogus: Int, data1: Int,
                 data2: Int, data3: Int, data4: Int) : this(index, hours, id, timestamp, seqNo, bogus,
            data1, data2, data3, data4,
            idp = data1.asBytes().component1(),
            op = data1.asBytes().component2(),
            insulinDuration = if (data1.asBytes().component3() and 1 != 0)
                Duration(data2.asUnsignedShorts().component1().toLong() * 60000) else null,
            maxBolus = if (data1.asBytes().component3() and 2 != 0)
                data2.asUnsignedShorts().component2() else null,
            useCarbs = if (data1.asBytes().component3() and 3 != 0)
                data3.asBytes().component1() == 1 else null
    )
}

data class IdpList(
        public override val index: Int, public override val hours: Int,
        public override val id: Int,
        public override val timestamp: Instant,
        public override val seqNo: Int,
        public override val bogus: Int,
        public override val data1: Int,
        public override val data2: Int,
        public override val data3: Int,
        public override val data4: Int,
        public val numProfiles: Int,
        public val slots: List<Int>) : LogEvent {
    constructor (index: Int, hours: Int, id: Int, timestamp: Instant, seqNo: Int, bogus: Int, data1: Int,
                 data2: Int, data3: Int, data4: Int) : this(index, hours, id, timestamp, seqNo, bogus,
            data1, data2, data3, data4,
            numProfiles = data1.asBytes().component1(),
            slots = listOf(*(data2.asBytes().toTypedArray()),*(data3.asBytes().toTypedArray())))
}

data class ParamPumpSettings(
        public override val index: Int, public override val hours: Int,
        public override val id: Int,
        public override val timestamp: Instant,
        public override val seqNo: Int,
        public override val bogus: Int,
        public override val data1: Int,
        public override val data2: Int,
        public override val data3: Int,
        public override val data4: Int) : LogEvent {

}

data class ParamGlobalSettings(
        public override val index: Int, public override val hours: Int,
        public override val id: Int,
        public override val timestamp: Instant,
        public override val seqNo: Int,
        public override val bogus: Int,
        public override val data1: Int,
        public override val data2: Int,
        public override val data3: Int,
        public override val data4: Int) : LogEvent {
}

data class DailyBasal(
        public override val index: Int, public override val hours: Int,
        public override val id: Int,
        public override val timestamp: Instant,
        public override val seqNo: Int,
        public override val bogus: Int,
        public override val data1: Int,
        public override val data2: Int,
        public override val data3: Int,
        public override val data4: Int,
        public val totalDailyBasal: Float,
        public val lastBasalRate: Float,
        public override val iob: Float,
        public val batteryPct: Int,
        public val batteryMv: Int) : LogEvent, IobRecord  {
    constructor (index: Int, hours: Int, id: Int, timestamp: Instant, seqNo: Int, bogus: Int, data1: Int,
                 data2: Int, data3: Int, data4: Int) : this(index, hours, id, timestamp, seqNo, bogus,
            data1, data2, data3, data4,
            totalDailyBasal = data1.asFloat(),
            lastBasalRate = data2.asFloat(),
            iob = data3.asFloat(),
            batteryPct = data4.asBytes().component2(),
            batteryMv = data4.asUnsignedShorts().component2())
}

data class FactoryReset(
        public override val index: Int, public override val hours: Int,
        public override val id: Int,
        public override val timestamp: Instant,
        public override val seqNo: Int,
        public override val bogus: Int,
        public override val data1: Int,
        public override val data2: Int,
        public override val data3: Int,
        public override val data4: Int) : LogEvent {
}

data class NewDay(
        public override val index: Int, public override val hours: Int,
        public override val id: Int,
        public override val timestamp: Instant,
        public override val seqNo: Int,
        public override val bogus: Int,
        public override val data1: Int,
        public override val data2: Int,
        public override val data3: Int,
        public override val data4: Int,
        public val currentBasalRate: Float) : LogEvent {
    constructor (index: Int, hours: Int, id: Int, timestamp: Instant, seqNo: Int, bogus: Int, data1: Int,
                 data2: Int, data3: Int, data4: Int) : this(index, hours, id, timestamp, seqNo, bogus,
            data1, data2, data3, data4, currentBasalRate = data1.asFloat())
}

data class CorrectionDeclined(
        public override val index: Int, public override val hours: Int,
        public override val id: Int,
        public override val timestamp: Instant,
        public override val seqNo: Int,
        public override val bogus: Int,
        public override val data1: Int,
        public override val data2: Int,
        public override val data3: Int,
        public override val data4: Int) : LogEvent {
}

data class ParamReminder(
        public override val index: Int, public override val hours: Int,
        public override val id: Int,
        public override val timestamp: Instant,
        public override val seqNo: Int,
        public override val bogus: Int,
        public override val data1: Int,
        public override val data2: Int,
        public override val data3: Int,
        public override val data4: Int) : LogEvent {
}

data class ParamReminderSettings(
        public override val index: Int, public override val hours: Int,
        public override val id: Int,
        public override val timestamp: Instant,
        public override val seqNo: Int,
        public override val bogus: Int,
        public override val data1: Int,
        public override val data2: Int,
        public override val data3: Int,
        public override val data4: Int) : LogEvent {
}
