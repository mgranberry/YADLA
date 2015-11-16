package com.kludgenics.alrightypump.dexcom

import okio.ByteString

/**
 * Created by matthias on 11/5/15.
 */
interface Payload {
    companion object {
        fun parseResponse(originalCommand: Int, command: Int, payload: ByteArray): Payload {
            return when (command) {
                AckCommand.COMMAND -> when (originalCommand) {
                    NullCommand.COMMAND -> NullCommand(payload)
                    AckCommand.COMMAND -> AckCommand(payload)
                    NakCommand.COMMAND -> NakCommand(payload)
                    InvalidCommand.COMMAND -> InvalidCommand(payload)
                    InvalidParam.COMMAND -> InvalidCommand(payload)
                    IncompletePacket.COMMAND -> IncompletePacket(payload)
                    ReceiverError.COMMAND -> ReceiverError(payload)
                    InvalidMode.COMMAND -> InvalidMode(payload)
                    Ping.COMMAND -> Ping()
                    ReadFirmwareHeader.COMMAND -> ReadFirmwareHeader(payload)
                    ReadDatabasePartitionInfo.COMMAND -> ReadDatabasePartitionInfo(payload)
                    ReadDataPageRange.COMMAND -> ReadDataPageRange(payload)
                    ReadDataPages.COMMAND -> ReadDataPages(payload)
                    ReadDataPageHeader.COMMAND -> ReadDataPageHeader(payload)
                    ReadLanguage.COMMAND -> ReadLanguage(payload)
                    ReadDisplayTimeOffset.COMMAND -> ReadDisplayTimeOffset(payload)
                    ReadSystemTime.COMMAND -> ReadSystemTime(payload)
                    ReadSystemTimeOffset.COMMAND -> ReadSystemTimeOffset(payload)
                    ReadGlucoseUnit.COMMAND -> ReadGlucoseUnit(payload)
                    ReadClockMode.COMMAND -> ReadClockMode(payload)
                    else ->
                        UnsupportedPayload(command, payload)
                }
                NakCommand.COMMAND -> NakCommand(payload)
                else ->
                    UnsupportedPayload(command, payload)

            }
        }
    }

    val command: Int
    val payload: ByteArray
}

