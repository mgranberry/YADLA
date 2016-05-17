package com.kludgenics.cgmlogger.app.viewmodel

import android.databinding.Bindable
import android.databinding.Observable
import android.databinding.PropertyChangeRegistry
import android.view.View
import com.kludgenics.cgmlogger.app.BR
import com.kludgenics.cgmlogger.app.NightscoutSync
import com.kludgenics.cgmlogger.app.model.SyncStore
import com.kludgenics.cgmlogger.extension.transaction
import com.kludgenics.cgmlogger.extension.where
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmObject
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.async
import org.jetbrains.anko.info
import org.jetbrains.anko.error
import java.io.Closeable
import java.util.concurrent.Executors
import java.util.concurrent.Future

/**
 * Created by matthias on 3/29/16.
 */
class NightscoutConfig(val store: SyncStore, val realm: Realm? = null): DataBindingObservable,
        RealmChangeListener<Realm>, Closeable, AnkoLogger {
    override var mCallbacks: PropertyChangeRegistry? = null
    private val executor = Executors.newSingleThreadExecutor()
    val url: ObservableString = ObservableString(store.parameters)

    @get:Bindable
    var errorText: String? by DataBindingDelegates.observable(BR.errorText, null)

    val urlCallback: Observable.OnPropertyChangedCallback = object:Observable.OnPropertyChangedCallback() {
        var asyncResult: Future<Unit>? = null

        @Synchronized
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            val result = asyncResult
            if (result != null) {
                result.cancel(true)
                asyncResult = null
            }
            asyncResult = async(executor) {
                try {
                    errorText = if (!url.isNullOrEmpty()) {
                        Thread.sleep(500)
                        val testUrl = url.get()
                        if (!testUrl.isNullOrEmpty()) {
                            errorText = NightscoutSync.getInstance().testUrl(testUrl!!)
                        }
                        errorText
                    } else null
                } catch (e: InterruptedException) {
                } catch (e: Exception) {
                    error("Exception getting result:", e)
                    errorText = e.message
                    e.message
                }
            }
        }
    }

    init {
        realm?.addChangeListener(this)
        info ("adding change listener")
        url.addOnPropertyChangedCallback(urlCallback)
    }

    override fun close() {
        realm?.removeChangeListener(this)
    }

    override fun onChange(item: Realm) {
        if (store.isValid) {
            info("forwarding model URL change")
            url.set(store.parameters)
        }
    }

    @Suppress("unused")
    fun onSave() {
        val realm = Realm.getDefaultInstance()
        realm.use {
            realm.transaction {
                if (store.isValid) {
                    info("Store claims to be valid")
                    store.parameters = url.get() ?: ""
                    store.storeType = SyncStore.STORE_TYPE_NIGHTSCOUT
                } else {
                    val storeId = realm.where<SyncStore>().max("storeId")?.toInt() ?: 0 + 1
                    store.storeId = storeId
                    store.parameters = url.get() ?: ""
                    store.storeType = SyncStore.STORE_TYPE_NIGHTSCOUT
                    realm.copyToRealmOrUpdate(store)
                }
            }
        }
    }

    fun onConfigureUploader(view: View) {

    }
}