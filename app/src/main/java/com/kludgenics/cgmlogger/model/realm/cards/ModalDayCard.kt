package com.kludgenics.cgmlogger.model.realm.cards

import io.realm.RealmObject
import io.realm.annotations.Required
import java.util.*

public open class ModalDayCard : RealmObject(), Card {

    override var metadata: CardMetadata = CardMetadata()

    @Required
    public open var day: Date = Date()

    @Required
    public open var trendline = ByteArray(0)
}