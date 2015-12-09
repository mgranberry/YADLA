package com.kludgenics.alrightypump

import com.fazecast.jSerialComm.SerialPort
import com.kludgenics.alrightypump.cloud.nightscout.Nightscout
import com.kludgenics.alrightypump.device.dexcom.g4.DexcomG4
import com.kludgenics.alrightypump.device.tandem.TandemPump
import com.kludgenics.alrightypump.therapy.ConcurrentSkipListTherapyTimeline
import com.kludgenics.alrightypump.therapy.NavigableMapTherapyTimeline
import com.squareup.okhttp.HttpUrl
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Instant
import org.joda.time.Period
import kotlin.concurrent.thread


fun main(args: Array<String>) {
    val start = Instant()
    val ports = SerialPort.getCommPorts()
    val timeline = ConcurrentSkipListTherapyTimeline()
    val startTime = DateTime.now() - Period.months(3)
    val threads = arrayListOf<Thread>()
    ports.forEach {
        println("Looking for device on ${it.descriptivePortName}")
        when (it.descriptivePortName) {
            "Tandem Virtual COM Port" -> {
                threads.add(thread {
                    println("Found Tandem pump. Fetching data.")
                    it.baudRate = 115200 * 12
                    val connection = SerialConnection(it)
                    connection.use {
                        val tslim = TandemPump(connection.source(), connection.sink())
                        timeline.merge({ it.time >= startTime }, tslim.basalRecords, tslim.bolusRecords,
                                tslim.smbgRecords, tslim.consumableRecords)
                    }
                })
            }
            "DexCom Gen4 USB Serial" -> {
                threads.add(thread {
                    println("Found DexCom G4.  Fetching data.")
                    val connection = SerialConnection(it)
                    connection.use {
                        val g4 = DexcomG4(connection.source(), connection.sink())
                        timeline.merge({ it.time >= startTime }, g4.cgmRecords, g4.smbgRecords, g4.calibrationRecords,
                                g4.eventRecords, g4.consumableRecords)
                    }
                })
            }
        }
    }

    threads.forEach { println("join"); it.join(); println("joined") }
    val mid = Instant()
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
    val end = Instant()
    println("Time to upload: ${Duration(mid, end)}")
}
