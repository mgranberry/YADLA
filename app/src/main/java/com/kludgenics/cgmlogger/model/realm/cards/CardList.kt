package com.kludgenics.cgmlogger.model.realm.cards

import io.realm.RealmList
import io.realm.RealmObject

/**
 * Created by matthias on 10/11/15.
 */

public open class CardList: RealmObject() {
    public open var id: Int = 0
    public open var cards: RealmList<CardMetadata> = RealmList()
}