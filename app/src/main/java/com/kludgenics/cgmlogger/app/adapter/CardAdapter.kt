package com.kludgenics.cgmlogger.app.adapter

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.kludgenics.cgmlogger.app.databinding.CardDeviceStatusBinding
import com.kludgenics.cgmlogger.app.viewmodel.ObservableStatus
import com.kludgenics.cgmlogger.app.viewmodel.RealmStatus
import io.realm.RealmChangeListener
import io.realm.RealmObject
import io.realm.RealmResults
import org.jetbrains.anko.layoutInflater

/**
 * Created by matthias on 3/22/16.
 */
class CardAdapter(private val results: RealmResults<out RealmObject>) : RecyclerView.Adapter<BindingViewHolder>(), RealmChangeListener{

    val positionMap = hashMapOf<RealmObject, Pair<Int, RealmChangeListener>>()
    init {
        setHasStableIds(true)
        results.addChangeListener(this)
        /*
        Use this once Realm supports fine-grained change notifications.

        results.filterNotNull().forEachIndexed { idx, realmObject ->
            positionMap[realmObject] = idx to RealmChangeListener { onItemChanged(realmObject) }
            realmObject.addChangeListener (positionMap[realmObject]?.second)
        }
        */
    }

    companion object {
        val VIEW_TYPE_STATUS = 0
        val VIEW_TYPE_SEPARATOR = 1
        val VIEW_TYPE_UNKNOWN = 2
    }

    init {
    }

    override fun onBindViewHolder(holder: BindingViewHolder, position: Int) {
        when (holder.binding) {
            is CardDeviceStatusBinding -> holder.binding.status = ObservableStatus(results[position] as RealmStatus)
        }
        holder.binding.executePendingBindings()
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView?) {
        super.onDetachedFromRecyclerView(recyclerView)
        results.removeChangeListener(this)
    }

    override fun getItemCount(): Int = results.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder {
        return when (viewType) {
            VIEW_TYPE_STATUS -> {
                val statusBinding = CardDeviceStatusBinding.inflate(parent.context.layoutInflater, parent, false)
                BindingViewHolder(statusBinding)
            }
            else -> throw UnsupportedOperationException()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (results.get(position)) {
            is RealmStatus -> VIEW_TYPE_STATUS
            else -> VIEW_TYPE_UNKNOWN
        }
    }

    override fun onChange() {
        println("results onChange()")
        /*
        Use this once Realm supports fine-grained change notifications.

        results.filterNotNull().forEachIndexed { idx, realmObject ->
            val oldListener = positionMap[realmObject]?.second
            if (oldListener != null)
                realmObject.removeChangeListener(oldListener)
            val newListener = RealmChangeListener { onItemChanged(realmObject) }
            positionMap[realmObject] = idx to newListener
            realmObject.addChangeListener (positionMap[realmObject]?.second)
        } */
        notifyDataSetChanged()
    }

    fun onItemChanged(realmObject: RealmObject) {
        println("results object changed: $realmObject")
        val result = positionMap[realmObject]
        if (result != null)
            notifyItemChanged(result.first)
    }
}