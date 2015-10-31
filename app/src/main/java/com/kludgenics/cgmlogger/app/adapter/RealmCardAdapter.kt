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

    protected var results: CardList? = null
    private val ids = ArrayList<Int>()

    private val differ = Diff(ids, { old, new ->
        val realm = Realm.getDefaultInstance()
        DateTime(realm.where<CardMetadata> {
            equalTo("id", new)
        }.findFirst().lastUpdated) > lastUpdate
    })

    private var lastUpdate: DateTime = DateTime.now()

    private val listener = RealmChangeListener {
        val changeSet = differ.update(ids)
        changeSet.operations.forEach { operation ->
            when (operation.operation) {
                Diff.OP_REMOVE -> {
                    notifyItemRemoved(operation.from)
                    ids.removeAt(operation.from)
                }
                Diff.OP_INSERT -> {
                    notifyItemInserted(operation.from)
                    ids.add(operation.from, operation.item)
                }
                Diff.OP_MOVE -> {
                    notifyItemMoved(operation.from, operation.to)
                    ids.removeAt(operation.from)
                    ids.add(operation.to, operation.item)
                }
                Diff.OP_MODIFY -> {
                    notifyItemChanged(operation.from)
                }
            }

        }
        lastUpdate = DateTime.now()
    }

    override fun getItemCount(): Int {
        return ids.size
    }

    fun updateRealmResults(queryResults: CardList?) {
        results?.removeChangeListener(listener)
        queryResults?.addChangeListener(listener)
        results = queryResults
        updateIdList(queryResults)
        differ.update(ids)
        lastUpdate = DateTime.now()
        notifyDataSetChanged()
    }

    private fun updateIdList(queryResults: CardList?) {
        ids.clear()
        queryResults?.cards?.mapTo(ids) { it.id }
    }

    override fun getItemViewType(position: Int): Int {
        return results?.cards?.get(position)?.cardtType ?: -1
    }
}