package com.kludgenics.alrightypump.dexcom

import okio.Buffer
import okio.ByteString

/**
 * Created by matthias on 11/5/15.
 */

interface DexcomCommand: Payload {
    override val command: Int
    override val payload: Buffer
}

class NullCommand(payload: Buffer) : DexcomCommand, UnsupportedPayload(NullCommand.COMMAND, payload) {

    companion object {
        public val COMMAND: Int = 0
    }

    override public val command: Int get() = COMMAND

}

class AckCommand(payload: Buffer) : DexcomCommand, UnsupportedPayload(AckCommand.COMMAND, payload) {

   companion object {
        public val COMMAND: Int = 1
    }

    override public val command: Int get() = COMMAND

}

class NakCommand(payload: Buffer) : DexcomCommand, UnsupportedPayload(NakCommand.COMMAND, payload) {

    companion object {
        public val COMMAND: Int = 2
    }

    override public val command: Int get() = COMMAND

}

class InvalidCommand(payload: Buffer) : DexcomCommand, UnsupportedPayload(InvalidCommand.COMMAND, payload) {

    companion object {
        public val COMMAND: Int = 3
    }

    override public val command: Int get() = COMMAND

}

class InvalidParam(payload: Buffer) : DexcomCommand, UnsupportedPayload(InvalidParam.COMMAND, payload) {

    companion object {
        public val COMMAND: Int = 4
    }

    override public val command: Int get() = COMMAND

}

class IncompletePacket(payload: Buffer) : DexcomCommand, UnsupportedPayload(IncompletePacket.COMMAND, payload) {

    companion object {
        public val COMMAND: Int = 5
    }

    override public val command: Int get() = COMMAND

}

class ReceiverError(payload: Buffer) : DexcomCommand, UnsupportedPayload(ReceiverError.COMMAND, payload) {

    companion object {
        public val COMMAND: Int = 6
    }

    override public val command: Int get() = COMMAND

}

class InvalidMode(payload: Buffer) : DexcomCommand, UnsupportedPayload(InvalidMode.COMMAND, payload) {

    companion object {
        public val COMMAND: Int = 7
    }

    override public val command: Int get() = COMMAND

}

class Ping(payload: Buffer = Buffer()) : DexcomCommand, UnsupportedPayload(Ping.COMMAND, payload) {

    companion object {
        public val COMMAND: Int = 10
    }

    override public val command: Int get() = COMMAND

}

class ReadFirmwareHeader(payload: Buffer = Buffer()) : DexcomCommand, XmlPayload(ReadFirmwareHeader.COMMAND, payload) {

    companion object {
        public val COMMAND: Int = 11
    }

    override public val command: Int get() = COMMAND

}


class ReadDatabasePartitionInfo(payload: Buffer) : DexcomCommand, XmlPayload(ReadDatabasePartitionInfo.COMMAND, payload) {

    companion object {
        public val COMMAND: Int = 15
    }

    override public val command: Int get() = COMMAND

}

class ReadDataPageRange(payload: Buffer) : DexcomCommand, UnsupportedPayload(ReadDataPageRange.COMMAND, payload) {

    companion object {
        public val COMMAND: Int = 16
    }

    override public val command: Int get() = COMMAND

}

class ReadDataPages(payload: Buffer) : DexcomCommand, UnsupportedPayload(ReadDataPages.COMMAND, payload) {

    companion object {
        public val COMMAND: Int = 17
    }

    override public val command: Int get() = COMMAND

}

class ReadDataPageHeader(payload: Buffer) : DexcomCommand, UnsupportedPayload(ReadDataPageHeader.COMMAND, payload) {

    companion object {
        public val COMMAND: Int = 18
    }

    override public val command: Int get() = COMMAND

}

class ReadLanguage(payload: Buffer) : DexcomCommand, UnsupportedPayload(ReadLanguage.COMMAND, payload) {

    companion object {
        public val COMMAND: Int = 27
    }

    override public val command: Int get() = COMMAND

}

class ReadDisplayTimeOffset(payload: Buffer) : DexcomCommand, UnsupportedPayload(ReadDisplayTimeOffset.COMMAND, payload) {

    companion object {
        public val COMMAND: Int = 29
    }

    override public val command: Int get() = COMMAND

}

class ReadSystemTime(payload: Buffer) : DexcomCommand, UnsupportedPayload(ReadSystemTime.COMMAND, payload) {

    companion object {
        public val COMMAND: Int = 34
    }

    override public val command: Int get() = COMMAND

}

class ReadSystemTimeOffset(payload: Buffer) : DexcomCommand, UnsupportedPayload(ReadSystemTimeOffset.COMMAND, payload) {

    companion object {
        public val COMMAND: Int = 35
    }

    override public val command: Int get() = COMMAND

}

class ReadGlucoseUnit(payload: Buffer) : DexcomCommand, UnsupportedPayload(ReadGlucoseUnit.COMMAND, payload) {

    companion object {
        public val COMMAND: Int = 37
    }

    override public val command: Int get() = COMMAND

}

class ReadClockMode(payload: Buffer) : DexcomCommand, UnsupportedPayload(ReadClockMode.COMMAND, payload) {

    companion object {
        public val COMMAND: Int = 41
    }

    override public val command: Int get() = COMMAND

}

open class XmlPayload(override public val command: Int, override val payload: Buffer):
        UnsupportedPayload(command, payload) {

    override fun toString(): String {
        return "${this.javaClass.simpleName}(command: ${command.toInt()}, payload: ${payloadString.utf8()})"
    }
}

open class UnsupportedPayload(override public val command: Int, override val payload: Buffer) : Payload {
    val payloadString: ByteString
        get() = payload.snapshot()

    override fun toString(): String {
        return "${this.javaClass.simpleName}(command: ${command.toInt()}, payload: ${payloadString.hex()})"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as UnsupportedPayload

        if (command != other.command) return false
        if (payload != other.payload) return false

        return true
    }

    override fun hashCode(): Int {
        var result: Int = command.toInt()
        result += 31 * result + payload.hashCode()
        return result
    }
}