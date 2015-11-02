package com.kludgenics.cgmlogger.model.realm.cards

import com.kludgenics.cgmlogger.extension.where
import io.realm.Realm

/**
 * Created by matthias on 10/14/15.
 */

interface Card {
    companion object {

        const val CARD_TYPE_AGP = 0
        const val CARD_TYPE_IOB_COB = 1
        const val CARD_TYPE_MODAL_DAY = 2
        const val CARD_TYPE_TREATMENT = 3
        const val CARD_TYPE_OVERVIEW = 4

        fun retrieve(metadata: CardMetadata?): Card? {
            val r = Realm.getDefaultInstance()
            r.use {
                if (metadata == null) // compiler error if this is a single line
                    return null
                else
                    return when (metadata.cardtType) {
                        CARD_TYPE_AGP -> r.where<AgpCard> { equalTo("metadata.id", metadata.id) }.findFirst()
                        CARD_TYPE_IOB_COB -> r.where<IobCobCard> { equalTo("metadata.id", metadata.id) }.findFirst()
                        CARD_TYPE_MODAL_DAY -> r.where<ModalCard> { equalTo("metadata.id", metadata.id) }.findFirst()
                        CARD_TYPE_TREATMENT -> r.where<TreatmentDetailCard> { equalTo("metadata.id", metadata.id) }.findFirst()
                        CARD_TYPE_OVERVIEW -> r.where<TreatmentOverviewCard> { equalTo("metadata.id", metadata.id) }.findFirst()
                        else -> null
                    }
            }
        }
    }

    public var metadata: CardMetadata
}