package com.kludgenics.cgmlogger.app

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import com.kludgenics.alrightypump.therapy.GlucoseRecord
import com.kludgenics.cgmlogger.app.adapter.CardAdapter
import com.kludgenics.cgmlogger.app.databinding.ActivityMainBinding
import com.kludgenics.cgmlogger.app.databinding.DialogConfigureNightscoutBinding
import com.kludgenics.cgmlogger.app.model.PersistedRawCgmRecord
import com.kludgenics.cgmlogger.app.model.SyncStore
import com.kludgenics.cgmlogger.app.service.SyncService
import com.kludgenics.cgmlogger.app.viewmodel.NightscoutConfig
import com.kludgenics.cgmlogger.app.viewmodel.ObservableStatus
import com.kludgenics.cgmlogger.app.viewmodel.RealmStatus
import com.kludgenics.cgmlogger.extension.where
import com.squareup.otto.Subscribe
import io.realm.Realm
import io.realm.Sort
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.alert
import org.jetbrains.anko.info
import org.jetbrains.anko.onUiThread
import org.joda.time.DateTime
import org.joda.time.Duration
import java.util.*

class MainActivity :  BaseActivity(), AnkoLogger {

    val realm = Realm.getDefaultInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(binding.includedListViewpager.toolbar)

        val start = DateTime()
        // TODO this is a query on the UI thread.  It would be nice if it could be done async, but the async versions of queries don't play well with
        binding.includedListViewpager.recycler.adapter = CardAdapter(
                realm.where<RealmStatus>()
                        .findAllSorted("modificationTime", Sort.DESCENDING)
                        .distinct("serialNumber"))
        val end = DateTime()
        info("Sync status query took ${Duration(start, end)} to complete.")
        binding.includedListViewpager.recycler.layoutManager = LinearLayoutManager(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    override fun onStart() {
        super.onStart()
        startService(Intent(this, SyncService::class.java))
    }

    override fun onResume() {
        super.onResume()
        EventBus.register(this)
    }

    override fun onPause() {
        super.onPause()
        EventBus.unregister(this)
    }

    @Subscribe
    fun onBgAvailable(glucose: PersistedRawCgmRecord) {
        val mgDl = glucose.value.mgdl
        val time = glucose.time
        onUiThread {
            supportActionBar?.title = mgDl.toString()
            supportActionBar?.subtitle = time.toString()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu);
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            alert {
                title("Nightscout URL")
                message("Set this to your Nightscout's address.")
                val binding = DialogConfigureNightscoutBinding.inflate(layoutInflater)
                binding.config = NightscoutConfig(SyncStore())
                customView ( binding.root )
                negativeButton("CANCEL") {
                    this.cancel()
                }
                positiveButton("SAVE", { binding.config.onSave() })
            }.show()
        }

        return super.onOptionsItemSelected(item)
    }

}
