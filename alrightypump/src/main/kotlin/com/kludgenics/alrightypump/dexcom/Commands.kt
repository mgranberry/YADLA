package com.kludgenics.alrightypump.dexcom

import okio.ByteString

/**
 * Created by matthias on 11/5/15.
 */

interface DexcomCommand: Payload {
    override val command: Int
    override val payload: ByteArray
}

class NullCommand(payload: ByteArray) : DexcomCommand, UnsupportedPayload(NullCommand.COMMAND, payload) {

    companion object {
        public val COMMAND: Int = 0
    }

    override public val command: Int get() = COMMAND

}

class AckCommand(payload: ByteArray) : DexcomCommand, UnsupportedPayload(AckCommand.COMMAND, payload) {

   companion object {
        public val COMMAND: Int = 1
    }

    override public val command: Int get() = COMMAND

}

class NakCommand(payload: ByteArray) : DexcomCommand, UnsupportedPayload(NakCommand.COMMAND, payload) {

    companion object {
        public val COMMAND: Int = 2
    }

    override public val command: Int get() = COMMAND

}

class InvalidCommand(payload: ByteArray) : DexcomCommand, UnsupportedPayload(InvalidCommand.COMMAND, payload) {

    companion object {
        public val COMMAND: Int = 3
    }

    override public val command: Int get() = COMMAND

}

class InvalidParam(payload: ByteArray) : DexcomCommand, UnsupportedPayload(InvalidParam.COMMAND, payload) {

    companion object {
        public val COMMAND: Int = 4
    }

    override public val command: Int get() = COMMAND

}

class IncompletePacket(payload: ByteArray) : DexcomCommand, UnsupportedPayload(IncompletePacket.COMMAND, payload) {

    companion object {
        public val COMMAND: Int = 5
    }

    override public val command: Int get() = COMMAND

}

class ReceiverError(payload: ByteArray) : DexcomCommand, UnsupportedPayload(ReceiverError.COMMAND, payload) {

    companion object {
        public val COMMAND: Int = 6
    }

    override public val command: Int get() = COMMAND

}

class InvalidMode(payload: ByteArray) : DexcomCommand, UnsupportedPayload(InvalidMode.COMMAND, payload) {

    companion object {
        public val COMMAND: Int = 7
    }

    override public val command: Int get() = COMMAND

}

class Ping() : DexcomCommand, UnsupportedPayload(Ping.COMMAND, ByteArray(0)) {

    companion object {
        public val COMMAND: Int = 10
    }

    override public val command: Int get() = COMMAND

}

class ReadFirmwareHeader(payload: ByteArray = ByteArray(0)) : DexcomCommand, XmlPayload(ReadFirmwareHeader.COMMAND, payload) {

    companion object {
        public val COMMAND: Int = 11
    }

    override public val command: Int get() = COMMAND

}


class ReadDatabasePartitionInfo(payload: ByteArray) : DexcomCommand, UnsupportedPayload(ReadDatabasePartitionInfo.COMMAND, payload) {

    companion object {
        public val COMMAND: Int = 15
    }

    override public val command: Int get() = COMMAND

}

class ReadDataPageRange(payload: ByteArray) : DexcomCommand, UnsupportedPayload(ReadDataPageRange.COMMAND, payload) {

    companion object {
        public val COMMAND: Int = 16
    }

    override public val command: Int get() = COMMAND

}

class ReadDataPages(payload: ByteArray) : DexcomCommand, UnsupportedPayload(ReadDataPages.COMMAND, payload) {

    companion object {
        public val COMMAND: Int = 17
    }

    override public val command: Int get() = COMMAND

}

class ReadDataPageHeader(payload: ByteArray) : DexcomCommand, UnsupportedPayload(ReadDataPageHeader.COMMAND, payload) {

    companion object {
        public val COMMAND: Int = 18
    }

    override public val command: Int get() = COMMAND

}

class ReadLanguage(payload: ByteArray) : DexcomCommand, UnsupportedPayload(ReadLanguage.COMMAND, payload) {

    companion object {
        public val COMMAND: Int = 27
    }

    override public val command: Int get() = COMMAND

}

class ReadDisplayTimeOffset(payload: ByteArray) : DexcomCommand, UnsupportedPayload(ReadDisplayTimeOffset.COMMAND, payload) {

    companion object {
        public val COMMAND: Int = 29
    }

    override public val command: Int get() = COMMAND

}

class ReadSystemTime(payload: ByteArray) : DexcomCommand, UnsupportedPayload(ReadSystemTime.COMMAND, payload) {

    companion object {
        public val COMMAND: Int = 34
    }

    override public val command: Int get() = COMMAND

}

class ReadSystemTimeOffset(payload: ByteArray) : DexcomCommand, UnsupportedPayload(ReadSystemTimeOffset.COMMAND, payload) {

    companion object {
        public val COMMAND: Int = 35
    }

    override public val command: Int get() = COMMAND

}

class ReadGlucoseUnit(payload: ByteArray) : DexcomCommand, UnsupportedPayload(ReadGlucoseUnit.COMMAND, payload) {

    companion object {
        public val COMMAND: Int = 37
    }

    override public val command: Int get() = COMMAND

}

class ReadClockMode(payload: ByteArray) : DexcomCommand, UnsupportedPayload(ReadClockMode.COMMAND, payload) {

    companion object {
        public val COMMAND: Int = 41
    }

    override public val command: Int get() = COMMAND

}

open class XmlPayload(override public val command: Int, override val payload: ByteArray):
        UnsupportedPayload(command, payload) {

    override fun toString(): String {
        return "${this.javaClass.simpleName}(command: ${command.toInt()}, payload: ${payloadString.utf8()})"
    }
}

open class UnsupportedPayload(override public val command: Int, override val payload: ByteArray) : Payload {
    val payloadString: ByteString
        get() = ByteString.of(payload, 0, payload.size)

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