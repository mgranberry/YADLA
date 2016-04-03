package com.kludgenics.alrightypump

import com.fazecast.jSerialComm.SerialPort
import com.kludgenics.alrightypump.cloud.nightscout.Nightscout
import com.kludgenics.alrightypump.device.dexcom.g4.DexcomG4
import com.kludgenics.alrightypump.device.tandem.*
import com.kludgenics.alrightypump.therapy.ConcurrentSkipListTherapyTimeline
import com.kludgenics.alrightypump.therapy.Record
import okhttp3.Cache
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.joda.time.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

val startTime = LocalDateTime.now() - Period.days(14)
val t = DateTime()

private fun downloadRecords(threads: MutableList<Thread>, lastUploads: MutableMap<String, LocalDateTime>, port: SerialPort,
                    block: (connection: SerialConnection, (Record) -> Boolean) -> LocalDateTime?) {
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
    val calls = nightscout?.prepareUploads(timeline.events)
    calls?.forEach { it.call.enqueue(object : Callback<ResponseBody> {
        override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
        }

        override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
        }
    }) }
    while (okHttpClient.dispatcher().queuedCallsCount() > 0) {
        println("Waiting on ${okHttpClient.dispatcher().queuedCallsCount()} records to finish uploading.")
        Thread.sleep(1000)
    }
}

fun main(args: Array<String>) {
    val okHttpClient = OkHttpClient.Builder().cache(Cache(File("/tmp/ok"), 1024 * 1024 * 50)).build()
    var lastUploads = ConcurrentHashMap<String, LocalDateTime>().withDefault { startTime }
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
                                    println("Final offset: ${tslim.timeCorrectionOffset}")
                                    timeline.merge(predicate, tslim.basalRecords, tslim.bolusRecords,
                                            tslim.smbgRecords, tslim.consumableRecords, tslim.profileRecords)
                                    timeline.events.lastOrNull()?.time
                                }
                                true
                            }
                            "DexCom Gen4 USB Serial" -> {
                                downloadRecords(threads, lastUploads, it) {
                                    connection, predicate ->
                                    val g4 = DexcomG4(connection.source(), connection.sink())
                                    println("Final offset: ${g4.timeCorrectionOffset}")
                                    g4.rawEnabled = true
                                    timeline.merge(predicate, g4.cgmRecords, g4.smbgRecords, g4.calibrationRecords,
                                            g4.eventRecords, g4.consumableRecords)
                                    timeline.events.lastOrNull()?.time
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
        timeline.events.forEach { println("${it.time} ${it}") }
        uploadRecords(nightscout, nightscout_url, okHttpClient, timeline)
        val end = Instant.now()
        println("Time to upload: ${Duration(mid, end)}")

        val sleepTime = (Duration(30000) - Duration(start, end)).millis
        if (sleepTime > 0)
            Thread.sleep(sleepTime)
    }
}
