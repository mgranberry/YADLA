package com.kludgenics.alrightypump.tandem

import com.kludgenics.alrightypump.Frame
import okio.Buffer
import org.joda.time.Instant

/**
 * Created by matthias on 11/19/15.
 */
abstract class TandemFrame : Frame {

    abstract override val header: Buffer
    abstract override val payload: Buffer
    abstract override val footer: Buffer

    override val sync: Int get() = 0x55

    override val headerLength: Long get() = 3
    override val footerLength: Long get() = 6
    override val payloadLength: Long get() = header.getByte(2).toLong() and 0xFF

    override val calculatedChecksum: Int by lazy {
        val buff = Buffer()
        buff.write(header.snapshot())
        buff.write(payload.snapshot())
        buff.readByte()
        var checksum = 0
        while (!buff.exhausted())
            checksum = checksum + (buff.readByte().toInt() and 0xFF)
        checksum and 0xFFFF
    }

    override fun calculateChecksum(buffer: Buffer, initialRemainder: Int, finalXor: Int, mask: Int): Int {
        val temp = Buffer()
        buffer.copyTo(temp, checksumRange.start, checksumRange.end - checksumRange.start)
        var sum = 0
        while (!buffer.exhausted())
            sum += buffer.readByte()
        return sum and 0xFFFF
    }
}