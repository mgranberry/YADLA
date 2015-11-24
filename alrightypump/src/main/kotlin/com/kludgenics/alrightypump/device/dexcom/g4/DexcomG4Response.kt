package com.kludgenics.alrightypump.device.dexcom.g4

import com.kludgenics.alrightypump.CRC
import com.kludgenics.alrightypump.ResponseFrame
import okio.Buffer
import okio.BufferedSource
import okio.Okio

/**
 * Created by matthias on 11/7/15.
 */


class DexcomG4Response(source: BufferedSource) : DexcomG4Frame, ResponseFrame {
    override val expectedChecksum: Int
    override val frame: Buffer
    override val payloadLength: Long
    override val command: Int get() = frame.getByte(3).toInt() and 0xFF
    override public val valid: Boolean
        get() = (expectedChecksum == calculatedChecksum) && command != NakResponse.COMMAND

    init {
        frame = Buffer()
        source.require(6)
        source.skip(source.indexOf(sync.toByte()))
        source.require(6)
        frame.writeByte(source.readByte().toInt())
        val length = source.readShortLe().toInt() and 0xFFFF
        frame.writeShortLe(length)
        source.require(length.toLong() - 3)
        source.read(frame, length.toLong() - 3)
        payloadLength = length.toLong() - 6
        expectedChecksum = footer.readShortLe().toInt() and 0xFFFF
    }


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


    }

    override fun toString() : String{
        return "${this.javaClass.simpleName}(header = [${header.snapshot().hex()} l:${headerLength} r: ${headerRange}], payload = [${payload.snapshot().hex()} l: $payloadLength r: $payloadRange], footer = [${footer.snapshot().hex()} l: ${footerLength} r: ${footerRange}]}"
    }

}