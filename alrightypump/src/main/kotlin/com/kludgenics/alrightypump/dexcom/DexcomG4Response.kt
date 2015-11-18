package com.kludgenics.alrightypump.dexcom

import com.kludgenics.alrightypump.CRC
import okio.Buffer
import okio.BufferedSource

/**
 * Created by matthias on 11/7/15.
 */


class DexcomG4Response(public val originalCommand: Int,
                       public val command: Int,
                       public val payload: ResponsePayload,
                       public val calculatedCrc: Int? = null,
                       public val expectedCrc: Int? = null) {
    companion object {
        fun parsePayload(originalCommand: Int, command: Int, payload: Buffer): ResponsePayload {
            return when (command) {
                AckResponse.COMMAND -> when (originalCommand) {
                    NullResponse.COMMAND -> NullResponse(payload)
                    AckResponse.COMMAND -> AckResponse(payload)
                    NakResponse.COMMAND -> NakResponse(payload)
                    InvalidCommandResponse.COMMAND -> InvalidCommandResponse(payload)
                    InvalidParam.COMMAND -> InvalidParam(payload)
                    IncompletePacket.COMMAND -> IncompletePacket(payload)
                    ReceiverError.COMMAND -> ReceiverError(payload)
                    InvalidMode.COMMAND -> InvalidMode(payload)
                    Ping.COMMAND -> Ping()
                    ReadFirmwareHeader.COMMAND -> ReadFirmwareHeaderResponse(payload)
                    ReadDatabasePartitionInfo.COMMAND -> ReadDatabasePartitionInfoResponse(payload)
                    ReadDataPageRange.COMMAND -> ReadDataPageRangeResponse(payload)
                    ReadDataPages.COMMAND -> ReadDataPagesResponse(payload)
                    ReadDataPageHeader.COMMAND -> ReadDataPageHeaderResponse(payload)
                    ReadLanguage.COMMAND -> ReadLanguageResponse(payload)
                    ReadDisplayTimeOffset.COMMAND -> ReadDisplayTimeOffsetResponse(payload)
                    ReadSystemTime.COMMAND -> ReadSystemTimeResponse(payload)
                    ReadSystemTimeOffset.COMMAND -> ReadSystemTimeOffsetResponse(payload)
                    ReadGlucoseUnit.COMMAND -> ReadGlucoseUnitResponse(payload)
                    ReadClockMode.COMMAND -> ReadClockModeResponse(payload)
                    else ->
                        InvalidCommandResponse(payload)
                }
                NakResponse.COMMAND -> NakResponse(payload)
                else ->
                    InvalidCommandResponse(payload)
            }
        }

        public fun parse(source: BufferedSource, originalCommand: Int): DexcomG4Response {
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
            val payload = parsePayload(originalCommand, command, payloadBuffer)
            crc = CRC.updateChecksum(crc, source.buffer().readByteArray(payloadLength))
            val calculatedCrc = crc xor CRC.DEXCOM_FINAL_XOR
            val expectedCrc = source.readShortLe().toInt() and 0xFFFF
            return DexcomG4Response(originalCommand, command, payload, calculatedCrc, expectedCrc)
        }
    }

    public val valid: Boolean get() =
    (expectedCrc == calculatedCrc) && command != NakResponse.COMMAND
}