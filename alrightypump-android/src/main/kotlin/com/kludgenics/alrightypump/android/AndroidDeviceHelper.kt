package com.kludgenics.alrightypump.android

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log
import com.kludgenics.alrightypump.device.Device
import com.kludgenics.alrightypump.device.dexcom.g4.DexcomG4
import com.kludgenics.alrightypump.device.tandem.TandemPump
import okio.*
import java.util.*

/**
 * Created by matthias on 12/13/15.
 */
class AndroidDeviceHelper private constructor() {
    data class DeviceEntry(val serialConnection: AndroidSerialConnection,
                           val device: Device)
    companion object {
        private fun getDevicesFor(context: Context, productId: Int, vendorId: Int) : List<UsbDevice> {
            val manager = context.getSystemService(Context.USB_SERVICE) as UsbManager;
            val devices: HashMap<String, UsbDevice>? = manager.deviceList
            return devices?.values?.filter { it.productId == productId && it.vendorId == vendorId } ?: emptyList()
        }

        fun getTandemPumps (context: Context) : List<UsbDevice> =
                getDevicesFor(context, 0x5740, 0x0483)


        fun getDexcomG4s (context: Context) : List<UsbDevice> =
                getDevicesFor(context, 0x0047, 0x22a3)

        fun getDeviceEntry (context: Context, device: UsbDevice): DeviceEntry? {
            val serial = AndroidSerialConnection(context, device)
            var entry: DeviceEntry? = null
            try {
                entry = when (device) {
                    in getTandemPumps(context) -> DeviceEntry(serial, TandemPump(source(serial), sink(serial)))
                    in getDexcomG4s(context) -> DeviceEntry(serial, DexcomG4(source(serial), sink(serial)))
                    else -> {
                        null
                    }
                }
            } finally {
                if (entry == null)
                    serial.close()
            }
            return entry
        }

        fun source(connection: AndroidSerialConnection): BufferedSource {
            return Okio.buffer(timeout(connection).source(connection))
        }

        fun sink(connection: AndroidSerialConnection): BufferedSink {
            return Okio.buffer(timeout(connection).sink(connection))
        }

        private fun timeout(connection: AndroidSerialConnection) = object : AsyncTimeout() {
            override fun timedOut() {
                super.timedOut()
                Log.d("Timeout", "Timed out!")
                connection.close()
            }
        }

    }
}