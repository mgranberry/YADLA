package com.kludgenics.cgmlogger.app.adapter

import android.support.v7.widget.RecyclerView
import com.kludgenics.cgmlogger.model.realm.cards.CardList
import com.kludgenics.cgmlogger.extension.*
import com.kludgenics.cgmlogger.model.realm.cards.CardMetadata
import com.kludgenics.util.Diff
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.joda.time.DateTime
import java.util.*

abstract class RealmCardAdapter<T: RecyclerView.ViewHolder>(): RecyclerView.Adapter<T>(), AnkoLogger {

    private var results: RealmResults<CardList>? = null
    private val ids = ArrayList<Long>()

    private val differ = Diff(ids, { old, new ->
        val realm = Realm.getDefaultInstance()
        DateTime(realm.where<CardMetadata> {
            equalTo("id", new)
        }.findFirst().lastUpdated) > lastUpdate
    })

    private var lastUpdate: DateTime = DateTime.now()
    private var listener = RealmChangeListener {
        info("new id list: $ids")
        info("old differ list: ${differ.old}")
        val changeSet = differ.update(ids)
        info("change list: $changeSet")
        info("new differ list: ${differ.old}")
        changeSet.operations.forEach { operation ->
            when (operation.operation) {
                Diff.OP_REMOVE -> notifyItemRemoved(operation.from)
                Diff.OP_INSERT -> notifyItemInserted(operation.from)
                Diff.OP_MOVE -> notifyItemMoved(operation.from, operation.to)
                Diff.OP_MODIFY -> notifyItemChanged(operation.from)
            }
        }
        lastUpdate = DateTime.now()
    }

    override fun getItemCount(): Int {
        return synchronized(ids) {
            ids.size
        }
    }

    fun updateRealmResults(queryResults: RealmResults<CardList>?) {
        val realm = Realm.getDefaultInstance()
        synchronized(this) {
            if (results != null) {
                realm.removeChangeListener(listener)
            }
            if (queryResults != null) {
                realm.addChangeListener(listener)
            }
            results = queryResults
            updateIdList(queryResults)
            differ.update(ids)
            lastUpdate = DateTime.now()
            notifyDataSetChanged()
        }
        realm.close()
    }

    private fun updateIdList(queryResults: RealmResults<CardList>?) {
        synchronized(ids) {
            ids.clear()
            queryResults?.flatMapTo(ids) {
                it.cards.map { it.id }
            }
        }
    }
}