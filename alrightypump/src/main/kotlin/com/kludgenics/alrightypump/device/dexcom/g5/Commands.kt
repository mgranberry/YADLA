package com.kludgenics.alrightypump.device.dexcom.g5

import okio.*

/**
 * Created by matthias on 11/24/15.
 */

interface DexcomG5Command : Source {
    val payload: ByteString

    companion object {
        const final val OPCODE_AUTH_REQUEST_COMMAND = 0x01
        const final val OPCODE_AUTH_CHALLENGE_COMMAND = 0x04
        const final val OPCODE_KEEPALIVE_COMMAND = 0x06
        const final val OPCODE_BOND_COMMAND = 0x07
        const final val OPCODE_DISCONNECT_COMMAND = 0x09
        const final val OPCODE_TRANSMITTER_TIME_COMMAND = 0x24
        const final val OPCODE_GLUCOSE_COMMAND = 0x30
    }

    override fun timeout(): Timeout {
        return Timeout.NONE
    }

    override fun close() {
    }

    override fun read(sink: Buffer, byteCount: Long): Long {
        sink.write(payload)
        return payload.size().toLong()
    }
}

interface DexcomG5Response {
    companion object {
        const final val OPCODE_AUTH_CHALLENGE_RESPONSE = 0x03
        const final val OPCODE_AUTH_STATUS_RESPONSE = 0x05
        const final val OPCODE_GLUCOSE_RESPONSE = 0x31
        const final val OPCODE_TRANSMITTER_TIME_RESPONSE = 0x25

        fun parse(source: BufferedSource): DexcomG5Response {
            source.require(1)
            val opcode = source.readByte().toInt() and 0xFF
            val result = when (opcode) {
                OPCODE_AUTH_CHALLENGE_RESPONSE -> {
                    source.require(16)
                    AuthChallengeResponse(opcode, source)
                }
                OPCODE_AUTH_STATUS_RESPONSE -> {
                    source.require(2)
                    AuthStatusResponse(opcode, source)
                }
                OPCODE_GLUCOSE_RESPONSE -> {
                    source.require(13)
                    GlucoseResponse(opcode, source)
                }
                OPCODE_TRANSMITTER_TIME_RESPONSE -> {
                    source.require(9)
                    TransmitterTimeResponse(opcode, source)
                }
                else -> UnknownResponse(opcode, source)
            }
            val snapshot = source.readByteString() // consume remnants
            if (snapshot.size() != 0) {
                println("${snapshot.hex()} remained after reading $result")
            }
            return result
        }
    }
}

data class AuthChallengeCommand(public val challengeHash: ByteString) : DexcomG5Command {
    override val payload: ByteString by lazy {
        Buffer().writeByte(DexcomG5Command.OPCODE_AUTH_CHALLENGE_COMMAND)
                .write(challengeHash)
                .snapshot()
    }
}

data class AuthChallengeResponse(public val opcode: Int,
                                 public val tokenHash: ByteString,
                                 public val challenge: ByteString) : DexcomG5Response {
    constructor(opcode: Int, source: BufferedSource) : this(opcode = opcode,
            tokenHash = source.readByteString(8),
            challenge = source.readByteString(8))
    // Cipher.getInstance("AES/ECB/NoPadding")
}

data class AuthRequestCommand(public val singleUseToken: ByteString,
                              public val token: ByteString) : DexcomG5Command {
    override val payload: ByteString by lazy {
        Buffer().writeByte(DexcomG5Command.OPCODE_AUTH_REQUEST_COMMAND)
                .write(token)
                .writeByte(0x02)
                .snapshot()
    }
}

data class AuthStatusResponse(public val opcode: Int,
                         public val authenticated: Int,
                         public val bonded: Int) : DexcomG5Response {
    constructor(opcode: Int, source: BufferedSource) : this(opcode = opcode,
            authenticated = source.readByte().toInt() and 0xFF,
            bonded = source.readByte().toInt() and 0xFF)
}

class BondRequestCommand : DexcomG5Command {
    override val payload: ByteString by lazy {
        Buffer().writeByte(DexcomG5Command.OPCODE_BOND_COMMAND).snapshot()
    }
}

class DisconnectCommand : DexcomG5Command {
    override val payload: ByteString by lazy {
        Buffer().writeByte(DexcomG5Command.OPCODE_DISCONNECT_COMMAND).snapshot()
    }
}

class GlucoseResponse(public val opcode: Int,
                      public val status: Int,
                      public val sequence: Int,
                      public val timestamp: Int,
                      public val glucose: Int,
                      public val state: Int,
                      public val trend: Int) : DexcomG5Response {
    constructor(opcode: Int, source: BufferedSource) : this(opcode = opcode,
            status = source.readByte().toInt(),
            sequence = source.readIntLe(),
            timestamp = source.readIntLe(),
            glucose = source.readShortLe().toInt(),
            state = source.readByte().toInt() and 0xFF,
            trend = source.readByte().toInt())
}

class GlucoseCommand : DexcomG5Command {
    override val payload: ByteString by lazy {
        Buffer().writeByte(DexcomG5Command.OPCODE_GLUCOSE_COMMAND)
                .writeShortLe(0x3653) // CRC
                .snapshot()
    }
}

data class KeepaliveCommand(public val time: Int) : DexcomG5Command {
    override val payload: ByteString by lazy {
        Buffer().writeByte(DexcomG5Command.OPCODE_KEEPALIVE_COMMAND)
                .writeByte(time)
                .snapshot()
    }
}

class TransmitterTimeCommand : DexcomG5Command {
    override val payload: ByteString by lazy {
        Buffer().writeByte(DexcomG5Command.OPCODE_TRANSMITTER_TIME_COMMAND)
                .writeShortLe(0x64e6) // CRC
                .snapshot()
    }
}

class TransmitterTimeResponse(public val opcode: Int,
                              public val status: Int,
                              public val currentTime: Int,
                              public val sessionStartTime: Int) : DexcomG5Response {
    constructor(opcode: Int, source: BufferedSource) : this(opcode = opcode,
            status = source.readByte().toInt() and 0xFF,
            currentTime = source.readIntLe(),
            sessionStartTime = source.readIntLe())
}

class UnknownResponse(public val opcode: Int,
                      public val contents: ByteString) : DexcomG5Response {
    constructor(opcode: Int, source: BufferedSource) : this(opcode, source.readByteString())
}