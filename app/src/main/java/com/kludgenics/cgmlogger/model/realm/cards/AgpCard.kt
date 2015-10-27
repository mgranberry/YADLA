package com.kludgenics.cgmlogger.model.realm.cards

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import io.realm.annotations.Required
import java.util.*

@RealmClass
public open class AgpCard : RealmObject(), Card {
    companion object {
        public val TYPE = 0
    }
    override public var metadata: CardMetadata = CardMetadata()
    @Required
    public open var day: Date = Date()
    public open var period: Int = 1
    @Required
    public open var agpGraph: String = ""
}