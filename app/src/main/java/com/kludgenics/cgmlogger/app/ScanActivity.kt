package com.kludgenics.cgmlogger.app

import android.databinding.DataBindingUtil
import android.databinding.ObservableList
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.kludgenics.alrightypump.android.DexcomShareBleConnection
import com.kludgenics.alrightypump.android.ShareGatt
import com.kludgenics.alrightypump.device.dexcom.g4.DexcomG4
import com.kludgenics.cgmlogger.app.databinding.ActivityScanBinding
import com.kludgenics.cgmlogger.app.viewmodel.ScannerModel
import io.realm.Realm
import okio.Okio
import okio.Sink
import okio.Source
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.async
import org.jetbrains.anko.info
import org.jetbrains.anko.error
import org.joda.time.DateTime
import org.joda.time.Duration

class ScanActivity :  AppCompatActivity(), AnkoLogger {

    val realm = Realm.getDefaultInstance()
    lateinit var scanner: ScannerModel
    var connection: DexcomShareBleConnection? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        info("onCreate")
        val binding: ActivityScanBinding = DataBindingUtil.setContentView(this, R.layout.activity_scan)
        //setSupportActionBar(binding.includedListViewpager.toolbar)
        //actionBar.setHomeButtonEnabled(true)
        scanner = ScannerModel(this)
        binding.includedListViewpager.scanResults = scanner
    }

    override fun onDestroy() {
        super.onDestroy()
        info("onDestroy")
        realm.close()
    }

    override fun onStart() {
        super.onStart()
        info("onStart")

        scanner.results.addOnListChangedCallback(object : ObservableList.OnListChangedCallback<ObservableList<ScannerModel.Result>>() {
            override fun onItemRangeRemoved(p0: ObservableList<ScannerModel.Result>, p1: Int, p2: Int) {
                //info("onItemRangeRemoved ${p0[p1]} + $p2")
            }

            override fun onChanged(p0: ObservableList<ScannerModel.Result>) {
                info("onChanged($p0)")
            }

            override fun onItemRangeChanged(p0: ObservableList<ScannerModel.Result>, p1: Int, p2: Int) {
                info("onItemRangeChanged ${p0[p1]} + $p2")
            }

            override fun onItemRangeMoved(p0: ObservableList<ScannerModel.Result>, p1: Int, p2: Int, p3: Int) {
                info("onItemRangeMoved ${p0[p1]} + $p2 to $p3")
            }

            @Synchronized
            override fun onItemRangeInserted(p0: ObservableList<ScannerModel.Result>, p1: Int, p2: Int) {
                info("onItemRangeInserted ${p0[p1]} + $p2")
            }
        })
    }

    override fun onResume() {
        super.onResume()
        info("onResume")
        scanner.results.clear()
        scanner.startScan()
    }

    @Synchronized
    override fun onPause() {
        super.onPause()
        info("onPause")
        scanner.stopScan()
    }
}
