package com.kludgenics.alrightypump

import com.fazecast.jSerialComm.SerialPort
import okio.*
import java.io.Closeable
import java.io.IOException


class SerialConnection(private val port: SerialPort) : Closeable {

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

        val byteBuffer = ByteArray(2048)

        override fun read(sink: Buffer, byteCount: Long): Long {
            if (byteCount > Integer.MAX_VALUE)
                throw IllegalArgumentException("byteCount > Integer.MAX_VALUE: " + byteCount)
            timeout.enter()
            // println("$byteCount ${port.bytesAvailable()}")
            val toRead = Math.min(byteBuffer.size.toLong(), byteCount)
            val bytesRead = port.readBytes(byteBuffer, toRead)
            //println("SerialCommSource: read($sink, $byteCount): actual=$bytesRead")
            sink.write(byteBuffer, 0, bytesRead)
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
            if (byteCount > Integer.MAX_VALUE)
                throw IllegalArgumentException("byteCount > Integer.MAX_VALUE: " + byteCount)
            timeout.enter()
            //println("Writing $byteCount bytes")
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