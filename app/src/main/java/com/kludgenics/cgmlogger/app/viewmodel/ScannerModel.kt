package com.kludgenics.cgmlogger.app.viewmodel

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.content.Context
import android.databinding.Bindable
import android.databinding.ObservableArrayList
import android.databinding.ObservableList
import android.databinding.PropertyChangeRegistry
import com.kludgenics.alrightypump.android.BleScanner
import com.kludgenics.cgmlogger.app.BR
import java.io.Closeable

class ScannerModel(applicationContext: Context) : DataBindingObservable, BleScanner.StatusCallback, Closeable {

    data class Result(val device: BluetoothDevice,
                      var name: String,
                      var elapsedNanos: Long,
                      var rssi: Int,
                      var isPaired: Boolean = false) {
        constructor(result: ScanResult) : this(result.device, result.scanRecord.deviceName,
                result.timestampNanos, result.rssi, false)
    }

    @get:Bindable
    var errorText: String? by DataBindingDelegates.observable(BR.errorText, null)

    @Bindable
    var scanning: Boolean = false

    @Bindable
    val results: ObservableList<Result> = ObservableArrayList()

    val scanner: BleScanner = BleScanner(applicationContext, this)
    override var mCallbacks: PropertyChangeRegistry? = null

    fun startScan() {
        scanner.startScan()
        results.sortBy { it.elapsedNanos }
        scanning = true
    }

    fun stopScan() {
        scanner.stopScan()
        scanning = false
    }

    override fun close() {
        stopScan()
    }


    override fun onFailurePermissionsNecessary() {
        errorText = "Error: Location permissions are required to perform Bluetooth scan."
    }

    override fun onFailureBluetoothDisabled() {
        errorText = "Error: Bluetooth must be enabled."
    }

    override fun onFailure(message: String?) {
        errorText = message
    }

    override fun onDeviceDiscovery() {
        throw UnsupportedOperationException()
    }

    override fun onScanResult(result: ScanResult) {
        processNewResult(result)
    }

    override fun onScanResultFirstMatch(result: ScanResult) {
        processNewResult(result)
    }

    override fun onScanResultLost(result: ScanResult) {
        val listedResult = results.find { it.device == result.device } ?: Result(result)
        if (results.contains(listedResult))
            results.remove(listedResult)
    }

    override fun onScanResultAllMatches(result: ScanResult) {
        processNewResult(result)
    }

    fun processNewResult(result: ScanResult) {
        val listedResult = results.find { it.device == result.device } ?: Result(result)
        if (!results.contains(listedResult))
            results.add(listedResult)
        results.sortBy { it.elapsedNanos }
    }
}

