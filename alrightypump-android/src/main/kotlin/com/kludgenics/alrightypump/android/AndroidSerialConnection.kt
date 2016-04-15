package com.kludgenics.alrightypump.android

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface
import okio.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit

class AndroidSerialConnection(context: Context, device: UsbDevice): Source, Sink {

    private var closed: Boolean = false
    private val serial: UsbSerialDevice
    private val deviceConnection: UsbDeviceConnection
    private val blockingQueue = ArrayBlockingQueue<ByteArray>(128)

    private val readCallback: UsbSerialInterface.UsbReadCallback = object: UsbSerialInterface.UsbReadCallback {
        override fun onReceivedData(bytes: ByteArray) {
            blockingQueue.add(bytes)
        }
    }

    init {
        val manager = context.getSystemService(Context.USB_SERVICE) as UsbManager;
        deviceConnection = manager.openDevice(device)
        serial = UsbSerialDevice.createUsbSerialDevice(device, deviceConnection)
        serial.open()
        serial.read(readCallback)
    }

    var timeout = Timeout.NONE

    override fun read(sink: Buffer, byteCount: Long): Long {
        if (byteCount < 0) throw IllegalArgumentException("byteCount < 0: " + byteCount)
        return if (closed) {
            -1L
        } else {
            val buffer: ByteArray? = try {
                blockingQueue.poll(5, TimeUnit.SECONDS)
            } catch (e: InterruptedException) {
                null
            }
            if (buffer == null)
                if (closed) -1 else 0L
            else {
                val bytes = buffer.size
                if (bytes >= 0)
                    sink.write(buffer, 0, bytes)
                bytes.toLong()
            }
        }
    }

    override fun timeout(): Timeout {
        return timeout
    }

    @Synchronized
    override fun close() {
        closed = true
        serial.close()
     }

    override fun write(source: Buffer, byteCount: Long) {
        if (byteCount < 0) throw IllegalArgumentException("byteCount < 0: " + byteCount)
        var bytesWritten = 0
        source.require(byteCount)
        serial.write(source.readByteArray(byteCount - bytesWritten))
    }

    override fun flush() {
    }

}


