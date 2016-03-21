package com.kludgenics.cgmlogger.app

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import com.kludgenics.alrightypump.cloud.nightscout.Nightscout
import com.kludgenics.alrightypump.cloud.nightscout.NightscoutEntryJson
import com.kludgenics.cgmlogger.app.databinding.ActivityMainBinding
import com.squareup.moshi.JsonDataException
import io.realm.Realm
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okio.Buffer
import org.jetbrains.anko.AnkoLogger
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Created by matthias on 12/13/15.
 */
class DownloadActivity : AnkoLogger, AppCompatActivity() {
    val realm = Realm.getDefaultInstance()
    lateinit var binding: ActivityMainBinding
    lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        toolbar = binding.includedListViewpager.toolbar
        setSupportActionBar(toolbar)
        System.out.println(toolbar.title)

        //navBinding = NavHeaderBinding.bind(binding.navView)
        //System.out.println("NavBinding=$navBinding")
        //navBinding.status = status
        //setContentView(R.layout.activity_main)
    }

    override fun onDestroy() {
        realm.close()
    }

    override fun onStart() {
        super.onStart()
        println("Syncing")
        DeviceSync.sync(this, {
            timeline ->
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
        })
        println("Synced")
    }
}