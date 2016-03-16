package com.kludgenics.alrightypump

import com.fazecast.jSerialComm.SerialPort
import com.kludgenics.alrightypump.cloud.nightscout.Nightscout
import com.kludgenics.alrightypump.device.dexcom.g4.DexcomG4
import com.kludgenics.alrightypump.device.tandem.*
import com.kludgenics.alrightypump.therapy.ConcurrentSkipListTherapyTimeline
import com.kludgenics.alrightypump.therapy.Record
import com.squareup.okhttp.Cache
import com.squareup.okhttp.HttpUrl
import com.squareup.okhttp.OkHttpClient
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Instant
import org.joda.time.Period
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

val startTime = DateTime.now() - Period.days(1)


private fun downloadRecords(threads: MutableList<Thread>, lastUploads: MutableMap<String, DateTime>, port: SerialPort,
                    block: (connection: SerialConnection, (Record) -> Boolean) -> DateTime?) {
    println("Found ${port.descriptivePortName}. Fetching data.")
    threads.add (thread {
        try {
            val connection = SerialConnection(port)
            connection.use {
                val lastUpload = lastUploads.getOrElse(port.descriptivePortName, {startTime})
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

private fun uploadRecords(nightscout: Nightscout?, nightscout_url: String?, okHttpClient: OkHttpClient, timeline: ConcurrentSkipListTherapyTimeline) {
    println("Uploading ${timeline.events.count()} records to $nightscout_url")
    nightscout?.postRecords(timeline.events)
    while (okHttpClient.dispatcher.queuedCallCount > 0) {
        println("Waiting on ${okHttpClient.dispatcher.queuedCallCount} records to finish uploading.")
        Thread.sleep(1000)
    }
}

fun main(args: Array<String>) {
    val okHttpClient = OkHttpClient()
    okHttpClient.cache = Cache(File("/tmp/ok"), 1024 * 1024 * 50)
    var lastUploads = ConcurrentHashMap<String, DateTime>().withDefault { startTime }
    val nightscout_url = System.getenv("NIGHTSCOUT_HOST") ?: args.getOrNull(0)
    val nightscout = if (nightscout_url == null) {
        println("Error: must set environment variable NIGHTSCOUT_HOST or provide Nightscout URL on command line.  This should look like https://key@hostname.example.com:1234/")
        null
    } else {
        Nightscout(HttpUrl.parse(nightscout_url), okHttpClient)
    }

    while (nightscout != null) {
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
                                            tslim.smbgRecords, tslim.consumableRecords, tslim.profileRecords)
                                    timeline.events.lastOrNull()?.time?.toDateTime()
                                }
                                true
                            }
                            "DexCom Gen4 USB Serial" -> {
                                downloadRecords(threads, lastUploads, it) {
                                    connection, predicate ->
                                    val g4 = DexcomG4(connection.source(), connection.sink())
                                    g4.rawEnabled = true
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
        timeline.events.forEach { println(it) }
        //uploadRecords(nightscout, nightscout_url, okHttpClient, timeline)
        val end = Instant.now()
        println("Time to upload: ${Duration(mid, end)}")

        val sleepTime = (Duration(30000) - Duration(start, end)).millis
        if (sleepTime > 0)
            Thread.sleep(sleepTime)
    }
}
