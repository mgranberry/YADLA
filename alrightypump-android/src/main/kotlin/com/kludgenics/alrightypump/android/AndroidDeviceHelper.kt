package com.kludgenics.alrightypump.android

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
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
    companion object {
        private fun getDevicesFor(context: Context, productId: Int, vendorId: Int) : List<AndroidSerialConnection> {
            val manager = context.getSystemService(Context.USB_SERVICE) as UsbManager;
            val devices: HashMap<String, UsbDevice>? = manager.deviceList
            return devices?.values?.filter { it.productId == productId && it.vendorId == vendorId }?.map {
                val connection = manager.openDevice(it)
                AndroidSerialConnection(it, connection)
            } ?: emptyList()
        }

        fun getTandemPumps (context: Context) : List<TandemPump> {
            return getDevicesFor(context, 0x5740, 0x0483).map {
                println(it)
                TandemPump(Okio.buffer(it as Source), Okio.buffer(it as Sink))
            }
        }

        fun getDexcomG4 (context: Context) : List<DexcomG4> {
            return getDevicesFor(context, 0x0047, 0x22a3).map {
                DexcomG4(Okio.buffer(it as Source), Okio.buffer(it as Sink))
            }
        }
    }
}