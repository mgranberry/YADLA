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