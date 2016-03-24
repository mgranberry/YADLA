package com.kludgenics.alrightypump.android

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import com.kludgenics.alrightypump.device.Device
import com.kludgenics.alrightypump.device.dexcom.g4.DexcomG4
import com.kludgenics.alrightypump.device.tandem.TandemPump
import okio.Okio
import okio.Sink
import okio.Source
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
            val manager = context.getSystemService(Context.USB_SERVICE) as UsbManager;
            val connection = manager.openDevice(device)
            println("connection:${connection}")
            val serial = AndroidSerialConnection(device, connection)
            println("serial:$serial")
            serial.open()
            val res = when (device) {
                in getTandemPumps(context) -> DeviceEntry(serial, TandemPump(Okio.buffer(serial as Source), Okio.buffer(serial as Sink)))
                in getDexcomG4s(context) -> DeviceEntry(serial, DexcomG4(Okio.buffer(serial as Source), Okio.buffer(serial as Sink)))
                else -> {
                    serial.close()
                    null
                }
            }
            println("res: $res")
            return res
        }
    }
}