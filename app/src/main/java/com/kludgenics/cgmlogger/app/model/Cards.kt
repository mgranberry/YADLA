package com.kludgenics.cgmlogger.app.model

import android.support.annotation.DrawableRes
import android.support.annotation.LayoutRes
import com.kludgenics.cgmlogger.app.R
import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey
import java.util.*
import com.kludgenics.cgmlogger.extension.*
/**
 * Created by matthias on 3/26/16.
 */

interface Card {
    companion object {
        const val TYPE_UNKNOWN = 0
        const val TYPE_DEVICE_SYNC = 1
        const val TYPE_BLOOD_GLUCOSE = 2
        const val TYPE_SEPARATOR_WITH_TEXT = 3
    }
    var time: Date
    var type: Int
    var key: String
    @get:LayoutRes
    val layoutId: Int
}

open class CardMetaData(@Index override var time: Date = Date(0),
                   @PrimaryKey override var key: String = "",
                   override var type: Int = 0,
                   override var layoutId: Int = 0) : Card, RealmObject() {
    constructor(card: Card) : this(time=card.time,
            key = card.key,
            type = card.type,
            layoutId = card.layoutId)
    inline fun <reified R: RealmObject> inflate(realm: Realm): R? =
        realm.where<R> { equalTo("key", this@CardMetaData.key) }.findAll().firstOrNull() as? R

}

open class DeviceSyncCard(@Index override var time: Date = Date(0),
                     @PrimaryKey override var key: String = "",
                     @DrawableRes var icon: Int = 0,
                     var titleText: String? = null,
                     var subtitleText: String? = null,
                     var shortText: String? = null,
                     var extraText: String? = null) : Card, RealmObject() {
    override val layoutId: Int get() = R.layout.card_device_status
    override var type: Int = Card.TYPE_DEVICE_SYNC
}

open class BloodGlucoseCard(@Index override var time: Date = Date(0),
                       @PrimaryKey override var key: String = "",
                       var start: Date? = null,
                       var end: Date? = null,
                       var options: Int = 0): Card, RealmObject() {
    object Options {
        const val SHOW_BG = 1
        const val SHOW_IOB = 2
        const val SHOW_CARBS = 4
        const val SHOW_CHARTJUNK = 8
        const val SHOW_HIGH = 16
        const val SHOW_LOW = 32
        const val SHOW_HIGH_LINE = 64
        const val SHOW_LOW_LINE = 128
    }
    override val layoutId: Int get() = R.layout.card_bloodglucose
    override var type: Int = Card.TYPE_BLOOD_GLUCOSE
}