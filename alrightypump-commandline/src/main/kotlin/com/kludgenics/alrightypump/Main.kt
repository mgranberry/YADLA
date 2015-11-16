package com.kludgenics.alrightypump

import com.fazecast.jSerialComm.SerialPort
import com.kludgenics.alrightypump.dexcom.DexcomG4
import com.kludgenics.alrightypump.dexcom.DexcomG4Packet
import com.kludgenics.alrightypump.dexcom.Ping


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
            }
        }
    }
    //val connection = SerialConnection(SerialPort.getCommPort(SerialPort.getCommPorts()))
}
