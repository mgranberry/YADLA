package com.kludgenics.cgmlogger.app.service

import android.content.Context
import com.kludgenics.cgmlogger.model.nightscout.NightscoutApiEndpoint
import com.kludgenics.cgmlogger.model.treatment.Treatment
import io.realm.Realm
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

/**
 * Created by matthiasgranberry on 6/4/15.
 */

class NightscoutTreatmentTask(override val ctx: Context,
                          override val nightscoutEndpoint: NightscoutApiEndpoint): NightscoutTask, AnkoLogger {


    override fun postprocess(realm: Realm, l: List<*>) {
    }

    override val init: NightscoutApiEndpoint.() -> List<Map<String, String>>
        get() = fun (): List<Map<String, String>> {
            return getTreatmentsSince("2010")
        }

    override val copy: Realm.(Any) -> Unit
        get() = fun (it: Any) {
            info ("Incoming treatment: $it")
            when (it) {
                is Map<*, *> -> {
                    val treatment = it as Map<String, String>
                    info("Processing $treatment")
                    copyToRealmOrUpdate(Treatment(treatment))
                }
            }
        }

}