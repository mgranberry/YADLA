package com.kludgenics.alrightypump

import com.fazecast.jSerialComm.SerialPort
import com.kludgenics.alrightypump.dexcom.*
import okio.Buffer
import org.joda.time.DateTime


val command = DexcomG4Packet(Ping.COMMAND, Ping())

fun main (args: Array<String>) {
    val ports = SerialPort.getCommPorts()
    ports.forEach {
        when (it.descriptivePortName) {
            "DexCom Gen4 USB Serial" -> {
                val connection = SerialConnection(it)

                val g4 = DexcomG4(connection.source(), connection.sink())
                println(g4.version)
                println(g4.databasePartitionInfo)
                //val pgs = g4.readDataPageRange(RecordPage.EGV_DATA)
                //println("pages: $pgs")
                val egvs = g4.egvs.asSequence().filterNot{it.skipped}
                egvs.first()
                val sgvs = g4.sgvs.asSequence()
                sgvs.first()
                //val zipped = egvs.zip(sgvs.asSequence())
                //zipped.forEach { } //println("${it.first.displayTime} ${it.second.displayTime}" ) }
                //val firstFive = egvs.asSequence().take(5)
                //egvs.asSequence().filter { !it.skipped }
                //        .forEach { println("${it.displayTime.toDateTime()} ${it.rawGlucose} ${it.glucose} ${it.skipped}"); println(it) }
                g4.calibrations.asSequence().take(5).forEach { println(it); println(it.displayTime) }
                //g4.events.forEach { /*println(it); println(it.displayTime.toDateTime())*/}
                //g4.insertions.forEach { /*println(it); println(it.displayTime) ; println(it.systemTime)*/ }
                //g4.meters.forEach { /*println(it)*/ }
                //g4.settings.forEach { /*println(it)*/ }
            }
        }
    }
    //val connection = SerialConnection(SerialPort.getCommPort(SerialPort.getCommPorts()))
}
