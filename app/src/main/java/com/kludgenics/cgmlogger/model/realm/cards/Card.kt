package com.kludgenics.cgmlogger.model.realm.cards

import com.kludgenics.cgmlogger.extension.where
import io.realm.Realm

/**
 * Created by matthias on 10/14/15.
 */

interface Card {
    companion object {
        fun retrieve(metadata: CardMetadata): Card? {
            val r = Realm.getDefaultInstance()
            return when (metadata.cardtType) {
                AgpCard.TYPE -> r.where<AgpCard> {equalTo("metadata.id", metadata.id)}.findFirst()
                IobCobCard.TYPE -> r.where<IobCobCard> {equalTo("metadata.id", metadata.id)}.findFirst()
                ModalDayCard.TYPE -> r.where<ModalDayCard> {equalTo("metadata.id", metadata.id)}.findFirst()
                TreatmentDetailCard.TYPE -> r.where<TreatmentDetailCard> {equalTo("metadata.id", metadata.id)}.findFirst()
                else -> null
            }
        }
    }
    public var metadata: CardMetadata
}