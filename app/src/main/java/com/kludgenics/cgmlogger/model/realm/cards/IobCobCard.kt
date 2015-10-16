package com.kludgenics.cgmlogger.model.realm.cards

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import io.realm.annotations.Required
import java.util.*

@RealmClass
public open class IobCobCard : RealmObject(), Card {
    companion object {
        public val TYPE = 1
    }

    override var metadata: CardMetadata = CardMetadata()

    @Required
    public open var day: Date = Date()

    @Required
    public open var iobPoly: String = ""

    @Required
    public open var cobPoly: String = ""

    @Required
    public open var bgTrendline: String = ""
}