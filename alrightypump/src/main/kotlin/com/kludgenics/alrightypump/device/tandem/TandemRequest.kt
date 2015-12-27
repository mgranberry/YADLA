package com.kludgenics.alrightypump.device.tandem

import com.kludgenics.alrightypump.therapy.BaseGlucoseValue
import com.kludgenics.alrightypump.therapy.GlucoseUnit
import okio.Buffer
import org.joda.time.Duration
import org.joda.time.LocalTime
import java.util.*

/**
 * Created by matthias on 11/20/15.
 */
class TandemRequest(val command: TandemPayload) : TandemFrame() {
    override val header: Buffer
        get() {
            val buff = Buffer()
            buff.writeByte(sync)
            buff.writeByte(command.id)
            buff.writeByte(command.payloadLength)
            return buff
        }

    override val payload: Buffer get() = command.payload

    override val footer: Buffer
        get() {
            val buff = Buffer()
            buff.writeInt(0) // The footer is Big-Endian intentionally
            buff.writeShort(calculatedChecksum)
            return buff
        }

    override val expectedChecksum: Int
        get() = calculatedChecksum
}

interface TandemPayload {
    val id: Int
    val payloadLength: Int get() = 0
    val payload: Buffer get() = Buffer()
}

class VersionReq : TandemPayload {
    companion object {
        const public val COMMAND: Int = 82
    }

    override public val id: Int get() = COMMAND
}

data class VersionResp(val armSwVer: String,
                       val mspSwVer: String,
                       val pumpSN: Int,
                       val pumpPartNo: Int,
                       val pumpRev: String,
                       val pcbaSN: Int,
                       val pcbaRev: String,
                       val modelNumber: Int) : TandemPayload {
    constructor(buffer: Buffer) : this(armSwVer = buffer.readUtf8(16).trim(0.toChar()),
            mspSwVer = buffer.readUtf8(16).trim(0.toChar()),
            pumpSN = {buffer.skip(20); buffer.readIntLe()}(),
            pumpPartNo = buffer.readIntLe(),
            pumpRev = buffer.readUtf8(8).trim(0.toChar()),
            pcbaSN = buffer.readIntLe(),
            pcbaRev = {buffer.skip(4); buffer.readUtf8(8).trim(0.toChar())}(),
            modelNumber = {buffer.skip(50); buffer.readIntLe()}())

    companion object {
        const public val COMMAND: Int = 81
    }

    override val id: Int
        get() = COMMAND
}

data class LogEntrySeqReq(public val seqNum: Long) : TandemPayload {
    companion object {
        const public val COMMAND: Int = 151
    }

    override val payloadLength: Int get() = 4
    override val payload: Buffer
        get() = Buffer().writeIntLe(seqNum.toInt())
    override public val id: Int get() = COMMAND
}

class LogEntrySeqMultiReq : TandemPayload {
    companion object {
        const public val COMMAND: Int = 152
    }

    override public val id: Int get() = COMMAND
}

data class LogEntrySeqMultiStopDump(public val seqNum: Long,
                                    public val howMany: Long) : TandemPayload {
    companion object {
        const public val COMMAND: Int = 153
    }

    override val payloadLength: Int get() = 8
    override val payload: Buffer
        get() = Buffer().writeIntLe(seqNum.toInt()).writeIntLe(howMany.toInt())
    override public val id: Int get() = COMMAND
}

class LogSizeReq : TandemPayload {
    companion object {
        const public val COMMAND: Int = 168
    }

    override public val id: Int get() = COMMAND
}

data class LogSizeResp(public val numEntries: Int,
                       public val firstSeqNo: Int,
                       public val lastSeqNo: Int) : TandemPayload {
    constructor(buffer: Buffer) : this(buffer.readIntLe(),
            buffer.readIntLe(),
            buffer.readIntLe()) {

    }

    companion object {
        const public val COMMAND: Int = 169
    }

    public override val id: Int get() = COMMAND
    val range = firstSeqNo .. lastSeqNo
}

class IdpListReq : TandemPayload {
    companion object {
        const public val COMMAND: Int = 173
    }

    override public val id: Int get() = COMMAND
}

data class IdpListResp(val idps: IntArray) : TandemPayload {
    constructor(buffer: Buffer) : this(idps = {
        val count = buffer.readByte()
        val idps = IntArray(count.toInt(), { buffer.readByte().toInt() and 0xFF })
        buffer.skip((6 - count).toLong())
        idps
    }())
    companion object {
        const public val COMMAND: Int = 173
    }

    override public val id: Int get() = COMMAND
}

data class IdpReq(public val idp: Int) : TandemPayload {
    companion object {
        const public val COMMAND: Int = 175
    }

    override val payload: Buffer
        get() = Buffer().writeByte(idp)
    override val payloadLength = 1
    override public val id: Int get() = COMMAND
}

data class IdpResp(val idp: Int,
                   val name: String,
                   val timeDependentSettings: List<TimeDependentSettings>,
                   val bolusSettings: BolusSettings) : TandemPayload {
    constructor(buffer: Buffer) : this(idp = buffer.readByte().toInt() and 0xFF,
            name = buffer.readUtf8(17).trim(0.toChar()),
            timeDependentSettings = {
                val segmentCount = buffer.readByte().toInt() and 0xFF
                val settings = ArrayList<TimeDependentSettings>(segmentCount)
                for (segment in 1..segmentCount) {
                    var minutesFromMidnight = buffer.readShortLe().toInt() and 0xFFFF
                    var basalRate: Double? = (buffer.readShortLe().toInt() and 0xFFFF) / 1000.0
                    var carbRatio: Double? = (buffer.readIntLe()) / 1000.0
                    var targetBg: Int? = buffer.readShortLe().toInt()
                    var isf: Int? = buffer.readShortLe().toInt()
                    val status = buffer.readByte().toInt() and 0xFF
                    val localTime = LocalTime(minutesFromMidnight / 60, minutesFromMidnight % 60)
                    basalRate = if (status and 0x01 != 0) basalRate else null
                    carbRatio = if (status and 0x02 != 0) carbRatio else null
                    targetBg = if (status and 0x04 != 0) targetBg else null
                    isf = if (status and 0x08 != 0) isf else null

                    settings.add(TimeDependentSettings(startTime = localTime,
                            basalRate = basalRate,
                            carbRatio = carbRatio,
                            targetBg = if (targetBg != null)
                                BaseGlucoseValue(targetBg.toDouble(), GlucoseUnit.MGDL)
                            else
                                null,
                            isf = isf))
                }
                buffer.skip((13 * (16 - segmentCount)).toLong())
                settings
            }(),
            bolusSettings = BolusSettings(insulinDuration = Duration(buffer.readShortLe().toLong() * 60 * 1000),
                        maxBolus = buffer.readShortLe() / 1000.0,
                        carbEntry = buffer.readByte() == 1.toByte()))
    companion object {
        const public val COMMAND: Int = 176
    }
    override public val id: Int get() = COMMAND
}

class GlobalsReq : TandemPayload {
    companion object {
        const public val COMMAND: Int = 178
    }

    override public val id: Int get() = COMMAND
}

class PumpSettingsReq : TandemPayload {
    companion object {
        const public val COMMAND: Int = 181
    }

    override public val id: Int get() = COMMAND
}

class RemindSettingsReq : TandemPayload {
    companion object {
        const public val COMMAND: Int = 184
    }

    override public val id: Int get() = COMMAND
}

class Command188(val value1: Int = 7, val value2: Int) : TandemPayload {
    companion object {
        const public val COMMAND: Int = 188
    }
    override val payload: Buffer
        get() = Buffer().writeIntLe(value1).writeIntLe(value2)
    override val payloadLength = 8

    override public val id: Int = COMMAND
}

class Command189() : TandemPayload {
    companion object {
        const public val COMMAND: Int = 189
    }

    override public val id: Int = COMMAND
}
class Command209() : TandemPayload {
    companion object {
        const public val COMMAND: Int = 209
    }

    override public val id: Int = COMMAND
}

class Command83() : TandemPayload {
    companion object {
        const public val COMMAND: Int = 83
    }
    override val payload: Buffer
        get() = Buffer().writeIntLe(0x17a5c352)
    override val payloadLength = 4
    override public val id: Int = COMMAND
}

class Command61() : TandemPayload {
    companion object {
        const public val COMMAND: Int = 61
    }
    override public val id: Int = COMMAND
}