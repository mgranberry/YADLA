package com.kludgenics.cgmlogger.app.service

import android.content.Context
import com.kludgenics.cgmlogger.model.nightscout.NightscoutApiEndpoint
import com.kludgenics.cgmlogger.model.nightscout.NightscoutApiEntry
import com.kludgenics.cgmlogger.model.nightscout.NightscoutApiTreatment
import io.realm.Realm

/**
 * Created by matthiasgranberry on 6/4/15.
 */

class NightscoutEntryTask(ctx: Context,
                          override val nightscoutEndpoint: NightscoutApiEndpoint,
                          val count: Int = 10): NightscoutTask {

    override val realm: Realm = Realm.getInstance(ctx)

    override val init: NightscoutApiEndpoint.() -> List<NightscoutApiEntry>
        get() = fun (): List<NightscoutApiEntry> {
            return getEntries(count)
        }

    override val copy: Realm.(Any) -> Unit
        get() = fun (it: Any) {
            if (it is NightscoutApiEntry)
                copyToRealmOrUpdate(it.asRealmObject())
        }
}