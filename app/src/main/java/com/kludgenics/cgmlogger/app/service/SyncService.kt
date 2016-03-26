package com.kludgenics.cgmlogger.app.service

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.IBinder
import android.os.PowerManager
import com.kludgenics.alrightypump.android.AndroidDeviceHelper
import com.kludgenics.alrightypump.cloud.nightscout.Nightscout
import com.kludgenics.alrightypump.therapy.TherapyTimeline
import com.kludgenics.cgmlogger.app.DeviceSync
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.jetbrains.anko.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Created by matthias on 3/22/16.
 */

class SyncService : Service(), AnkoLogger {
    inner class IntentReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    val devices = intent.getParcelableArrayListExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                    devices.forEach { DeviceSync.stopDeviceSync(it) }
                }
                ACTION_USB_PERMISSION -> {
                    val permissionGranted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                    info("received permission for device.  Granted? ${permissionGranted}")
                    if (permissionGranted) {
                        val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE);
                        performDeviceSync(device)
                    } else {
                        unregisterReceiver(this)
                        stopSelf()
                    }
                }
            }
        }
    }

    companion object {
        val ACTION_USB_PERMISSION = "com.kludgenics.cgmlogger.ACTION_USB_PERMISSION"
        @JvmStatic
        val TAG = SyncService::class.java.simpleName
    }

     val receiver: BroadcastReceiver = IntentReceiver()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    fun performDeviceSync(device: UsbDevice) {
        async() {
            val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG)
            try {
                wakeLock.acquire(60000)
                val timeline = DeviceSync.sync(this@SyncService, device)
                uploadToNightscout(timeline)
            } finally {
                info("Releasing wakelock, stopping.")
                wakeLock.release()
                unregisterReceiver(receiver)
                stopSelf()
            }
        }
    }

    fun uploadToNightscout(timeline: TherapyTimeline?) {
        info("Sync complete, received ${timeline?.events?.count()}")
        val nightscout_url = "https://12345678901234@omnominable.granberrys.us/"
        try {
            val nightscout = Nightscout(HttpUrl.parse(nightscout_url), OkHttpClient())
            if (timeline != null)
                nightscout.postRecords(timeline.events, object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {

                    }

                    override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                        println("Throwable2: $t ${t?.message} ${t?.cause}")
                        t?.printStackTrace()
                        println("cause")
                        t?.cause?.printStackTrace()
                    }
                })
            info("Upload completed")
        } catch (e: Exception) {
            println("Exception $e")
            e.printStackTrace()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        info("onStartCommand()")
        val permissionIntent = PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), 0);
        registerReceiver()
        val tandemPumps = AndroidDeviceHelper.getTandemPumps(this)
        val dexcomG4s = AndroidDeviceHelper.getDexcomG4s(this)
        dexcomG4s.forEach { device ->
            info("requesting permisison for ${device}")
            usbManager.requestPermission(device, permissionIntent)
        }
        tandemPumps.forEach { device ->
            info("requesting permisison for ${device}")
            usbManager.requestPermission(device, permissionIntent)
        }
        if (tandemPumps.isEmpty() && dexcomG4s.isEmpty()) {
            unregisterReceiver()
            stopSelf()
        }
        return Service.START_NOT_STICKY
    }

    private fun registerReceiver() {
        info("Registering Receiver")
        val filter = IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(receiver, filter);
    }

    private fun unregisterReceiver() {
        info("Unregistering Receiver")
        unregisterReceiver(receiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        info("onDestroy()")
    }
}