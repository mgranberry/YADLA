package com.kludgenics.cgmlogger.app.service

import android.content.Context
import com.google.android.gms.gcm.GcmNetworkManager
import com.google.gson.JsonParseException
import com.kludgenics.cgmlogger.model.nightscout.NightscoutApiEndpoint
import com.kludgenics.cgmlogger.model.nightscout.NightscoutApiTreatment
import io.realm.Realm
import org.jetbrains.anko.*
import org.joda.time.DateTime
import retrofit.RetrofitError
import java.io.Closeable
import java.util.concurrent.Callable

/**
 * Created by matthiasgranberry on 6/4/15.
 */

interface NightscoutTask: Callable<Int>, AnkoLogger {
    val nightscoutEndpoint: NightscoutApiEndpoint
    val init: NightscoutApiEndpoint.() -> List<Any>?
    val ctx: Context
    val copy: Realm.(e: Any) -> Unit
    fun postprocess(realm: Realm, l: List<*>): Unit

    override fun call(): Int {
        val realm = Realm.getInstance(ctx)
        realm use {
            try {
                val items = nightscoutEndpoint.init()
                realm.beginTransaction()
                if (items != null && items.isNotEmpty()) {
                    items forEach {
                        realm copy it
                    }
                    info("sync completed")
                    realm.commitTransaction()
                    realm.beginTransaction()
                    info("ps")
                    postprocess(realm, items)
                    realm.commitTransaction()
                    info("pe")
                    return GcmNetworkManager.RESULT_SUCCESS
                } else {
                    info("sync failed")
                    realm.cancelTransaction()
                    return GcmNetworkManager.RESULT_RESCHEDULE
                }
            } catch (e: RetrofitError) {
                error("Retrofit Error: ${e}")
                e.printStackTrace()
                return when (e.getKind()) {
                    RetrofitError.Kind.CONVERSION -> GcmNetworkManager.RESULT_FAILURE
                    RetrofitError.Kind.HTTP -> GcmNetworkManager.RESULT_FAILURE
                    RetrofitError.Kind.NETWORK -> GcmNetworkManager.RESULT_RESCHEDULE
                    RetrofitError.Kind.UNEXPECTED -> throw(e)
                    else -> GcmNetworkManager.RESULT_FAILURE
                }
            } catch (t: JsonParseException) {
                error("sync failed: ${t}")
                return GcmNetworkManager.RESULT_FAILURE
            }
        }
    }
}