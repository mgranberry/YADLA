package com.kludgenics.alrightypump

import com.fazecast.jSerialComm.SerialPort
import okio.*
import java.io.Closeable
import java.io.IOException


public class SerialConnection(private val port: SerialPort) : Closeable {

    init {
        port.baudRate = 115200
        port.openPort()
        port.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING or SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 100, 0);

        val array = ByteArray(port.bytesAvailable())
        port.readBytes(array, array.size.toLong())
    }

    @JvmOverloads
    fun sink(timeout: AsyncTimeout = AsyncTimeout()): BufferedSink {
        return Okio.buffer(SerialCommSink(timeout))
    }

    @JvmOverloads
    fun source(timeout: AsyncTimeout = AsyncTimeout()): BufferedSource {
        return Okio.buffer(SerialCommSource(timeout))
    }

    override fun close() {
        port.closePort()
    }

    private inner class SerialCommSource(private val timeout: AsyncTimeout) : Source {

        override fun read(sink: Buffer, byteCount: Long): Long {
            timeout.enter()
            println("$byteCount ${port.bytesAvailable()}")
            val toRead = Math.max(Math.min(byteCount, port.bytesAvailable().toLong()), byteCount)
            println("SerialCommSource: read($sink, $byteCount): toRead=$toRead")
            if (toRead == 0L)
                throw  IOException()
            val bytes = ByteArray(toRead.toInt())
            val bytesRead = port.readBytes(bytes, toRead)
            println("SerialCommSource: read($sink, $byteCount): toRead=$toRead actual=$bytesRead")

            sink.write(bytes, 0, bytesRead)
            timeout.exit()
            return bytesRead.toLong()
        }

        override fun timeout(): Timeout {
            return timeout;
        }

        override fun close() {
        }

    }

    private inner class SerialCommSink(private val timeout: AsyncTimeout) : Sink {

        override fun flush() {
        }

        override fun write(source: Buffer, byteCount: Long) {
            timeout.enter()
            println("Writing $byteCount bytes")
            port.writeBytes(source.readByteArray(byteCount), byteCount)
            timeout.exit()
        }

        override fun timeout(): Timeout? {
            throw UnsupportedOperationException()
        }

        override fun close() {
        }

    }

}