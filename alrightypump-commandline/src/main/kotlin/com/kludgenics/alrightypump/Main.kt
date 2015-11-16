package com.kludgenics.alrightypump

import com.fazecast.jSerialComm.SerialPort
import com.kludgenics.alrightypump.dexcom.*
import okio.Buffer


val command = DexcomG4Packet(Ping.COMMAND, Ping())

fun main (args: Array<String>) {
    val ports = SerialPort.getCommPorts()
    ports.forEach {
        println(it.descriptivePortName);
        when (it.descriptivePortName) {
            "DexCom Gen4 USB Serial" -> {
                val connection = SerialConnection(it)
                val g4 = DexcomG4(connection.source(), connection.sink())
                println(g4.version)
                val buffer = Buffer()
                buffer.writeByte(4)
                buffer.writeIntLe(0x000005ba)
                buffer.writeByte(1)
                //buffer.writeUtf8("CalSet")
                val response = g4.commandResponse(ReadDataPageHeader(buffer))
                println(response.command)
                println(response.payload)
                println(RecordPage.parse(response.payload.payload))
            }
        }
    }
    //val connection = SerialConnection(SerialPort.getCommPort(SerialPort.getCommPorts()))
}
