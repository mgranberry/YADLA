package com.kludgenics.cgmlogger.model.realm.cards

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import io.realm.annotations.Required
import java.util.*

@RealmClass
public open class TreatmentDetailCard : RealmObject(), Card {
    companion object {
        public val TYPE = 3
    }

    @Required
    override var metadata: CardMetadata = CardMetadata()

    @Required
    public open var summary: String = ""
}