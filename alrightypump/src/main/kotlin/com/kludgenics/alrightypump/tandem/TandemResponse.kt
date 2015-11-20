package com.kludgenics.alrightypump.tandem

import okio.Buffer
import okio.BufferedSource
import org.joda.time.Instant

/**
 * Created by matthias on 11/20/15.
 */
class TandemResponse(source: BufferedSource) : TandemFrame() {
    override val header: Buffer
        get() = throw UnsupportedOperationException()
    override val payload: Buffer

    override val footer: Buffer
    override val expectedChecksum: Int
    override val frame: Buffer
    override val payloadLength: Long
    val timestamp: Instant
    override public val valid: Boolean
        get() = (expectedChecksum == calculatedChecksum)

    init {
        frame = Buffer()
        source.require(9)
        source.skip(source.indexOf(sync.toByte()))
        source.require(9)
        frame.writeByte(source.readByte().toInt())
        val command = source.readByte().toInt() and 0xFF
        val length = source.readByte().toInt() and 0xFF
        frame.writeByte(length)
        payload = Buffer()
        source.require(length.toLong() + footerLength)
        source.read(payload, length.toLong())
        payloadLength = length.toLong()
        frame.write(payload.snapshot())
        val rawTimestamp = source.readInt().toLong() and 0xFFFFFFFF
        timestamp = EPOCH + rawTimestamp * 1000
        footer = Buffer()
        footer.writeInt(rawTimestamp.toInt())
        expectedChecksum = source.readShort().toInt() and 0xFFFF
        footer.writeInt(rawTimestamp.toInt())
        frame.write(footer.snapshot())
    }
}