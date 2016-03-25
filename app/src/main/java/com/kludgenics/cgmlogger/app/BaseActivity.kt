package com.kludgenics.cgmlogger.app

import android.support.v7.app.AppCompatActivity
import com.kludgenics.cgmlogger.app.events.postOnPause
import com.kludgenics.cgmlogger.app.events.postOnResume

/**
 * Created by matthias on 3/24/16.
 */

open class BaseActivity : AppCompatActivity() {
    override fun onPause() {
        super.onPause()
        postOnPause()
    }

    override fun onResume() {
        super.onResume()
        postOnResume()
    }
}