package com.kludgenics.cgmlogger.app

import com.kludgenics.alrightypump.cloud.nightscout.Nightscout
import com.kludgenics.cgmlogger.app.model.InflatedRecord
import com.kludgenics.cgmlogger.app.model.PersistedTherapyTimeline
import com.kludgenics.cgmlogger.app.model.SyncStore
import com.kludgenics.cgmlogger.extension.transaction
import com.squareup.moshi.JsonDataException
import io.realm.Realm
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.io.IOException

class NightscoutSync: AnkoLogger {
    private constructor()

    var okHttpClient = OkHttpClient()

    companion object {
        private val _instance: NightscoutSync by lazy { NightscoutSync() }
        @JvmStatic
        fun getInstance(): NightscoutSync = NightscoutSync._instance
    }

    fun testUrl(url: String): String? {
        try {
            val parsedUrl = HttpUrl.parse(url)
            if (parsedUrl == null)
                return "Please enter a valid URL."
            val nightscout = Nightscout(parsedUrl, okHttpClient)
            val result = nightscout.status()
            if (!result.isSuccessful)
                return "Failed to connect with result ${result.code()} ${result.message()}.  Check address.  Do not include \"/api/v1/\""
            val status = result.body()
            if (status == null)
                return "Unable to read status."
            if (status.apiEnabled == false)
                return "API disabled on server.  Unable to upload."
            val auth = nightscout.verifyAuth().body()
            if (auth == null || auth.message != "OK")
                return "Auth verification failed.  Check password."
            return null
        } catch (e: IOException) {
            return "Error reading data: ${e.message}.  Check to make sure that address is correct."
        } catch (e: JsonDataException) {
            return "Error reading data: ${e.message}.  Check to make sure the address is correct."
        }
    }

    fun uploadToNightscout(timeline: PersistedTherapyTimeline, syncStore: SyncStore) {
        val newEvents = timeline.eventsWithoutSource(syncStore)
        info("!!!Sync complete, received ${newEvents.count()}")
        val nightscout_url = syncStore.parameters
        info("uploading to $nightscout_url")
        try {
            info("Trying")
            val nightscout = Nightscout(HttpUrl.parse(nightscout_url), okHttpClient)
            val uploadCalls = nightscout.prepareUploads(timeline.eventsWithoutSource(syncStore)).toList()
            info("cc: ${uploadCalls.count()}")
            uploadCalls.forEach {
                chunk: Nightscout.ChunkedCall<InflatedRecord> ->
                info("call")
                val response = chunk.call.execute();
                info("executed")
                val realm = Realm.getDefaultInstance()
                realm.use {
                    realm.transaction {
                        info("$response")
                        if (response.isSuccessful)
                            chunk.argumentList.forEach {
                                it.record.syncedStores.add(syncStore)
                            }
                    }
                }
            }
            info("Upload queued")
        } catch (e: Exception) {
            info("Exception in nightscoutSync", e)
            e.printStackTrace()
        }
    }

}