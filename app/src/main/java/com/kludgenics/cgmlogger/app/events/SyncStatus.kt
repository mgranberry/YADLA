package com.kludgenics.cgmlogger.app.events

import android.bluetooth.BluetoothDevice
import android.hardware.usb.UsbDevice


data class SyncStatus(val statusText: String,
                      val isActive: Boolean,
                      val usbDevice: UsbDevice? = null,
                      val bleDevice: BluetoothDevice? = null,
                      val serialNumber: String? = null)