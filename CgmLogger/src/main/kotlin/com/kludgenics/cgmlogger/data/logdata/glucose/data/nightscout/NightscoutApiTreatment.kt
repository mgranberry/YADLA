package com.kludgenics.cgmlogger.data.logdata.glucose.data.nightscout

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.Date

/**
 * Created by matthiasgranberry on 5/24/15.
 */
public class NightscoutApiTreatment : RealmObject() {
    Expose
    PrimaryKey
    SerializedName("_id")
    public var id: String? = null
    Expose
    SerializedName("created_at")
    public var createdAt: Date? = null
    Expose
    public var eventType: String? = null
    Expose
    public var enteredBy: String? = null
    Expose
    public var glucose: Float = 0.toFloat()
    Expose
    public var insulin: Float = 0.toFloat()
    Expose
    public var glucoseType: String? = null
    Expose
    public var preBolus: Int = 0
    Expose
    public var notes: String? = null
    Expose
    public var units: String? = null
    Expose
    public var carbs: Int = 0
}