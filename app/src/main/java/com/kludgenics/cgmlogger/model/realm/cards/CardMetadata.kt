package com.kludgenics.cgmlogger.model.realm.cards

import com.kludgenics.cgmlogger.extension.create
import com.kludgenics.cgmlogger.extension.createInsideTransaction
import com.kludgenics.cgmlogger.extension.transaction
import com.kludgenics.cgmlogger.extension.where
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

/**
 * Created by matthias on 10/14/15.
 */
public open class CardMetadata: RealmObject() {
    @PrimaryKey
    public open var id: Int = 0
    public open var lastUpdated: Date? = null
    public open var cardtType: Int = -1
}

public inline fun RealmList<CardMetadata>.newCard(realm: Realm, init: CardMetadata.() -> Unit) {
    add(realm.createInsideTransaction<CardMetadata> {
        id = realm.where<CardMetadata>().max("id").toInt() + 1
        lastUpdated = Date()
        init()
    })
}