package com.kludgenics.cgmlogger.app

import com.kludgenics.alrightypump.cloud.nightscout.Nightscout
import com.kludgenics.alrightypump.therapy.TherapyTimeline
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NightscoutSync: AnkoLogger {
    private constructor()
    companion object {
        private val _instance: NightscoutSync by lazy {NightscoutSync()}
        @JvmStatic
        fun getInstance(): NightscoutSync = NightscoutSync._instance
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

}