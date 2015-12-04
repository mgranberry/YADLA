package com.kludgenics.alrightypump.android

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface
import okio.Buffer
import okio.Source
import okio.Sink
import okio.Timeout
import java.io.InterruptedIOException
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit

class AndroidSerialConnection(val device: UsbDevice, val deviceConnection: UsbDeviceConnection): Source, Sink,
        UsbSerialInterface.UsbReadCallback {

    val serial: UsbSerialDevice = {
        val device = UsbSerialDevice.createUsbSerialDevice(device, deviceConnection)
        device.read(this)
        device
    }()

    val readBuffer = Buffer()
    public var timeout: Timeout = Timeout.NONE
    val queue = ArrayBlockingQueue<ByteArray>(4)

    override fun read(sink: Buffer, byteCount: Long): Long {
        if (byteCount < 0) throw IllegalArgumentException("byteCount < 0: " + byteCount)
        return if (readBuffer.size() != 0L) {
            val toRead = if (byteCount > readBuffer.size()) readBuffer.size() else byteCount
            sink.write(readBuffer, toRead)
            toRead
        } else {
            val result = queue.poll(timeout.timeoutNanos(), TimeUnit.NANOSECONDS) ?:
                    throw InterruptedIOException("timeout reading $byteCount bytes")
            readBuffer.write(result)
            val toRead = if (byteCount > readBuffer.size()) readBuffer.size() else byteCount
            sink.write(readBuffer, toRead)
            toRead
        }
    }

    override fun timeout(): Timeout {
        return timeout
    }

    public fun open() {
        serial.open()
    }

    override fun close() {
        serial.close()
    }

    override fun write(source: Buffer, byteCount: Long) {
        if (byteCount < 0) throw IllegalArgumentException("byteCount < 0: " + byteCount)
        serial.write(source.readByteArray(byteCount))
    }

    override fun flush() {
    }

    override fun onReceivedData(data: ByteArray) {
        queue.put(data)
    }
}


