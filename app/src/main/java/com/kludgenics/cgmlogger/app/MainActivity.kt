package com.kludgenics.cgmlogger.app

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import com.kludgenics.cgmlogger.app.adapter.CardAdapter
import com.kludgenics.cgmlogger.app.databinding.ActivityMainBinding
import com.kludgenics.cgmlogger.app.service.SyncService
import com.kludgenics.cgmlogger.app.viewmodel.ObservableStatus
import com.kludgenics.cgmlogger.app.viewmodel.RealmStatus
import com.kludgenics.cgmlogger.extension.where
import io.realm.Realm
import io.realm.Sort
import org.jetbrains.anko.AnkoLogger
import java.util.*

class MainActivity :  AppCompatActivity(), AnkoLogger {

    val realm = Realm.getDefaultInstance()
    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    companion object {
        const val TWITTER_KEY = "XpH1SOqMSaH3v8P7A9e0RFBHm";
        const val TWITTER_SECRET = "BYYqmgAxxSyzfxbkYomajXZNvthMmvMLrdhhOChwHiUqGtln94";
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.status = ObservableStatus(RealmStatus(active = true, modificationTime = Date(), statusText = "Hello World", serialNumber = "SM12345678"))
        // TODO this is a query on the UI thread.  It would be nice if it could be done async, but the async versions of queries don't play well with
        binding.includedListViewpager.recycler.adapter = CardAdapter(
                realm.where<RealmStatus>()
                        .findAllSorted("modificationTime", Sort.DESCENDING)
                        .distinct("serialNumber"))

        realm.where<RealmStatus>()
                .findAllSorted("modificationTime", Sort.DESCENDING).forEach { println("${it.modificationTime} ${it.latestRecordTime} ${it.serialNumber} ${it.statusText}") }

        binding.includedListViewpager.recycler.layoutManager = LinearLayoutManager(this)
        //setContentView(R.layout.activity_main)
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    override fun onStart() {
        super.onStart()
        startService(Intent(this, SyncService::class.java))
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item!!.itemId

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

}
