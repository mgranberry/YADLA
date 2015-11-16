package com.kludgenics.alrightypump.dexcom

import com.kludgenics.alrightypump.DexcomCrcSink
import com.kludgenics.alrightypump.DexcomCrcSource
import okio.*
import kotlin.reflect.KProperty

public open class DexcomG4Packet(public val command: Int,
                     public val payload: Payload,
                     public val calculatedCrc: Int? = null,
                     public val expectedCrc: Int? = null): Source {

    private var closed = false
    private val timeout = Timeout.NONE

    public val packet: Buffer get() {
        val buff = Buffer()
        val crcSink = DexcomCrcSink(buff)
        val crcBuffer = Okio.buffer(crcSink)
        crcBuffer.writeByte(DexcomG4Frame.syncByte.toInt())
        val payloadSnapshot = payload.payload.snapshot()
        crcBuffer.writeShortLe(payloadSnapshot.size() + 6)
        crcBuffer.writeByte(command.toInt())
        crcBuffer.write(payloadSnapshot)
        crcBuffer.flush()
        buff.writeShortLe(crcSink.crc)
        return buff
    }

    public override fun read(sink: Buffer, byteCount: Long): Long {
        if (closed) throw IllegalStateException("closed")
        return packet.read(sink, byteCount)
    }

    public override fun close() {
        packet.clear()
        closed = true
    }

    public override fun timeout(): Timeout {
        return timeout
    }

    public open val valid: Boolean get() = (calculatedCrc == expectedCrc)

}
