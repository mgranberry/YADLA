package com.kludgenics.cgmlogger.app.service

import android.content.Context
import com.kludgenics.cgmlogger.model.nightscout.NightscoutApiEndpoint
import com.kludgenics.cgmlogger.model.nightscout.NightscoutApiEntry
import com.kludgenics.cgmlogger.model.nightscout.NightscoutApiTreatment
import io.realm.Realm
import java.io.Closeable

/**
 * Created by matthiasgranberry on 6/4/15.
 */

class NightscoutTreatmentTask(ctx: Context,
                          override val nightscoutEndpoint: NightscoutApiEndpoint): NightscoutTask {

    override val realm: Realm = Realm.getInstance(ctx)

    override val init: NightscoutApiEndpoint.() -> List<NightscoutApiTreatment>
        get() = fun (): List<NightscoutApiTreatment> {
            return getTreatments()
        }

    override val copy: Realm.(Any) -> Unit
        get() = fun (it: Any) {
            if (it is NightscoutApiTreatment)
                copyToRealmOrUpdate(it.toTreatment())
        }

}