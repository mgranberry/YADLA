package com.kludgenics.cgmlogger.app.service

import android.app.AlarmManager
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
import com.kludgenics.cgmlogger.app.DeviceSync
import com.kludgenics.cgmlogger.app.EventBus
import com.kludgenics.cgmlogger.app.NightscoutSync
import com.kludgenics.cgmlogger.app.events.SyncComplete
import com.kludgenics.cgmlogger.app.model.PersistedTherapyTimeline
import com.kludgenics.cgmlogger.app.model.SyncStore
import com.kludgenics.cgmlogger.extension.where
import com.squareup.otto.Subscribe
import io.realm.Realm
import org.jetbrains.anko.*

/**
 * Created by matthias on 3/22/16.
 */

class SyncService : Service(), AnkoLogger {
    companion object {
        val ACTION_USB_PERMISSION = "com.kludgenics.cgmlogger.ACTION_USB_PERMISSION"
    }

    val receiver: BroadcastReceiver = IntentReceiver()
    var isRegistered = false
    var activeCount = 0

    inner class IntentReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                    info ("device detached: $device")
                    if (device != null)
                        DeviceSync.getInstance().stopDeviceSync(device)
                }
                ACTION_USB_PERMISSION -> {
                    val permissionGranted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                    info("received permission for device.  Granted? ${permissionGranted}")
                    if (permissionGranted) {
                        val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE);
                        performDeviceSync(device)
                    } else {
                        unregisterReceiver()
                        //e()
                    }
                }
            }
        }
    }



    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    fun performDeviceSync(device: UsbDevice) {
        async() {
            val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, loggerTag)
            try {
                wakeLock.acquire()
                DeviceSync.getInstance().sync(this@SyncService, device)
                info("sync finished")
                val timeline = PersistedTherapyTimeline()
                timeline.use {
                    val realm = Realm.getDefaultInstance()
                    realm.use {
                        val nightscoutInstances = realm.where<SyncStore> {
                            equalTo("storeType", SyncStore.STORE_TYPE_NIGHTSCOUT)
                        }.findAll()
                        nightscoutInstances.toList().forEach {
                            NightscoutSync.getInstance().uploadToNightscout(timeline, it)
                        }
                    }
                }
            } catch (e: Exception) {
                error("sync failed:", e)
            } finally {
                info("Releasing wakelock, stopping.")
                wakeLock.release()
                unregisterReceiver()
                //stopSelf()
            }
        }
    }

    @Suppress("unused")
    @Subscribe
    fun syncCompleted(syncEvent: SyncComplete) {
        info("Nightscout sync initiated")
        async() {
            val timeline = PersistedTherapyTimeline()
            timeline.use {
                val realm = Realm.getDefaultInstance()
                realm.use {
                    val nightscoutInstances = realm.where<SyncStore> {
                        equalTo("storeType", SyncStore.STORE_TYPE_NIGHTSCOUT)
                    }.findAll()
                    nightscoutInstances.toList().forEach {
                        NightscoutSync.getInstance().uploadToNightscout(timeline, it)
                    }
                }
            }
        }

        val nextSync = if (syncEvent.nextSync?.time ?: 0 > System.currentTimeMillis())
            syncEvent.nextSync!!.time
        else
            System.currentTimeMillis() + 60000*5
        val pendingIntent = PendingIntent.getService(applicationContext, 0, Intent(applicationContext, this.javaClass), PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextSync, pendingIntent)
        unregisterReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        info("onStartCommand() activeCount=${++activeCount}")

        val pendingIntent = PendingIntent.getService(applicationContext, 0, Intent(applicationContext, this.javaClass), PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60000*5, pendingIntent)

        registerReceiver()
        async() {
            val permissionIntent = PendingIntent.getBroadcast(this@SyncService, 0, Intent(ACTION_USB_PERMISSION), 0);
            val tandemPumps = AndroidDeviceHelper.getTandemPumps(this@SyncService)
            val dexcomG4s = AndroidDeviceHelper.getDexcomG4s(this@SyncService)
            dexcomG4s.forEach { device ->
                info("requesting permisison for ${device}")
                usbManager.requestPermission(device, permissionIntent)
            }
            tandemPumps.forEach { device ->
                info("requesting permisison for ${device}")
                usbManager.requestPermission(device, permissionIntent)
            }
            val bluetoothAdapter = bluetoothManager.adapter
            if (bluetoothAdapter != null) {
                val device = bluetoothAdapter.getRemoteDevice("E1:A0:CF:AB:96:FE")
                DeviceSync.getInstance().sync(this@SyncService, device)
            }
            if (tandemPumps.isEmpty() && dexcomG4s.isEmpty() && false) {
                unregisterReceiver()
                stopSelf()
            }
        }
        return Service.START_NOT_STICKY
    }

    private fun registerReceiver() {
        onUiThread {
            if (!isRegistered) {
                isRegistered = true
                info("Registering Receiver")
                EventBus.instance.register(this)
                val filter = IntentFilter(ACTION_USB_PERMISSION);
                filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
                registerReceiver(receiver, filter);
            }
        }
    }

    private fun unregisterReceiver() {
        onUiThread {
            if (--activeCount == 0)
                stopSelf()
            if (isRegistered) {
                isRegistered = false
                info("Unregistering Receiver")
                EventBus.instance.unregister(this)
                unregisterReceiver(receiver)
                stopSelf()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        info("onDestroy()")
        unregisterReceiver()
    }
}