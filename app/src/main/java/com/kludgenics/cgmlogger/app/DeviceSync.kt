package com.kludgenics.cgmlogger.app

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.SyncResult
import android.hardware.usb.UsbDevice
import android.os.PowerManager
import com.crashlytics.android.Crashlytics
import com.kludgenics.alrightypump.android.AndroidDeviceHelper
import com.kludgenics.alrightypump.android.DexcomShareBleConnection
import com.kludgenics.alrightypump.android.ShareGatt
import com.kludgenics.alrightypump.device.Device
import com.kludgenics.alrightypump.device.dexcom.g4.DexcomG4
import com.kludgenics.alrightypump.device.tandem.TandemPump
import com.kludgenics.alrightypump.therapy.*
import com.kludgenics.cgmlogger.app.events.SyncCompleteEvent
import com.kludgenics.cgmlogger.app.model.PersistedTherapyTimeline
import com.kludgenics.cgmlogger.app.viewmodel.RealmStatus
import com.kludgenics.cgmlogger.app.viewmodel.Status
import com.kludgenics.cgmlogger.extension.create
import com.kludgenics.cgmlogger.extension.where
import io.realm.Realm
import io.realm.Sort
import org.jetbrains.anko.*
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.LocalDateTime
import org.joda.time.Period
import java.io.EOFException
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Created by matthias on 3/18/16.
 */

class DeviceSync : AnkoLogger {
    private constructor ()
    companion object {
        private val _instance: DeviceSync by lazy { DeviceSync() }
        fun getInstance() = _instance
    }
    val syncExecutorService = Executors.newCachedThreadPool()
    val executingSyncs: MutableSet<Any> = HashSet()
    val devices: MutableMap<UsbDevice, AndroidDeviceHelper.DeviceEntry> = HashMap()
    val executingSyncLock = ReentrantLock()

    private fun downloadTandem(therapyTimeline: TherapyTimeline, device: TandemPump,
                               fetchPredicate: (Record) -> Boolean): Record? {
        info("Syncing basal records")
        therapyTimeline.merge(fetchPredicate, device.basalRecords)
        info("Syncing bolus records")
        therapyTimeline.merge(fetchPredicate, device.bolusRecords)
        info("Syncing smbg records")
        therapyTimeline.merge(fetchPredicate, device.smbgRecords)
        info("Syncing consumable records")
        therapyTimeline.merge(fetchPredicate, device.consumableRecords)
        //info("Syncing provile records")
        //therapyTimeline.merge(fetchPredicate, device.profileRecords)
        info("Synced all records")
        return null
    }

    private fun downloadDexcomG4(therapyTimeline: TherapyTimeline, device: DexcomG4,
                                 fetchPredicate: (Record) -> Boolean): Record? {
        val cgmRecords = device.cgmRecords
        therapyTimeline.merge(fetchPredicate,
                cgmRecords)
        if (cgmRecords.calibrationRequired)
            therapyTimeline.merge(fetchPredicate, device.calibrationRecords)
        val event = therapyTimeline.events.lastOrNull()
        info("Synced pages: ${device.syncedPages}")
        return event
    }

    private fun createStatus(realm: Realm, device: Device): Int {
        val syncId = (realm.allObjects(RealmStatus::class.java).max("syncId") ?: 0).toInt() + 1
        return realm.create<RealmStatus> {
            syncStartTime = Date()
            modificationTime = syncStartTime
            serialNumber = device.serialNumber
            statusCode = Status.CODE_DEVICE_READ_IN_PROGRESS
            statusText = Status.IN_PROGRESS
            icon = R.drawable.bluetooth_circle
            active = true
            this.syncId = syncId
        }.syncId
    }

    private fun getLatestSuccessFor(device: Device): Date? {
        val realm = Realm.getDefaultInstance()
        realm.use {
            val updates = realm.where<RealmStatus> {
                equalTo("serialNumber", device.serialNumber)
                equalTo("statusText", Status.SUCCESS)
            }.findAllSorted("modificationTime", Sort.DESCENDING)
            return updates.firstOrNull()?.syncStartTime
        }
    }

    private fun updateStatus(realm: Realm, syncId: Int, statusCode: Int, latestRecordTime: Date? = null, inProgress: Boolean = false, clockOffsetMillis: Long? = null) {
        val status = realm.where<RealmStatus> {
            equalTo("syncId", syncId)
        }.findAllSorted("modificationTime", Sort.DESCENDING).firstOrNull()
        if (status != null) {
            realm.beginTransaction()
            status.modificationTime = Date()
            status.statusCode = statusCode
            status.statusText = when (status.statusCode) {
                Status.CODE_DEVICE_READ_IN_PROGRESS -> Status.IN_PROGRESS
                Status.CODE_SUCCESS -> Status.SUCCESS
                Status.CODE_FAILURE -> Status.FAILURE
                Status.CODE_EOF -> Status.EOF
                else -> "Unknown status"
            }
            status.active = inProgress
            status.latestRecordTime = if (clockOffsetMillis != null && latestRecordTime != null)
                Date(latestRecordTime.time + clockOffsetMillis)
            else
                latestRecordTime
            status.clockOffsetMillis = clockOffsetMillis
            realm.commitTransaction()
        }
    }

    fun sync(context: Context, device: BluetoothDevice) {
        val shouldSync = executingSyncLock.withLock {
            if (executingSyncs.contains(device)) {
                info("$device already in deviceMap: $executingSyncs")
                false
            } else {
                info("Adding $device to deviceMap: $executingSyncs")
                executingSyncs += device
                true
            }
        }
        if (shouldSync) {
            val connection = DexcomShareBleConnection(context.getApplicationContext())
            val wakelock = context.powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DeviceSync.Bluetooth")
            wakelock.acquire()
            connection.connect(device, onDisconnected = {
                info("disconnected")
                EventBus.post(SyncCompleteEvent(""))
                if (wakelock.isHeld) {
                    info("releasing wakelock")
                    wakelock.release()
                }
                executingSyncLock.withLock {
                    info("Removing $device from deviceMap: $executingSyncs")
                    executingSyncs -= device
                }
            }, onConnected = {
                async() {
                    connection.use {
                        try {
                            val source = DexcomShareBleConnection.source(it)
                            val sink = DexcomShareBleConnection.sink(it)
                            info("Setting timeouts")
                            source.timeout().timeout(5, TimeUnit.SECONDS)
                            sink.timeout().timeout(5, TimeUnit.SECONDS)
                            val g4 = DexcomG4(source, sink)

                            g4.bleEnabled = true
                            val completionEvent = syncDevice(context, g4).get()
                            info("syncDevice result is:$completionEvent")
                            EventBus.post(completionEvent)
                        } finally {
                            if (wakelock.isHeld) {
                                info("releasing wakelock")
                                wakelock.release()
                            }
                            executingSyncLock.withLock {
                                info("Removing $device from deviceMap: $executingSyncs")
                                executingSyncs -= device
                            }
                        }
                    }
                }
            }, onError = { shareGatt: ShareGatt, message: String ->
                error(message)
                EventBus.post(SyncCompleteEvent(""))
                info ("wakelock ${wakelock.isHeld}")
                if (wakelock.isHeld) {
                    info("releasing wakelock")
                    wakelock.release()
                }
                shareGatt.close()
                executingSyncLock.withLock {
                    info("Removing $device from deviceMap: $executingSyncs")
                    executingSyncs -= device
                }
            })
        } else {
            EventBus.post(SyncCompleteEvent(""))
        }
    }

    fun sync(context: Context, device: UsbDevice) {
        val shouldSync = executingSyncLock.withLock {
            if (executingSyncs.contains(device)) {
                info("$device already in deviceMap: $executingSyncs")
                false
            } else {
                info("Adding $device to deviceMap: $executingSyncs")
                executingSyncs += device
                true
            }
        }
        if (shouldSync) {
            try {
                info("Starting sync")
                val deviceEntry = AndroidDeviceHelper.getDeviceEntry(context, device)
                info ("AndroidDeviceHelper.getDeviceEntry($context, $device) returned $deviceEntry")
                deviceEntry?.serialConnection?.use {
                    devices[device] = deviceEntry
                    info("calling syncDevice")
                    val completionEvent = syncDevice(context, deviceEntry.device).get()
                    info("syncDevice result is:$completionEvent")
                    EventBus.post(completionEvent)
                }


            } finally {
                executingSyncLock.withLock {
                    info("Removing $device from deviceMap: $executingSyncs")
                    devices.remove(device)
                    executingSyncs -= device
                }
            }
        }
    }

    fun stopDeviceSync(device: UsbDevice) {
        val r = devices[device]
        r?.serialConnection?.close()
    }

    fun syncDevice(context: Context, device: Device): Future<SyncCompleteEvent> {
        info("Syncing ${device.serialNumber}")
        Crashlytics.log("Syncing ${device.serialNumber}")
        return context.asyncResult (syncExecutorService) {
            try {
                val realm = Realm.getDefaultInstance()
                var nextSync: Date? = null
                realm.use {
                    val therapyTimeline = PersistedTherapyTimeline()
                    therapyTimeline.use {
                        val syncId = createStatus(realm, device)
                        try {
                            val offset = device.timeCorrectionOffset ?: Duration.ZERO
                            val updateTime = LocalDateTime(getLatestSuccessFor(device) ?:
                                    (System.currentTimeMillis() - Period.days(1).toStandardSeconds().seconds * 1000L)) - offset
                            // Disable large raw fetches over ble
                            if (Period(updateTime, LocalDateTime.now()).toStandardHours().hours >= 1 && device is DexcomG4 && device.bleEnabled)
                                device.rawEnabled = false
                            info("Syncing back to ${device.serialNumber} $updateTime $syncId")
                            val fetchPredicate = { record: Record -> record.time > updateTime }
                            val latestEvent = when (device) {
                                is DexcomG4 -> downloadDexcomG4(therapyTimeline, device,
                                        fetchPredicate)
                                is TandemPump -> downloadTandem(therapyTimeline, device,
                                        fetchPredicate)
                                else -> null
                            }
                            info("sync of ${device.serialNumber} ${latestEvent?.time} $syncId done")
                            val latestTime = latestEvent?.time
                            updateStatus(realm, syncId, Status.CODE_SUCCESS, latestTime?.toDate(), inProgress = false, clockOffsetMillis = offset.millis)
                            val lastBgs = therapyTimeline.glucoseEvents.toList().reversed().take(2)
                            if (lastBgs.size == 2)
                                EventBus.post(lastBgs.component2() to lastBgs.component1())
                            if (latestTime != null)
                                nextSync = (latestTime + device.timeCorrectionOffset + Period.minutes(5)).toDate()
                        } catch (e: ArrayIndexOutOfBoundsException) {
                            info("sync of ${device.serialNumber} failed.", e)
                            e.printStackTrace()
                            updateStatus(realm, syncId, Status.CODE_FAILURE, null, false)
                            Crashlytics.logException(e)
                        } catch (e: EOFException) {
                            updateStatus(realm, syncId, Status.CODE_EOF, null, false)
                        } catch (e: java.io.InterruptedIOException) {
                            updateStatus(realm, syncId, Status.CODE_EOF, null, false)
                        }
                    }
                }
                SyncCompleteEvent(device.serialNumber, nextSync = nextSync)
            } catch (e: Exception) {
                info("Exception caught: ${e.message}")
                e.printStackTrace()
                Crashlytics.logException(e)
                throw e
            }
        }
    }
}