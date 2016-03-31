package com.kludgenics.cgmlogger.app.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by matthias on 3/23/16.
 */

open class SyncStore(var parameters: String = "", var storeType: Int = STORE_TYPE_NONE, @PrimaryKey var storeId: Int = -1): RealmObject() {
    companion object {
        const val STORE_TYPE_NONE = 0
        const val STORE_TYPE_NIGHTSCOUT = 1
    }
}