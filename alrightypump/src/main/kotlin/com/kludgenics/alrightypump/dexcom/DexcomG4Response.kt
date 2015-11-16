package com.kludgenics.alrightypump.dexcom

import com.kludgenics.alrightypump.CRC
import okio.Buffer
import okio.BufferedSource

/**
 * Created by matthias on 11/7/15.
 */

interface DexcomResponse {
    val command: Int
    val payload: Payload
    val valid: Boolean
}

class DexcomG4Response(originalCommand: Int, command: Int, payload: Payload, calculatedCrc: Int? = null,
                       expectedCrc: Int? = null): DexcomG4Packet(command, payload, calculatedCrc,
        expectedCrc), DexcomResponse
{
    companion object {
        public fun parse(source: BufferedSource, originalCommand: Int): DexcomResponse {
            source.require(6)
            source.skip(source.indexOf(DexcomG4Frame.syncByte))
            source.require(6)

            var crc = CRC.DEXCOM_INITIAL_REMAINDER
            val syncByte = source.readByte().toInt() and 0xFF
            crc = CRC.updateChecksum(crc, syncByte.toByte())
            val length = source.readShortLe().toInt() and 0xFFFF
            crc = CRC.updateChecksum(crc, (length and 0xFF).toByte())
            crc = CRC.updateChecksum(crc, (length shr 8).toByte())
            val command = source.readByte().toInt() and 0xFF
            crc = CRC.updateChecksum(crc, command.toByte())
            val payloadLength = length.toLong() - 6
            source.require(payloadLength)
            val payloadBuffer = Buffer()
            source.buffer().copyTo(payloadBuffer, 0, payloadLength)
            val payload = Payload.parseResponse(originalCommand, command, payloadBuffer)
            crc = CRC.updateChecksum(crc, payload.payload.snapshot().toByteArray())
            val calculatedCrc = crc xor CRC.DEXCOM_FINAL_XOR
            val expectedCrc = source.readShortLe().toInt() and 0xFFFF
            return DexcomG4Response(originalCommand, command, payload, calculatedCrc, expectedCrc)
        }
    }

    public override val valid: Boolean get() =
        (expectedCrc == calculatedCrc) && command != NakCommand.COMMAND
}