package com.kludgenics.cgmlogger.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.kludgenics.cgmlogger.app.service.SyncService
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

/**
 * Created by matthias on 12/13/15.
 */
class DownloadActivity : AnkoLogger, Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onStart() {
        super.onStart()
        info("Syncing")
        startService(Intent(this, SyncService::class.java))
        info("Finishing activity")
        finish()
    }
}