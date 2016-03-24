package com.kludgenics.cgmlogger.app

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import com.kludgenics.alrightypump.cloud.nightscout.Nightscout
import com.kludgenics.alrightypump.cloud.nightscout.NightscoutEntryJson
import com.kludgenics.cgmlogger.app.adapter.CardAdapter
import com.kludgenics.cgmlogger.app.databinding.ActivityMainBinding
import com.kludgenics.cgmlogger.app.databinding.CardDeviceStatusBinding
import com.kludgenics.cgmlogger.app.service.SyncService
import com.kludgenics.cgmlogger.app.viewmodel.ObservableStatus
import com.kludgenics.cgmlogger.app.viewmodel.RealmStatus
import com.kludgenics.cgmlogger.extension.where
import com.squareup.moshi.JsonDataException
import io.realm.Realm
import io.realm.Sort
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okio.Buffer
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.async
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

/**
 * Created by matthias on 12/13/15.
 */
class DownloadActivity : AnkoLogger, Activity() {
    val realm = Realm.getDefaultInstance()
    lateinit var binding: CardDeviceStatusBinding
    lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    override fun onStart() {
        super.onStart()
        println("Syncing")
        startService(Intent(this, SyncService::class.java))
        println("Finishing activity")
        finish()
    }
}