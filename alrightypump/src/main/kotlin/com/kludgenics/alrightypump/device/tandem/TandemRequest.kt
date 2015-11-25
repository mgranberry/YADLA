package com.kludgenics.alrightypump.device.tandem

import okio.Buffer

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

data class IdpReq(public val idp: Int) : TandemPayload {
    companion object {
        const public val COMMAND: Int = 175
    }

    override val payload: Buffer
        get() = Buffer().writeByte(idp)
    override val payloadLength = 1
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