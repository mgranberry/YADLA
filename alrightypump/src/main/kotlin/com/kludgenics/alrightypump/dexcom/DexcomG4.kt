package com.kludgenics.alrightypump.dexcom

import com.kludgenics.alrightypump.ContinuousGlucoseMonitor
import okio.BufferedSink
import okio.BufferedSource
import okio.ByteString

/**
 * Created by matthias on 11/5/15.
 */

open class DexcomG4(private val source: BufferedSource,
               private val sink: BufferedSink): ContinuousGlucoseMonitor {

    public val version: String by lazy { requestVersion() }

    private fun requestVersion(): String {
        val command = ReadFirmwareHeader()
        val response = commandResponse(command)
        return response.payload.toString()
    }

    public fun commandResponse(command: DexcomCommand): DexcomResponse {
        val packet = DexcomG4Packet(command.command, command).packet.snapshot()
        println("Writing ${packet.hex()}")
        sink.write(packet)
        val response = DexcomG4Response.parse(source, command.command)
        return response
    }
}

