package com.kludgenics.cgmlogger.model.realm.glucose

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by matthiasgranberry on 6/7/15.
 */
public open class BgByDay : RealmObject() {
    @PrimaryKey
    public open var day: Long = 0
    public open var bgRecords: RealmList<BloodGlucoseRecord> = RealmList()
}
