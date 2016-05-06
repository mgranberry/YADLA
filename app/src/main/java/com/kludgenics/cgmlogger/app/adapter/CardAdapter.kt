package com.kludgenics.cgmlogger.app.adapter

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.kludgenics.cgmlogger.app.databinding.CardChartBinding
import com.kludgenics.cgmlogger.app.databinding.CardDeviceStatusBinding
import com.kludgenics.cgmlogger.app.viewmodel.ObservableStatus
import com.kludgenics.cgmlogger.app.viewmodel.RealmStatus
import io.realm.RealmChangeListener
import io.realm.RealmObject
import io.realm.RealmResults
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.layoutInflater

/**
 * Created by matthias on 3/22/16.
 */
class CardAdapter(private val results: RealmResults<out RealmObject>) : AnkoLogger, RecyclerView.Adapter<BindingViewHolder>(), RealmChangeListener{

    val positionMap = hashMapOf<RealmObject, Pair<Int, RealmChangeListener>>()
    init {
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
        val VIEW_TYPE_CHART = 1
        val VIEW_TYPE_UNKNOWN = 2
    }

    override fun onBindViewHolder(holder: BindingViewHolder, position: Int) {
        when (holder.binding) {
            is CardDeviceStatusBinding -> holder.binding.status = ObservableStatus(results[position] as RealmStatus)
            is CardChartBinding -> {
                val periodHours = when (position - results.lastIndex) {
                    1 -> 3
                    2 -> 12
                    3 -> 24
                    else -> 0
                }
                holder.binding.period = 60000 * 60 * periodHours
            }
        }
        holder.binding.executePendingBindings()
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView?) {
        super.onDetachedFromRecyclerView(recyclerView)
        results.removeChangeListener(this)
    }

    override fun getItemCount(): Int = results.size + 3

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder {
        return when (viewType) {
            VIEW_TYPE_STATUS -> {
                val statusBinding = CardDeviceStatusBinding.inflate(parent.context.layoutInflater, parent, false)
                BindingViewHolder(statusBinding)
            }
            VIEW_TYPE_CHART -> {
                val chartBinding = CardChartBinding.inflate(parent.context.layoutInflater, parent, false)
                BindingViewHolder(chartBinding)
            }
            else -> throw UnsupportedOperationException()
        }
    }

    override fun getItemViewType(position: Int): Int {
        info("getItemViewType($position) ($itemCount)")
        if (position > results.lastIndex) {
            return VIEW_TYPE_CHART
        } else
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