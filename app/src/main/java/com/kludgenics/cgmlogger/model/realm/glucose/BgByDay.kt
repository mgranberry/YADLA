package com.kludgenics.cgmlogger.model.realm.glucose

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import io.realm.annotations.Required

/**
 * Created by matthiasgranberry on 6/7/15.
 */
@RealmClass
public open class BgByDay : RealmObject() {
    @PrimaryKey
    public open var day: Long = 0
    @Required
    public open var bgRecords: RealmList<BloodGlucoseRecord> = RealmList()
}
