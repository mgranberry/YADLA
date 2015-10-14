package com.kludgenics.cgmlogger.extension
import com.kludgenics.cgmlogger.model.realm.glucose.BloodGlucoseRecord
import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmQuery
import io.realm.RealmResults
import io.realm.exceptions.RealmException
import org.joda.time.DateTime


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
    val realmObject = createObject(T::class.java)
    realmObject.init()
    commitTransaction()
    return realmObject
}

inline fun <reified T: io.realm.RealmObject> Realm.createInsideTransaction(init: T.() -> Unit): T {
    val realmObject = createObject(T::class.java)
    realmObject.init()
    return realmObject
}

inline fun <reified T: RealmObject> Realm.update(key: String, value: String, f: (it: T, change: MutableMap<String, String>) -> Unit): UpdateResult<T> {
    beginTransaction()
    val realmObject = where(T::class.java).equalTo(key, value).findFirst()
    val changeMap: MutableMap<String, String> = hashMapOf()
    f(realmObject, changeMap)
    commitTransaction()
    return UpdateResult(realmObject, changeMap)
}

inline fun <reified T: RealmObject> Realm.where(init: RealmQuery<T>.() -> RealmQuery<T>): RealmQuery<T> {
    return where(T::class.java).init()
}

inline fun <reified T: RealmObject> RealmResults<T>.where(init: RealmQuery<T>.() -> RealmQuery<T>): RealmQuery<T> {
    return where().init()
}

inline fun <reified T: RealmObject> Realm.deleteAll() {
    transaction {
        val results = where(T::class.java).findAll()
        results.clear()
    }
}

inline fun <reified T: RealmObject> Realm.delete(key: String, value: String) {
    transaction {
        val results = where(T::class.java).equalTo(key, value).findAll()
        results.clear()
    }
}

inline fun <T: RealmObject> RealmQuery<T>.group(init: RealmQuery<T>.() -> RealmQuery<T>): RealmQuery<T> {
    beginGroup()
    init()
    endGroup()
    return this
}

inline var BloodGlucoseRecord.dateTime: DateTime
    get() = DateTime(date)
    set(dt: DateTime) = { date = dt.millis }()

data class UpdateResult<T: RealmObject>(val result: T, val changeMap: Map<String, String>)