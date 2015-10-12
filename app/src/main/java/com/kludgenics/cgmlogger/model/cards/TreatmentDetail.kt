package com.kludgenics.cgmlogger.model.cards

import io.realm.RealmObject
import io.realm.annotations.RealmClass
import io.realm.annotations.Required
import java.util.*

@RealmClass
public open class TreatmentDetail : RealmObject() {

    @Required
    public open var lastUpdated: Date = Date()

    @Required
    public open var summary: String = ""
}