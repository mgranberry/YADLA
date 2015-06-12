package com.kludgenics.cgmlogger.extension
import android.util.ArrayMap
import com.kludgenics.cgmlogger.model.glucose.BloodGlucose
import com.kludgenics.cgmlogger.model.glucose.BloodGlucoseRecord
import io.realm.*
import io.realm.exceptions.RealmException
import org.joda.time.DateTime
import rx.Observable
import rx.schedulers.Schedulers
import java.util.*


fun Realm.transaction (init: Realm.() -> Unit) {
    beginTransaction()
    try {
        init()
        commitTransaction()
    } catch (e: RuntimeException) {
        cancelTransaction()
        throw RealmException("Error during transaction.", e)
    } catch (e: Error) {
        cancelTransaction()
        throw e
    }
}

//inline fun <reified T: io.realm.RealmObject> Realm.create(f: (it: T) -> Unit): T {
inline fun <reified T: io.realm.RealmObject> Realm.create(init: T.() -> Unit): T {
    beginTransaction()
    val realmObject = createObject(javaClass<T>())
    realmObject.init()
    commitTransaction()
    return realmObject
}

inline fun <reified T: RealmObject> Realm.update(key: String, value: String, f: (it: T, change: MutableMap<String, String>) -> Unit): UpdateResult<T> {
    beginTransaction()
    val realmObject = where(javaClass<T>()).equalTo(key, value).findFirst()
    val changeMap: MutableMap<String, String> = hashMapOf()
    f(realmObject, changeMap)
    commitTransaction()
    return UpdateResult(realmObject, changeMap)
}

inline fun <reified T: RealmObject> Realm.where(init: RealmQuery<T>.() -> RealmQuery<T>): RealmQuery<T> {
    return where(javaClass<T>()).init()
}



inline fun <reified T: RealmObject> Realm.deleteAll() {
    transaction {
        val results = where(javaClass<T>()).findAll()
        results.clear()
    }
}

inline fun <reified T: RealmObject> Realm.delete(key: String, value: String) {
    transaction {
        val results = where(javaClass<T>()).equalTo(key, value).findAll()
        results.clear()
    }
}

inline fun <T: RealmObject> RealmQuery<T>.group(init: RealmQuery<T>.() -> RealmQuery<T>): RealmQuery<T> {
    beginGroup()
    init()
    endGroup()
    return this
}

inline var BloodGlucoseRecord.date: Date
    get() = Date(getDate())
    set(d: Date) = setDate(d.getTime())

inline var BloodGlucoseRecord.dateTime: DateTime
    get() = DateTime(getDate())
    set(dt: DateTime) = setDate(dt.getMillis())

data class UpdateResult<T: RealmObject>(val result: T, val changeMap: Map<String, String>)