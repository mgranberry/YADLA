package com.kludgenics.alrightypump.device.tandem

import com.kludgenics.alrightypump.therapy.GlucoseValue
import okio.Buffer
import okio.BufferedSource
import org.joda.time.Duration
import org.joda.time.Instant
import org.joda.time.LocalTime

/**
 * Created by matthias on 11/20/15.
 */
interface Payload {
    companion object {
        fun parse(command: Int, source: BufferedSource): Payload? {
            return when(command) {
                125 -> LogEvent.parse(source)
                123 -> Ack(source)
                else -> null
            }
        }
    }
}

class TandemResponse(source: BufferedSource) : TandemFrame() {
    override val header: Buffer get() {
        val buff = Buffer()
        frame.copyTo(buff, 0, headerLength)
        return buff
    }
    override val payload: Buffer
    public val parsedPayload: Payload?

    override val footer: Buffer
    override val expectedChecksum: Int
    override val frame: Buffer
    override val payloadLength: Long
    public val timestamp: Instant
    public val command: Int
    override public val valid: Boolean
        get() = (expectedChecksum == calculatedChecksum)

    init {
        frame = Buffer()
        source.require(9)
        source.skip(source.indexOf(sync.toByte()))
        source.require(9)
        frame.writeByte(source.readByte().toInt())
        command = source.readByte().toInt() and 0xFF
        val length = source.readByte().toInt() and 0xFF
        frame.writeByte(length)
        payload = Buffer()
        source.require(length.toLong() + footerLength)
        source.read(payload, length.toLong())
        payloadLength = length.toLong()
        frame.write(payload.snapshot())
        val rawTimestamp = source.readInt().toLong() and 0xFFFFFFFF
        timestamp = TandemPump.EPOCH + rawTimestamp * 1000
        footer = Buffer()
        footer.writeInt(rawTimestamp.toInt())
        expectedChecksum = source.readShort().toInt() and 0xFFFF
        footer.writeInt(rawTimestamp.toInt())
        frame.write(footer.snapshot())
        parsedPayload = Payload.parse(command, payload)
    }
}

data class TimeDependentSettings(public val startTime: LocalTime,
                                 public val basalRate: Double?,
                                 public val carbRatio: Double?,
                                 public val targetBg: GlucoseValue?,
                                 public val isf: Int?) // mask: basal/carb/target

data class BolusSettings(public val insulinDuration: Duration?, // minutes
                         public val maxBolus: Double,
                         public val carbEntry: Boolean?) // mask: dur/max/carb

data class QuickBolusSettings(public val useQuickBolus: Boolean?,
                              public val incrementInsulin: Float?,
                              public val incrementCarbs: Float?,
                              public val incrementsCarbs: Boolean?) // mask: use/ins/carb/which

data class Reminder(public val remindIn: Duration?, // minutes
                    public val startTime: LocalTime?, // minutes from midnight
                    public val endTime: LocalTime?,
                    public val days: Int?, // bitmask mtwrfss
                    public val active: Boolean) // mask: rin, act, start, end, days

data class Ack (val command: Int, val success: Int) : Payload {
    constructor(source: BufferedSource) : this(source.readByte().toInt() and 0xFF, source.readByte().toInt() and 0xFF)
}
