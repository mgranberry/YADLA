package com.kludgenics.cgmlogger.app.service

import android.content.Context
import com.kludgenics.cgmlogger.model.glucose.BgPostprocesser
import com.kludgenics.cgmlogger.model.nightscout.NightscoutApiEndpoint
import com.kludgenics.cgmlogger.model.nightscout.NightscoutApiEntry
import com.kludgenics.cgmlogger.model.nightscout.NightscoutApiTreatment
import io.realm.Realm
import org.joda.time.DateTime
import kotlin.properties.Delegates

/**
 * Created by matthiasgranberry on 6/4/15.
 */

class NightscoutEntryTask(override val ctx: Context,
                          override val nightscoutEndpoint: NightscoutApiEndpoint,
                          val count: Int = 10): NightscoutTask {

    override val init: NightscoutApiEndpoint.() -> List<NightscoutApiEntry>
        get() = fun (): List<NightscoutApiEntry> {
            return getEntries(count)
        }

    override val copy: Realm.(Any) -> Unit
        get() = fun (it: Any) {
            if (it is NightscoutApiEntry)
                copyToRealmOrUpdate(it.asRealmObject())
        }


    override val postprocess: Realm.(Any) -> Unit
        get() = fun (l: Any) {
            if (l is List<*>) {
                val fi = l.first() as NightscoutApiEntry
                val li = l.last() as NightscoutApiEntry
                BgPostprocesser.groupByDay(this, DateTime(li.getDate()), DateTime(fi.getDate()))

            }
        }

}