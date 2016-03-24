package com.kludgenics.cgmlogger.app.service

import android.app.IntentService
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
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.async
import org.jetbrains.anko.powerManager
import org.jetbrains.anko.usbManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Created by matthias on 3/22/16.
 */

class SyncService : Service() {
    inner class DisconnectReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == UsbManager.ACTION_USB_DEVICE_DETACHED) {
                val devices = intent.getParcelableArrayListExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                devices.forEach { DeviceSync.stopDeviceSync(it) }
            }
        }

    }

    companion object {
        val ACTION_USB_PERMISSION = "com.kludgenics.cgmlogger.ACTION_USB_PERMISSION"
        @JvmStatic
        val TAG = SyncService::class.java.simpleName

    }

     val receiver: BroadcastReceiver = object: BroadcastReceiver() {
         override fun onReceive(context: Context, intent: Intent) {
             val action = intent.action
             if (ACTION_USB_PERMISSION == action) {
                 val permissionGranted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                 println("received permission for device.  Granted? ${permissionGranted}")
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
                timeline.events.forEach { println("${it.time} ${it.id} $it") }
            } finally {
                wakeLock.release()
                unregisterReceiver(receiver)
                stopSelf()
            }
        }
    }

    fun uploadToNightscout(timeline: TherapyTimeline?) {
        println("Sync complete, received ${timeline?.events?.count()}")
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
        } catch (e: Exception) {
            println("Exception $e")
            e.printStackTrace()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val permissionIntent = PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), 0);
        val filter = IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(receiver, filter);
        AndroidDeviceHelper.getDexcomG4s(this).forEach { device ->
            println("requesting permisison for ${device}")
            usbManager.requestPermission(device, permissionIntent)
        }
        AndroidDeviceHelper.getTandemPumps(this).forEach { device ->
            println("requesting permisison for ${device}")
            usbManager.requestPermission(device, permissionIntent)
        }
        return Service.START_NOT_STICKY
    }

}