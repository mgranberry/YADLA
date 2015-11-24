package com.kludgenics.alrightypump

import com.fazecast.jSerialComm.SerialPort
import com.kludgenics.alrightypump.device.dexcom.g4.DexcomG4
import com.kludgenics.alrightypump.device.dexcom.g4.RecordPage
import com.kludgenics.alrightypump.device.tandem.*
import com.kludgenics.alrightypump.therapy.BasalRecord
import com.kludgenics.alrightypump.therapy.TemporaryBasalRecord
import com.kludgenics.alrightypump.therapy.TreeMapTherapyTimeline
import org.joda.time.DateTime
import org.joda.time.Period


fun main(args: Array<String>) {
    val ports = SerialPort.getCommPorts()
    val timeline = TreeMapTherapyTimeline()
    ports.forEach {
        println(it.descriptivePortName)
        when (it.descriptivePortName) {
            "Tandem Virtual COM Port" -> {
                println(it.baudRate)
                it.baudRate = 115200 * 12
                println(it.baudRate)
                val connection = SerialConnection(it)
                println(it.baudRate)
                println(it.flowControlSettings)

                val tslim = TandemPump(connection.source(), connection.sink())
                val response = tslim.commandResponse(LogSizeReq())
                val lsr = LogSizeResp(response.payload)
                println("lsr: $lsr ${lsr.range}")
                val start = DateTime()
                val records = tslim.records
                        .takeWhile { it.timestamp >= DateTime.now() - Period.days(3) }

                val boluses = records.filterIsInstance<BolusEventRecord>().groupBy { it.bolusId }
                val tempBasals = records.filterIsInstance<BasalRecord>()
                //boluses.forEach { println(it) }
                tempBasals.forEach {
                    @Suppress("IMPLICIT_CAST_TO_UNIT_OR_ANY")
                    println("${it.time} ${it.rate} ${if (it is TemporaryBasalRecord) it.duration.standardMinutes else ""} ${if (it is TemporaryBasalRecord) it.percent else ""} $it}") }
                    // println("Fetched ${records.size} records in ${Duration(start, DateTime.now())}")
            }
            "DexCom Gen4 USB Serial" -> {
                val connection = SerialConnection(it)
                val g4 = DexcomG4(connection.source(), connection.sink())
                println(g4.ping())
                println(g4.version)
                println(g4.databasePartitionInfo)
                val pgs = g4.readDataPageRange(RecordPage.INSERTION_TIME)
                println("pages: $pgs")
                //val egvs = g4.egvRecords.asSequence().filterNot{it.skipped}
                //val sgvs = g4.sgvRecords.asSequence()
                //val zipped = egvs.zip(sgvs.asSequence())
                //zipped.forEach { println(it) } //println("${it.first.displayTime} ${it.second.displayTime}" ) }
                //g4.calibrationRecords.asSequence().take(5).forEach { println(it); println(it.displayTime) }
                //g4.eventRecords.forEach { println(it); println(it.displayTime.toDateTime()) }
                //g4.insertionRecords.forEach { println("insertionTime:${it.insertionTime.toDateTime()} displayTime:${it.displayTime.toDateTime()} $it") }
                //g4.meterRecords.forEach { println(it) }
                //g4.settingsRecords.forEach { println(it) }
                //g4.rawEnabled = true
                // g4.cgmRecords.forEach { println("${it.time}: ${it.value.mgdl} ${it.value.rawMgdl}") }
                //g4.cgmRecords.filter{(it.value as DexcomG4GlucoseValue).calibration != null} .forEach{ println("${(it.value as DexcomG4GlucoseValue).raw} ${it.value.glucose}")}
                //g4.calibrationRecords.forEach { println("$it ${it.displayTime}") }
                timeline.merge({it.time >= DateTime.parse("2015-11-19")}, g4.cgmRecords, g4.calibrationRecords, g4.eventRecords, g4.insertionRecords, g4.smbgRecords)
            }
        }
    }
    println(timeline.events)
    timeline.events.forEach { println("${it.time} $it") }
}
