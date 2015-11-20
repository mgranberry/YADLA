package com.kludgenics.alrightypump.tandem

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
            buff.writeInt(0) // Big-Endian intentionally
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
        public val COMMAND: Int = 82
    }

    override public val id: Int get() = COMMAND
}

class LogEntrySeqReq(public val seqNum: Long) : TandemPayload {
    companion object {
        public val COMMAND: Int = 151
    }

    override val payloadLength: Int get() = 4
    override val payload: Buffer
        get() = Buffer().writeIntLe(seqNum.toInt())
    override public val id: Int get() = COMMAND
}

class LogEntrySeqMultiReq : TandemPayload {
    companion object {
        public val COMMAND: Int = 152
    }

    override public val id: Int get() = COMMAND
}

class LogEntrySeqMultiStopDump(public val seqNum: Long, public val howMany: Long) : TandemPayload {
    companion object {
        public val COMMAND: Int = 153
    }

    override val payloadLength: Int get() = 8
    override val payload: Buffer
        get() = Buffer().writeIntLe(seqNum.toInt()).writeIntLe(howMany.toInt())
    override public val id: Int get() = COMMAND
}

class LogSizeReq : TandemPayload {
    companion object {
        public val COMMAND: Int = 168
    }

    override public val id: Int get() = COMMAND
}

data class LogSizeResp(public val numEntries: Long,
                       public val firstSeqNo: Long,
                       public val lastSeqNo: Long) : TandemPayload {
    constructor(buffer: Buffer) : this(buffer.readIntLe().toLong() and 0xFFFFFFFF,
            buffer.readIntLe().toLong() and 0xFFFFFFFF,
            buffer.readIntLe().toLong() and 0xFFFFFFFF) {

    }

    companion object {
        public val COMMAND: Int = 169
    }

    public override val id: Int get() = COMMAND
    val range = firstSeqNo .. lastSeqNo
}

class IdpListReq : TandemPayload {
    companion object {
        public val COMMAND: Int = 173
    }

    override public val id: Int get() = COMMAND
}

class IdpReq(val idp: Int) : TandemPayload {
    companion object {
        public val COMMAND: Int = 175
    }

    override val payload: Buffer
        get() = Buffer().writeByte(idp)
    override val payloadLength = 1
    override public val id: Int get() = COMMAND
}

class GlobalsReq : TandemPayload {
    companion object {
        public val COMMAND: Int = 178
    }

    override public val id: Int get() = COMMAND
}

class PumpSettingsReq : TandemPayload {
    companion object {
        public val COMMAND: Int = 181
    }

    override public val id: Int get() = COMMAND
}

class RemindSettingsReq : TandemPayload {
    companion object {
        public val COMMAND: Int = 184
    }

    override public val id: Int get() = COMMAND
}

