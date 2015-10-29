package com.kludgenics.cgmlogger.model.realm.cards

import io.realm.RealmObject
import io.realm.annotations.Required

public open class TreatmentDetailCard : RealmObject(), Card {
    override var metadata: CardMetadata = CardMetadata()

    @Required
    public open var summary: String = ""
}