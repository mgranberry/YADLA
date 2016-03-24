package com.kludgenics.alrightypump.android

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.util.Log
import com.felhr.usbserial.UsbSerialDevice
import okio.Buffer
import okio.Source
import okio.Sink
import okio.Timeout

class AndroidSerialConnection(private val device: UsbDevice, private val deviceConnection: UsbDeviceConnection): Source, Sink {

    val manufacturerName: String get() = device.manufacturerName

    private val serial: UsbSerialDevice = UsbSerialDevice.createUsbSerialDevice(device, deviceConnection)

    var timeout: Timeout = Timeout.NONE

    override fun read(sink: Buffer, byteCount: Long): Long {
        if (byteCount < 0) throw IllegalArgumentException("byteCount < 0: " + byteCount)
        val buffer = ByteArray(byteCount.toInt())
        val bytes = serial.syncRead(buffer, 20000)
        if (bytes >= 0)
            sink.write(buffer, 0, bytes)
        return bytes.toLong()
    }

    override fun timeout(): Timeout {
        return timeout
    }

    fun open() {
        serial.syncOpen()
    }

    override fun close() {
        serial.close()
     }

    override fun write(source: Buffer, byteCount: Long) {
        if (byteCount < 0) throw IllegalArgumentException("byteCount < 0: " + byteCount)
        var bytesWritten = 0
        while (bytesWritten < byteCount) {
            val count = serial.syncWrite(source.readByteArray(byteCount - bytesWritten), 20000)
            bytesWritten += count
        }
    }

    override fun flush() {
    }

}


