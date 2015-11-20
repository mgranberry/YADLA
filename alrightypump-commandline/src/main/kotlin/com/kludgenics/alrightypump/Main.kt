package com.kludgenics.alrightypump

import com.fazecast.jSerialComm.SerialPort
import com.kludgenics.alrightypump.dexcom.DexcomG4
import com.kludgenics.alrightypump.dexcom.RecordPage


fun main(args: Array<String>) {
    val ports = SerialPort.getCommPorts()
    ports.forEach {
        when (it.descriptivePortName) {
            "DexCom Gen4 USB Serial" -> {
                val connection = SerialConnection(it)
                val g4 = DexcomG4(connection.source(), connection.sink())
                println(g4.ping())
                println(g4.version)
                println(g4.databasePartitionInfo)
                val pgs = g4.readDataPageRange(RecordPage.EGV_DATA)
                println("pages: $pgs")
                //val egvs = g4.egvRecords.asSequence().filterNot{it.skipped}
                //val sgvs = g4.sgvRecords.asSequence()
                //val zipped = egvs.zip(sgvs.asSequence())
                //zipped.forEach { println(it) } //println("${it.first.displayTime} ${it.second.displayTime}" ) }
                //g4.calibrationRecords.asSequence().take(5).forEach { println(it); println(it.displayTime) }
                //g4.eventRecords.forEach { println(it); println(it.displayTime.toDateTime()) }
                //g4.insertionRecords.forEach { println(it); println(it.displayTime) ; println(it.systemTime) }
                //g4.meterRecords.forEach { println(it) }
                //g4.settingsRecords.forEach { println(it) }
                g4.rawEnabled = true
                g4.cgmRecords.forEach { println("${it.time}: ${it.value.mgdl} ${it.value.rawMgdl}") }
                //g4.cgmRecords.filter{(it.value as DexcomG4GlucoseValue).calibration != null} .forEach{ println("${(it.value as DexcomG4GlucoseValue).raw} ${it.value.glucose}")}
                //g4.calibrationRecords.forEach { println("$it ${it.displayTime}") }
            }
        }
    }
}
