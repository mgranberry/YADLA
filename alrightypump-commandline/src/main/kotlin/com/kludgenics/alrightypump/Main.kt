package com.kludgenics.alrightypump

import com.fazecast.jSerialComm.SerialPort
import com.kludgenics.alrightypump.cloud.nightscout.Nightscout
import com.kludgenics.alrightypump.device.dexcom.g4.DexcomG4
import com.kludgenics.alrightypump.device.tandem.TandemPump
import com.kludgenics.alrightypump.therapy.ConcurrentSkipListTherapyTimeline
import com.kludgenics.alrightypump.therapy.Record
import com.squareup.okhttp.HttpUrl
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Instant
import org.joda.time.Period
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

fun downloadRecords(threads: MutableList<Thread>, lastUploads: MutableMap<String, DateTime>, port: SerialPort,
                    block: (connection: SerialConnection, (Record) -> Boolean) -> DateTime?) {
    println("Found ${port.descriptivePortName}. Fetching data.")
    threads.add (thread {
        try {
            val connection = SerialConnection(port)
            connection.use {
                val lastUpload = lastUploads.getOrImplicitDefault(port.descriptivePortName)
                val uploadStart = block(connection, { record -> record.time > lastUpload })
                if (uploadStart != null)
                    lastUploads[port.descriptivePortName] = uploadStart
            }
            println("Download from ${port.descriptivePortName} finished.")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    })
}

fun main(args: Array<String>) {
    val startTime = DateTime.now() - Period.months(3)
    var lastUploads = ConcurrentHashMap<String, DateTime>().withDefault { startTime }
    while (true) {
        var foundDevice = false
        val start = Instant.now()
        val timeline = ConcurrentSkipListTherapyTimeline()
        val threads = arrayListOf<Thread>()
        println("Waiting for a device to be recognized...")
        while (foundDevice == false) {
            SerialPort.getCommPorts()
                    .filterNot { it.descriptivePortName.contains("Dial-In") || it.descriptivePortName.contains("Bluetooth-") }
                    .forEach {
                        println("Looking for device on ${it.descriptivePortName}")
                        foundDevice = when (it.descriptivePortName) {
                            "Tandem Virtual COM Port" -> {
                                it.baudRate = 115200
                                downloadRecords(threads, lastUploads, it) {
                                    connection, predicate ->
                                    val tslim = TandemPump(connection.source(), connection.sink())
                                    timeline.merge(predicate, tslim.basalRecords, tslim.bolusRecords,
                                            tslim.smbgRecords, tslim.consumableRecords)
                                    timeline.events.lastOrNull()?.time?.toDateTime()
                                }
                                true
                            }
                            "DexCom Gen4 USB Serial" -> {
                                downloadRecords(threads, lastUploads, it) {
                                    connection, predicate ->
                                    val g4 = DexcomG4(connection.source(), connection.sink())
                                    g4.rawEnabled = true
                                    val cgms = g4.cgmRecords
                                    timeline.merge(predicate, g4.cgmRecords, g4.smbgRecords, g4.calibrationRecords,
                                            g4.eventRecords, g4.consumableRecords)
                                    timeline.events.lastOrNull()?.time?.toDateTime()
                                }
                                true
                            }
                            else -> false
                        } || foundDevice
                    }
            if (!foundDevice)
                Thread.sleep(1000)
        }
        val mid = Instant.now()

        threads.forEach { it.join() }
        threads.clear()
        println("Time to read from devices: ${Duration(start, mid)}")

        val nightscout_url = System.getenv("NIGHTSCOUT_HOST") ?: args.getOrNull(0)
        if (nightscout_url == null) {
            println("Error: must set environment variable NIGHTSCOUT_HOST or provide Nightscout URL on command line.  This should look like https://key@hostname.example.com:1234/")
        } else {
            val nightscout = Nightscout(HttpUrl.parse(nightscout_url))
            println("Uploading ${timeline.events.count()} records to $nightscout_url")
            nightscout.postRecords(timeline.events)
            while (nightscout.okHttpClient.dispatcher.queuedCallCount > 0) {
                println("Waiting on ${nightscout.okHttpClient.dispatcher.queuedCallCount} records to finish uploading.")
                Thread.sleep(1000)
            }
            nightscout.okHttpClient.dispatcher.executorService.shutdown()
        }
        val end = Instant.now()
        println("Time to upload: ${Duration(mid, end)}")

        val sleepTime = (Duration(30000) - Duration(start, end)).millis
        if (sleepTime > 0)
            Thread.sleep(sleepTime)
    }
}
