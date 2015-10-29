package com.kludgenics.cgmlogger.model.realm.glucose

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

/**
 * Created by matthiasgranberry on 5/24/15.
 */
public open class BloodGlucoseRecord(
        public open var value: Double = 0.0,
        public open var date: Long = 0,
        public open var type: String = "",
        @Required
        public open var unit: String = "",
        @PrimaryKey public open var id: String = ""
        ) : RealmObject() {
}

