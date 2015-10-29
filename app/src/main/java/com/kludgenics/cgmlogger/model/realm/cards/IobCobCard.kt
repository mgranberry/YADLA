package com.kludgenics.cgmlogger.model.realm.cards

import io.realm.RealmObject
import io.realm.annotations.Required
import java.util.*

public open class IobCobCard : RealmObject(), Card {
    override var metadata: CardMetadata = CardMetadata()

    @Required
    public open var day: Date = Date()

    public open var duration: Long = 0

    @Required
    public open var iobPoly = ByteArray(0)

    @Required
    public open var cobPoly = ByteArray(0)

    @Required
    public open var bgTrendline = ByteArray(0)
}