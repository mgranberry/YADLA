package com.kludgenics.alrightypump.android

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context

/**
 * Created by matthias on 3/31/16.
 */
class BleScanner(val applicationContext: Context, val statusCallback: StatusCallback) {
    companion object {
        const val MODE_RESULT_SINGLE = 0
        const val MODE_RESULT_MULTIPLE = 1
    }

    interface StatusCallback {
        fun onFailurePermissionsNecessary()
        fun onFailureBluetoothDisabled()
        fun onFailure (message: String?)
        fun onDeviceDiscovery ()
        fun onScanResult (result: ScanResult)
        fun onScanResultFirstMatch(result: ScanResult)
        fun onScanResultLost (result: ScanResult)
        fun onScanResultAllMatches(result: ScanResult)
    }

    private interface Scanner {
        fun startScan()
        fun stopScan()
    }

    private inner open class LollipopScanner(val adapter: BluetoothAdapter, val mode: Int): Scanner {
        val scanner = adapter.bluetoothLeScanner
        val scanCallback = object : ScanCallback() {
            override fun onBatchScanResults(results: List<ScanResult>) {
                results.forEach { statusCallback.onScanResult(it) }
            }

            override fun onScanFailed(errorCode: Int) {
                statusCallback.onFailure("Lollipop BLE scan failed with errror code $errorCode")
            }

            override fun onScanResult(callbackType: Int, result: ScanResult) {
                when (callbackType) {
                    ScanSettings.CALLBACK_TYPE_ALL_MATCHES -> statusCallback.onScanResultAllMatches(result)
                    ScanSettings.CALLBACK_TYPE_FIRST_MATCH -> statusCallback.onScanResultFirstMatch(result)
                    ScanSettings.CALLBACK_TYPE_MATCH_LOST -> statusCallback.onScanResultLost(result)
                }
                statusCallback.onScanResult(result)
            }
        }

        override fun startScan() {
            val filters = listOf<ScanFilter>(ScanFilter.Builder().setDeviceName("DEXCOMRX").build())
            val scanCallbackType = when (mode) {
                MODE_RESULT_MULTIPLE -> ScanSettings.CALLBACK_TYPE_ALL_MATCHES
                MODE_RESULT_SINGLE -> ScanSettings.CALLBACK_TYPE_FIRST_MATCH
                else -> ScanSettings.CALLBACK_TYPE_FIRST_MATCH
            }
            if (scanner == null) {
                statusCallback.onFailureBluetoothDisabled()
                statusCallback.onFailure("Bluetooth is disabled.")
                return
            }
            scanner.startScan(filters,
                    ScanSettings.Builder()
                            .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                            .setReportDelay(1000)
                            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                            .setCallbackType(scanCallbackType).build(),
                    scanCallback)
        }

        override fun stopScan() {
            if (scanner == null) {
                statusCallback.onFailureBluetoothDisabled()
                statusCallback.onFailure("Bluetooth is disabled.")
                return
            }
            try {
                scanner.stopScan(scanCallback)
            } catch (e: IllegalStateException) {
                statusCallback.onFailure(e.message)
            }
        }
    }

    private inner class MarshmallowScanner(adapter: BluetoothAdapter, mode: Int) : LollipopScanner(adapter, mode) {
        /// TODO request permissions etc
    }

    private inner class KitkatScanner(val adapter: BluetoothAdapter) : Scanner {
        val scanCallback: BluetoothAdapter.LeScanCallback = object : BluetoothAdapter.LeScanCallback {
            override fun onLeScan(device: BluetoothDevice, rssi: Int, scanRecord: ByteArray) {
                throw UnsupportedOperationException()
            }

        }
        override fun startScan() {
            adapter.startLeScan(scanCallback)
        }

        override fun stopScan() {
            adapter.stopLeScan(scanCallback)
        }
    }

    private lateinit var scanner: Scanner

    fun startScan(mode: Int = MODE_RESULT_MULTIPLE) {
        val bluetoothManager: BluetoothManager = applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
        if (bluetoothAdapter == null) {
            statusCallback.onFailure("Failed to obtain Bluetooth adapter.  Check location permissions and Bluetooth settings.")
            return;
        }
        scanner = LollipopScanner(bluetoothAdapter, mode)
        scanner.startScan()
    }

    fun stopScan() {
        scanner.stopScan()
    }
}