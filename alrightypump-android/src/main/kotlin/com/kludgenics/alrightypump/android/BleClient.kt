package com.kludgenics.alrightypump.android

import android.content.Context
import com.polidea.rxandroidble.RxBleClient

/**
 * Created by matthias on 4/16/16.
 */
object BleClient {
    private var isInitialized: Boolean = false
    private lateinit var instance: RxBleClient
    fun init(context: Context) {
        if (!isInitialized)
            instance = RxBleClient.create(context.applicationContext)
    }

    fun getInstance() : RxBleClient {
        return instance
    }
}