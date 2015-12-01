package com.kludgenics.alrightypump

import com.fazecast.jSerialComm.SerialPort
import com.kludgenics.alrightypump.cloud.nightscout.Nightscout
import com.kludgenics.alrightypump.cloud.nightscout.NightscoutSgvJson
import com.kludgenics.alrightypump.device.dexcom.g4.DexcomG4
import com.kludgenics.alrightypump.device.dexcom.g4.RecordPage
import com.kludgenics.alrightypump.device.tandem.LogSizeReq
import com.kludgenics.alrightypump.device.tandem.LogSizeResp
import com.kludgenics.alrightypump.device.tandem.TandemPump
import com.kludgenics.alrightypump.therapy.TreeMapTherapyTimeline
import com.squareup.okhttp.HttpUrl
import org.joda.time.DateTime


fun main(args: Array<String>) {
    val ports = SerialPort.getCommPorts()
    val timeline = TreeMapTherapyTimeline()
    ports.forEach {
        println(it.descriptivePortName)
        when (it.descriptivePortName) {
            "Tandem Virtual COM Port" -> {
                it.baudRate = 115200 * 12
                val connection = SerialConnection(it)

                val tslim = TandemPump(connection.source(), connection.sink())
                val response = tslim.commandResponse(LogSizeReq())
                val lsr = LogSizeResp(response.payload)
                println("lsr: $lsr ${lsr.range}")
                /*val start = DateTime()
                val bolusRecords = tslim.bolusRecords.takeWhile { it.time.toDateTime() > DateTime.now() - Period.days(2) }
                bolusRecords.forEach { println("${it.javaClass.simpleName}\t${it.time.toDateTime()}\tn:${it.deliveredNormal}\te:${it.deliveredExtended}\tdur:${it.extendedDuration}") }
                val basalRecords = tslim.basalRecords.takeWhile { it.time.toDateTime() > DateTime.now() - Period.days(60) }
                basalRecords.forEach {
                    when (it) {
                        is TemporaryBasalRecord -> {
                            println ("${it.javaClass.simpleName}\t${it.time.toDateTime()}\tr:${it.rate}\tp:${it.percent} d:${it.duration}")
                        }
                        is SuspendedBasalRecord -> {
                            println ("${it.javaClass.simpleName}\t${it.time.toDateTime()}\tr:${it.rate}")
                        }
                        is ScheduledBasalRecord -> {
                            println ("${it.javaClass.simpleName}\t${it.time.toDateTime()}\tr:${it.rate}\tsched:${it.schedule}")
                        }
                        else -> {
                            println ("${it.javaClass.simpleName}\t${it.time.toDateTime()}\t${it.rate}\t$it")
                        }
                    }
                }*/
                timeline.merge({it.time >= DateTime.parse("2015-11-25T00:00:01Z")},tslim.basalRecords, tslim.bolusRecords, tslim.smbgRecords)
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
    //
    val nightscout = Nightscout(HttpUrl.parse(""))
    nightscout.entryPageSize = 2880
    val r = nightscout.entries.takeWhile { it.date >= DateTime.parse("2015-7-19") }
    println("average: ${r.map { it.rawEntry }.filterIsInstance<NightscoutSgvJson>().map { it.sgv }.average()}")
    println(nightscout.entries.take(2).toArrayList())
    val e = nightscout.treatments
    e.forEach { println(it) }

    //timeline.events.forEach { println("${it.time.toDateTime()} $it") }
}
