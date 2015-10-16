package com.kludgenics.cgmlogger.model.realm.cards

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.RealmClass

/**
 * Created by matthias on 10/11/15.
 */

@RealmClass
public open class CardList: RealmObject() {
    public open var id: Long = 0
    public open var cards: RealmList<CardMetadata> = RealmList()
}