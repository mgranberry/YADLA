package com.kludgenics.cgmlogger.model.realm.cards

import io.realm.RealmObject
import io.realm.annotations.Required
import java.util.*

public open class TreatmentOverviewCard : RealmObject(), Card {
    override var metadata: CardMetadata = CardMetadata()

    public open var lbgi: Float = 0f
    public open var hbgi: Float = 0f
    public open var adrr: Float = 0f
    public open var percentile975: Float = 0f
    public open var percentile025: Float = 0f
    public open var date: Date = Date()
    public open var periodMillis: Long = 0
    public open var durationMillis: Long = 0
    @Required
    public open var summary: String = ""
}