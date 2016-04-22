package com.kludgenics.alrightypump.android

import android.bluetooth.*
import android.content.Context
import android.os.Build
import android.util.Log
import okio.*
import java.io.Closeable
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit

object ShareUuids {
    val POWER_LEVEL: UUID by lazy { UUID.fromString("00002a07-0000-1000-8000-00805f9b34fb") }
    val VENDOR: UUID by lazy { UUID.fromString("F0ACA0B1-EBFA-F96F-28DA-076C35A521DB") }

    val UART_SERVICE_1: UUID
            by lazy { UUID.fromString("F0ABA0B1-EBFA-F96F-28DA-076C35A521DB") }
    val UART_SERVICE_2: UUID
            by lazy { UUID.fromString("F0ACA0B1-EBFA-F96F-28DA-076C35A521DB") }

    val AUTHENTICATION_CODE_1: UUID
            by lazy { UUID.fromString("F0ABACAC-EBFA-F96F-28DA-076C35A521DB") }
    val AUTHENTICATION_CODE_2: UUID
            by lazy { UUID.fromString("F0ACACAC-EBFA-F96F-28DA-076C35A521DB") }

    val SHARE_MESSAGE_TX_1: UUID
            by lazy { UUID.fromString("F0ABB20A-EBFA-F96F-28DA-076C35A521DB") }
    val SHARE_MESSAGE_TX_2: UUID
            by lazy { UUID.fromString("F0ACB20A-EBFA-F96F-28DA-076C35A521DB") }

    val SHARE_MESSAGE_RX_1: UUID
            by lazy { UUID.fromString("F0ABB20B-EBFA-F96F-28DA-076C35A521DB") }
    val SHARE_MESSAGE_RX_2: UUID
            by lazy { UUID.fromString("F0ACB20B-EBFA-F96F-28DA-076C35A521DB") }

    val COMMAND_1: UUID
            by lazy { UUID.fromString("F0ABB0CC-EBFA-F96F-28DA-076C35A521DB") }
    val COMMAND_2: UUID
            by lazy { UUID.fromString("F0ACB0CC-EBFA-F96F-28DA-076C35A521DB") }

    val RESPONSE_1: UUID
            by lazy { UUID.fromString("F0ABB0CD-EBFA-F96F-28DA-076C35A521DB") }
    val RESPONSE_2: UUID
            by lazy { UUID.fromString("F0ACB0CD-EBFA-F96F-28DA-076C35A521DB") }

    val HEARTBEAT_1: UUID
            by lazy { UUID.fromString("F0AB2B18-EBFA-F96F-28DA-076C35A521DB") }
    val HEARTBEAT_2: UUID
            by lazy { UUID.fromString("F0AC2B18-EBFA-F96F-28DA-076C35A521DB") }

    val CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR: UUID
            by lazy { UUID.fromString("00002902-0000-1000-8000-00805f9b34fb") }

}

class ShareGatt(context: Context, val device: BluetoothDevice, private val onConnected: (ShareGatt)->Unit, private val onDisconnected: (ShareGatt)->Unit, private val onError: (ShareGatt, message: String)->Unit) : Closeable {
    val TAG = DexcomShareBleConnection::class.java.simpleName

    val shareService: BluetoothGattService by lazy {
        gatt.services.first {
            it.uuid == ShareUuids.UART_SERVICE_1
                    || it.uuid == ShareUuids.UART_SERVICE_2
        }
    }

    val txCharacteristic: BluetoothGattCharacteristic by lazy {
        shareService.characteristics.first {
            it.uuid == ShareUuids.SHARE_MESSAGE_TX_1
                    || it.uuid == ShareUuids.SHARE_MESSAGE_TX_2
        }
    }

    val rxCharacteristic: BluetoothGattCharacteristic by lazy {
        shareService.characteristics.first {
            it.uuid == ShareUuids.SHARE_MESSAGE_RX_1
                    || it.uuid == ShareUuids.SHARE_MESSAGE_RX_2
        }
    }

    val authCharacteristic: BluetoothGattCharacteristic by lazy {
        shareService.characteristics.first {
            it.uuid == ShareUuids.AUTHENTICATION_CODE_1
                    || it.uuid == ShareUuids.AUTHENTICATION_CODE_2
        }
    }

    val heartbeatCharacteristic: BluetoothGattCharacteristic by lazy {
        shareService.characteristics.first {
            it.uuid == ShareUuids.HEARTBEAT_1
                    || it.uuid == ShareUuids.HEARTBEAT_2
        }
    }

    val responseCharacteristic: BluetoothGattCharacteristic by lazy {
        shareService.characteristics.first {
            it.uuid == ShareUuids.RESPONSE_1 || it.uuid == ShareUuids.RESPONSE_2
        }
    }

    private val commandQueue: ArrayBlockingQueue<() -> Unit> = ArrayBlockingQueue(50)
    @Volatile private var commandPending: Boolean = false

    inline private fun BluetoothGatt.execute(crossinline block: () -> Boolean) {
        synchronized(commandPending) {
            if (commandPending) {
                Log.d(TAG, "queueing command")
                commandQueue.add {
                    val result = block()
                    if (result == false)
                        onError(this@ShareGatt, "Failed to execute command.")
                }
            } else {
                Log.d(TAG, "executing command immediately")
                commandPending = true
                val result = block()
                if (result == false)
                    onError(this@ShareGatt, "Failed to execute command.")
                else
                    false
            }
        }
    }

    internal val readQueue = ArrayBlockingQueue<ByteArray>(100)
    private val writeQueue = ArrayBlockingQueue<ByteArray>(100)

    private val gattCallback = object : BluetoothGattCallback() {
        var attempts = 0

        fun postNextCommand() {
            val next = commandQueue.poll()
            synchronized(commandPending) {
                if (next != null) {
                    Log.d(TAG, "posting queued command")
                    next()
                } else {
                    commandPending = false
                    Log.d(TAG, "no queued command exists")
                }
            }
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            Log.d(TAG, "!!!!!!!!!!!!!!onConnectionStateChange($gatt, $status, $newState)")
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d(TAG, "gatt connected.  Discovering services.")
                    val result = gatt.discoverServices()
                    if (!result)
                        gatt.close()
                    // don't actually call onConnected here because service discovery is required
                    // before the connection is fully established.
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    if (status == 19 && ++attempts < 3) {
                        Log.d(TAG, "reattempting connection")
                        gatt.connect()
                    } else {
                        gatt.disconnect()
                        gatt.close()
                        onDisconnected(this@ShareGatt)
                    }
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            Log.d(TAG, "!!!!!!!!!!!!!!onServicesDiscovered($gatt, $status)")
            Log.d(TAG, "onServicesDiscovered, ${gatt.device} ${gatt.device.bondState}")
            BluetoothGatt.GATT_CONNECTION_CONGESTED
            setupReceiver()
            setupAuthentication()
            gatt.execute {
                commandPending = false
                onConnected(this@ShareGatt) // call onConnected now that services have been
                // discovered and parameters have been set
                true;
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic?, status: Int) {
            Log.d(TAG, "!!!!!!!!!!!!!!onCharacteristicRead($gatt, $characteristic, $status)")
            postNextCommand()
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    when (characteristic) {
                        heartbeatCharacteristic -> {
                            gatt.setCharacteristicNotification(heartbeatCharacteristic, true)
                            gatt.execute { Log.d(TAG, "heartbeat"); gatt.readCharacteristic(heartbeatCharacteristic) }
                        }
                        rxCharacteristic -> {
                            Log.d(TAG, "rxCharacteristic")
                            readQueue.add(characteristic.value)
                        }
                    }
                }
            }
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor?, status: Int) {
            Log.d(TAG, "!!!!!!!!!!!!!!onDescriptorWrite($gatt, $descriptor, $status)")
            postNextCommand()
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt, rssi: Int, status: Int) {
            Log.d(TAG, "!!!!!!!!!!!!!!onReadRemoteRssi($gatt, $rssi, $status)")
        }

        override fun onDescriptorRead(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor?, status: Int) {
            Log.d(TAG, "!!!!!!!!!!!!!!onDescriptorRead($gatt, $descriptor, $status)")
            postNextCommand()
        }

        override fun onReliableWriteCompleted(gatt: BluetoothGatt, status: Int) {
            Log.d(TAG, "!!!!!!!!!!!!!!onReliableWriteCompleted($gatt, $status)")
            postNextCommand()
        }


        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic?) {
            Log.d(TAG, "onCharacteristicChanged($gatt, $characteristic(${characteristic?.uuid}=${characteristic?.value?.size})")
            when (characteristic) {
                heartbeatCharacteristic -> {
                    // keep this to see when new data is available
                }
                rxCharacteristic -> {
                    readQueue.add(characteristic.value)
                    if (characteristic.value.size < 20 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_BALANCED)
                }
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic?, status: Int) {
            Log.d(TAG, "!!!!!!!!!!!!!!onCharacteristicWrite($gatt, (${characteristic?.uuid}=${characteristic?.value}), $status)")
            postNextCommand()
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    val bytes: ByteArray? = writeQueue.poll()
                    if (bytes != null) {
                        val result = gatt.execute {
                            txCharacteristic.value = bytes
                            Log.d(TAG, "writeChar ${txCharacteristic.value.size} bytes");
                            gatt.writeCharacteristic(txCharacteristic)
                        }
                        Log.d(TAG, "onCharacteristicWrite result is $result")
                    }
                }
                else -> onError(this@ShareGatt, "Unexpected GATT status $status during write.")
            }
        }
    }

    val gatt: BluetoothGatt = device.connectGatt(context, false, gattCallback)

    private fun setupAuthentication() {
        Log.d(TAG, "authenticating device")
        val bondingKey = "SM50556137000000".toByteArray()
        gatt.execute {
            Log.d(TAG, "authCharacteristic")
            authCharacteristic.value = bondingKey
            gatt.writeCharacteristic(authCharacteristic)
        }
    }

    private fun setupReceiver() {
        Log.d(TAG, "configuring heartbeat characteristic")
        setupNotification(heartbeatCharacteristic)
        Log.d(TAG, "configuring rx characteristic")
        setupIndication(rxCharacteristic)
        Log.d(TAG, "configuring response characteristic")
        setupIndication(responseCharacteristic)
    }

    private fun setupNotification(characteristic: BluetoothGattCharacteristic, enabled: Boolean = true) {
        var ret = gatt.setCharacteristicNotification(characteristic, enabled)
        if (!ret) {
            onError(this@ShareGatt, "Failed to ${if (enabled) "enable" else "disable"} notifications for ${characteristic.uuid}.")
            return
        }
        gatt.execute { Log.d(TAG, "descriptor");
            val descriptor = characteristic.getDescriptor(ShareUuids.CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR)
            descriptor.value = if (enabled) BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE else BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
            gatt.writeDescriptor(descriptor)
        }
    }

    private fun setupIndication(characteristic: BluetoothGattCharacteristic, enabled: Boolean = true) {
        var ret = gatt.setCharacteristicNotification(characteristic, enabled)
        if (!ret) {
            onError(this@ShareGatt, "Failed to ${if (enabled) "enable" else "disable"} notifications for ${characteristic.uuid}.")
            return
        }
        gatt.execute {
            Log.d(TAG, "descriptor")
            val descriptor = characteristic.getDescriptor(ShareUuids.CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR)
            descriptor.value = if (enabled) BluetoothGattDescriptor.ENABLE_INDICATION_VALUE else BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
            gatt.writeDescriptor(descriptor)
        }
    }

    fun write(bytes: ByteArray) {
        val chunkedBytes = bytes.asSequence().chunked(20).map { it.toByteArray() }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)
        chunkedBytes.forEach {
            writeQueue.put(it)
            gatt.execute {
                val nextValue = writeQueue.poll()
                if (nextValue != null) {
                    txCharacteristic.value = nextValue
                    Log.d(TAG, "writing ${txCharacteristic.value.size} bytes");
                    gatt.writeCharacteristic(txCharacteristic)
                } else false
            }
       }
    }

    fun read(): ByteArray? {
        try {
            return readQueue.poll(5, TimeUnit.SECONDS) ?: null
        } catch (e: InterruptedException) {
            return read()
        }
    }

    override fun close() {
        gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_LOW_POWER)
        gatt.disconnect()
        gatt.close()
    }
}

class DexcomShareBleConnection(val applicationContext: Context) : Source, Sink {

    val TAG = DexcomShareBleConnection::class.java.simpleName
    var gatt: ShareGatt? = null

    companion object {
        fun source(connection: DexcomShareBleConnection): BufferedSource {
            return Okio.buffer(timeout(connection).source(connection))
        }

        fun sink(connection: DexcomShareBleConnection): BufferedSink {
            return Okio.buffer(timeout(connection).sink(connection))
        }

        private fun timeout(dexcomShareBleConnection: DexcomShareBleConnection) = object : AsyncTimeout() {
            override fun timedOut() {
                super.timedOut()
                Log.d("Timeout", "Timed out!")
                dexcomShareBleConnection.close()
            }
        }
    }

    @Synchronized
    fun connect(device: BluetoothDevice, onConnected: (ShareGatt)->Unit,
                onDisconnected: (ShareGatt)->Unit,
                onError: (ShareGatt, message: String)->Unit) {
        val immutableGatt = gatt
        if (immutableGatt != null && immutableGatt.device != device) {
            immutableGatt.close()
        } else if (device != gatt?.device) {
            val connectTimeout = ConnectTimeout(onError)
            connectTimeout.timeout(30, TimeUnit.SECONDS)
            connectTimeout.enter()
            gatt = ShareGatt(applicationContext, device, {shareGatt ->
                connectTimeout.exit()
                onConnected(shareGatt)
            }, onDisconnected, onError)
            connectTimeout.shareGatt = gatt
        }
    }

    override fun timeout(): Timeout = Timeout.NONE

    @Synchronized
    override fun close() {
        gatt?.close()
        gatt = null
    }

    override fun read(sink: Buffer, byteCount: Long): Long {
        val bytes = gatt?.read()
        val resultCount = bytes?.size?.toLong() ?: -1L
        if (bytes != null)
            sink.write(bytes)
        return resultCount
    }

    override fun flush() {
    }

    override fun write(source: Buffer, byteCount: Long) {
        gatt?.write(source.readByteArray(byteCount))
    }

    private class ConnectTimeout(val onError: (ShareGatt, message: String)->Unit) : AsyncTimeout() {
        var shareGatt: ShareGatt? = null
        override fun timedOut() {
            val gatt = shareGatt
            if (gatt != null) {
                gatt.close()
                onError(gatt, "Connection timed out.")
            }
        }
    }
}

fun <T> Sequence<T>.chunked(size: Int): Sequence<List<T>> {
    val iterator = this.iterator()

    return object : Sequence<List<T>> {
        override fun iterator() = object : AbstractIterator<List<T>>() {
            override fun computeNext() {
                val next = ArrayList<T>(size)
                while (iterator.hasNext() && next.size < size) {
                    next.add(iterator.next())
                }
                if (next.isEmpty()) done() else setNext(next)
            }
        }
    }
}