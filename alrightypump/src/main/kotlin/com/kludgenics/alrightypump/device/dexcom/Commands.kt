package com.kludgenics.alrightypump.device.dexcom

import okio.Buffer
import okio.ByteString
import java.util.*

/**
 * Created by matthias on 11/5/15.
 */

interface DexcomCommand {
    val command: Int
    val payload: Buffer get() = Buffer()
}

class NullResponse(payload: Buffer) : UnsupportedDexcomResponse(payload) {
    companion object {
        public val COMMAND: Int = 0
    }
    override public val command: Int get() = COMMAND
}

class AckResponse(payload: Buffer) : UnsupportedDexcomResponse(payload) {
    companion object {
        public val COMMAND: Int = 1
    }
    override public val command: Int get() = COMMAND
}

class NakResponse(payload: Buffer) : UnsupportedDexcomResponse(payload) {
    companion object {
        public val COMMAND: Int = 2
    }
    override public val command: Int get() = COMMAND
}

class InvalidCommandResponse(payload: Buffer) : UnsupportedDexcomResponse(payload) {
    companion object {
        public val COMMAND: Int = 3
    }
    override public val command: Int get() = COMMAND
}

class InvalidParam(payload: Buffer) : UnsupportedDexcomResponse(payload) {
    companion object {
        public val COMMAND: Int = 4
    }
    override public val command: Int get() = COMMAND
}

class IncompletePacket(payload: Buffer) : UnsupportedDexcomResponse(payload) {
    companion object {
        public val COMMAND: Int = 5
    }
    override public val command: Int get() = COMMAND
}

class ReceiverError(payload: Buffer) : UnsupportedDexcomResponse(payload) {
    companion object {
        public val COMMAND: Int = 6
    }
    override public val command: Int get() = COMMAND
}

class InvalidMode(payload: Buffer) : UnsupportedDexcomResponse(payload) {
    companion object {
        public val COMMAND: Int = 7
    }
    override public val command: Int get() = COMMAND
}

class Ping : DexcomCommand, ResponsePayload {
    companion object {
        public val COMMAND: Int = 10
    }
    override public val command: Int get() = COMMAND
}

class ReadFirmwareHeader : DexcomCommand {
    companion object {
        public val COMMAND: Int = 11
    }
    override public val command: Int get() = COMMAND
}

class ReadFirmwareHeaderResponse(payload: Buffer) : XmlDexcomResponse(payload) {
    override val command: Int
        get() = ReadFirmwareHeader.COMMAND
}

class ReadDatabasePartitionInfo : DexcomCommand {
    companion object {
        public val COMMAND: Int = 15
    }
    override public val command: Int get() = COMMAND
}

class ReadDatabasePartitionInfoResponse(payload: Buffer) : XmlDexcomResponse(payload) {
    override val command: Int
        get() = ReadDatabasePartitionInfo.COMMAND
}

class ReadDataPageRange(val recordType: Int) : DexcomCommand {
    companion object {
        public val COMMAND: Int = 16
    }
    override public val command: Int get() = COMMAND
    override public val payload: Buffer get() = Buffer().writeByte(recordType)
}

class ReadDataPageRangeResponse(payload: Buffer) : ResponsePayload {
    public val start: Int
    public val end: Int
    init {
        payload.require(8)
        start = payload.readIntLe()
        end = payload.readIntLe()
    }
    override public val command: Int get() = ReadDataPageRange.COMMAND
}

/**
 * Note: only a count of 1 is supported for responses.
 */
class ReadDataPages(val recordType: Int, val start: Int, val count: Int = 1) : DexcomCommand {
    companion object {
        public val COMMAND: Int = 17
    }
    public override val command: Int get() = COMMAND
    public override val payload: Buffer get() = Buffer().writeByte(recordType).writeIntLe(start).writeByte(count)
}

class ReadDataPagesResponse(payload: Buffer, count: Int = 1) : ResponsePayload {
    public val pages: List<RecordPage>
    init {
        pages = ArrayList<RecordPage>(count)
        for (i in 1..count) {
            val page = RecordPage.parse(payload)
            if (page != null)
                pages.add(page)
        }
    }
    public override val command: Int get() = ReadDataPages.COMMAND
}

class ReadDataPageHeader : DexcomCommand {
    companion object {
        public val COMMAND: Int = 18
    }
    override public val command: Int get() = COMMAND
}

class ReadDataPageHeaderResponse(payload: Buffer) : UnsupportedDexcomResponse(payload) {
    override val command: Int
        get() = ReadDataPageHeader.COMMAND

}

class ReadLanguage : DexcomCommand {
    companion object {
        public val COMMAND: Int = 27
    }
    override public val command: Int get() = COMMAND
}

class ReadLanguageResponse(payload: Buffer) : UnsupportedDexcomResponse(payload) {
    override val command: Int
        get() = ReadLanguage.COMMAND
}

class ReadDisplayTimeOffset : DexcomCommand {
    companion object {
        public val COMMAND: Int = 29
    }
    override public val command: Int get() = COMMAND
}

class ReadDisplayTimeOffsetResponse(payload: Buffer) : UnsupportedDexcomResponse(payload) {
    override val command: Int
        get() = ReadDisplayTimeOffset.COMMAND
}


class ReadSystemTime : DexcomCommand {
    companion object {
        public val COMMAND: Int = 34
    }
    override public val command: Int get() = COMMAND
}

class ReadSystemTimeResponse(payload: Buffer) : UnsupportedDexcomResponse(payload) {
    override val command: Int
        get() = ReadSystemTime.COMMAND
}

class ReadSystemTimeOffset : DexcomCommand {
    companion object {
        public val COMMAND: Int = 35
    }
    override public val command: Int get() = COMMAND
}

class ReadSystemTimeOffsetResponse(payload: Buffer) : UnsupportedDexcomResponse(payload) {
    override val command: Int
        get() = ReadSystemTimeOffset.COMMAND
}

class ReadGlucoseUnit : DexcomCommand {
    companion object {
        public val COMMAND: Int = 37
    }
    override public val command: Int get() = COMMAND
}

class ReadGlucoseUnitResponse(payload: Buffer) : UnsupportedDexcomResponse(payload) {
    override val command: Int
        get() = ReadGlucoseUnit.COMMAND
}

class ReadClockMode : DexcomCommand {
    companion object {
        public val COMMAND: Int = 41
    }
    override public val command: Int get() = COMMAND
}

class ReadClockModeResponse(payload: Buffer) : UnsupportedDexcomResponse(payload) {
    override val command: Int
        get() = ReadClockMode.COMMAND
}

abstract class XmlDexcomResponse(payload: Buffer) :
        UnsupportedDexcomResponse(payload) {
    override fun toString(): String {
        return "${this.javaClass.simpleName}(command: ${command.toInt()}, payload: ${payloadString.utf8()})"
    }
}

abstract class UnsupportedDexcomResponse(override val payload: Buffer) : ResponsePayload {
    val payloadString: ByteString
        get() = payload.snapshot()

    override fun toString(): String {
        return "${this.javaClass.simpleName}(command: ${command.toInt()}, payload: ${payloadString.hex()})"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as UnsupportedDexcomResponse

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