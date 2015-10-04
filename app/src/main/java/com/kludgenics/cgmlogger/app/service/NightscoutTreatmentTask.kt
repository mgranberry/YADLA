package com.kludgenics.cgmlogger.app.service

import android.content.Context
import com.kludgenics.cgmlogger.model.nightscout.NightscoutApiEndpoint
import com.kludgenics.cgmlogger.model.nightscout.NightscoutApiTreatment
import io.realm.Realm

/**
 * Created by matthiasgranberry on 6/4/15.
 */

class NightscoutTreatmentTask(override val ctx: Context,
                          override val nightscoutEndpoint: NightscoutApiEndpoint): NightscoutTask {
    override fun postprocess(realm: Realm, l: List<*>) {
    }

    override val init: NightscoutApiEndpoint.() -> List<Map<String, String>>
        get() = fun (): List<Map<String, String>> {
            return treatments
        }

    override val copy: Realm.(Any) -> Unit
        get() = fun (it: Any) {
            if (it is NightscoutApiTreatment)
                copyToRealmOrUpdate(it.toTreatment())
        }

}