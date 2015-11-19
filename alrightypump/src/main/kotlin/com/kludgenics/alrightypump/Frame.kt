package com.kludgenics.alrightypump

import okio.Buffer
import okio.ByteString

/**
 * Created by matthias on 11/4/15.
 */
interface Frame {
    val sync: Int

    val frame: Buffer
        get() {
            val buff = Buffer()
            header?.copyTo(buff, 0, headerLength)
            payload?.copyTo(buff, 0, payloadLength)
            footer?.copyTo(buff, 0, footerLength)
            return buff
        }

    val frameLength: Long
        get() = headerLength + payloadLength + footerLength

    val header: Buffer?
    val payload: Buffer?
    val footer: Buffer?

    val headerLength: Long
    val headerRange: LongRange
        get() = 0L until headerLength

    val payloadLength: Long
    val payloadRange: LongRange
        get() = headerLength until payloadLength + headerLength

    val footerLength: Long
    val footerRange: LongRange
        get() = headerLength + payloadLength until headerLength + payloadLength + footerLength

    val crcRange: LongRange
        get() = 0 .. payloadRange.end

    val calculatedChecksum: Int
    val expectedChecksum: Int

    fun calculateChecksum(buffer: Buffer,
                     initialRemainder: Int = 0,
                     finalXor: Int = 0,
                     mask: Int = 0xFFFF): Int {
        val temp = Buffer()
        buffer.copyTo(temp, crcRange.start, crcRange.end - crcRange.start)
        return CRC.updateChecksum(temp, temp.size(), initialRemainder, finalXor, mask)
    }

    val valid: Boolean get() = calculatedChecksum == expectedChecksum
}