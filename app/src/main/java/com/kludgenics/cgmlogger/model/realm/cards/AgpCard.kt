package com.kludgenics.cgmlogger.model.realm.cards

import io.realm.RealmObject
import io.realm.annotations.RealmClass
import io.realm.annotations.Required
import java.util.*

@RealmClass
public open class AgpCard : RealmObject() {

    @Required
    public open var day: Date = Date()

    @Required
    public open var period: Int = 1

    public open var lastUpdated: Date? = Date()

    @Required
    public open var agpGraph: String = ""
}