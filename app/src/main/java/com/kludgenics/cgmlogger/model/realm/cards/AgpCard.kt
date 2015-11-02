package com.kludgenics.cgmlogger.model.realm.cards

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Required
import java.util.*

public open class AgpCard : RealmObject(), Card, StatsCard {
    override public var metadata: CardMetadata = CardMetadata()
    @Required
    public open var day: Date = Date()
    public override var periodMillis: Long = 0L
    @Required
    public open var agpGraph = ""
    public open var modal = RealmList<ModalCard>()
    override public var statistics = ByteArray(0) // BloodGlucosePeriod
}