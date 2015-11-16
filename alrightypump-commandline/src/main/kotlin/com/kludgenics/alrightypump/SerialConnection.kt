package com.kludgenics.alrightypump

import com.fazecast.jSerialComm.SerialPort
import okio.*
import java.io.Closeable


public class SerialConnection(private val port: SerialPort) : Closeable {

    init {
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 0, 0);
        port.openPort()
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

            val toRead = Math.max(Math.min(byteCount, port.bytesAvailable().toLong()), 0)
            if (toRead > 0)
                println("SerialCommSource: read($sink, $byteCount): toRead=$toRead")
            val bytes = ByteArray(toRead.toInt())
            port.readBytes(bytes, toRead)
            sink.write(bytes)
            timeout.exit()
            return toRead
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