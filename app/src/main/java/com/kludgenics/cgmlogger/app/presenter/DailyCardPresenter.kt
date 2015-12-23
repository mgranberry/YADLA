package com.kludgenics.cgmlogger.app.presenter

import com.kludgenics.cgmlogger.extension.create
import com.kludgenics.cgmlogger.extension.transaction
import com.kludgenics.cgmlogger.extension.where
import com.kludgenics.cgmlogger.model.realm.cards.*
import com.kludgenics.cgmlogger.model.realm.glucose.BgByPeriod
import com.kludgenics.cgmlogger.model.realm.glucose.BloodGlucoseRecord
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults
import org.jetbrains.anko.async
import org.joda.time.DateTime
import org.joda.time.Period
import java.io.Closeable
import java.util.*

/**
 * Created by matthias on 10/29/15.
 */
class DailyCardPresenter(val period: Period, val listId: Int): Closeable {

    private val realm = Realm.getDefaultInstance()
    val bgListener = RealmChangeListener { onBgChanged() }
    val treatmentListener = RealmChangeListener { onTreatmentChanged() }
    private var bgQuery: RealmResults<BloodGlucoseRecord> = updateBgQuery()
    //private var treatmentQuery: RealmResults<RealmTreatment> = updateTreatmentQuery()
    private val cardList: CardList = realm.where <CardList> {
        equalTo("id", listId)
    }.findFirst() ?: realm.create<CardList> {
        id = listId
    }

    fun updateBgQuery(): RealmResults<BloodGlucoseRecord> {
        bgQuery.removeChangeListener(bgListener)
        val results = realm.where<BloodGlucoseRecord> {
            greaterThanOrEqualTo("date", (DateTime.now().withTimeAtStartOfDay() - period).millis)
        }.findAll()
        results.addChangeListener(bgListener)
        return results
    }

    /*fun updateTreatmentQuery(): RealmResults<RealmTreatment> {
        treatmentQuery.removeChangeListener(treatmentListener)
        val results = realm.where<RealmTreatment> {
            greaterThanOrEqualTo("eventTime", (DateTime.now().withTimeAtStartOfDay() - period).toDate())
        }.findAll()
        results.addChangeListener(treatmentListener)
        return results
    }*/

    private fun updateCards() {
        async() {
            val realm = Realm.getDefaultInstance()
            val endTime = DateTime.now().withTimeAtStartOfDay().plusDays(1)
            val startTime = (endTime - period) - Period.days(1)
            val deleteList = ArrayList<CardMetadata>()
            val updateList = ArrayList<CardMetadata>()
            for (cardMetaData in cardList.cards) {
                val card = Card.retrieve(cardMetaData)
                when (card) {
                    is ModalCard -> {
                        if (DateTime(card.day) < startTime)
                            deleteList.add(card.metadata)
                    }
                }
            }

            realm.transaction {
                cardList.cards.removeAll(deleteList)
                //newCard ?: cardList.cards.add(0, newCard)
            }
        }
    }

    public fun createBgCard(startTime: DateTime) {
        val day = realm.where<BgByPeriod> { equalTo("day", startTime.millis) }.findFirst()
        if (day != null) {
            cardList.cards.newCard(realm) {

            }
        }
    }

    public fun onDateChange() {
        updateCards()
    }


    private fun onBgChanged() {
        updateCards()
    }

    private fun onTreatmentChanged() {
        updateCards()
    }

    override fun close() {
        bgQuery.removeChangeListeners()
        //treatmentQuery.removeChangeListeners()
        realm.close()
    }

}