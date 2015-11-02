package com.kludgenics.cgmlogger.model.realm.cards

import io.realm.RealmObject
import io.realm.annotations.Required
import java.util.*

public open class ModalCard : RealmObject(), Card, StatsCard {
    override var metadata: CardMetadata = CardMetadata()

    @Required
    public open var day: Date = Date()
    public override var periodMillis = 86400000L // 1 day
    @Required
    public open var trendline = ByteArray(0)
    public override var statistics = ByteArray(0)
}