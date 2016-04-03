package com.kludgenics.cgmlogger.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.realm.Realm
import io.realm.RealmConfiguration

/**
 * Created by matthiasgranberry on 5/29/15.
 */
class OnBootReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val configuration = RealmConfiguration.Builder(context.applicationContext)
                .build()
        Realm.compactRealm(configuration)
    }
}
