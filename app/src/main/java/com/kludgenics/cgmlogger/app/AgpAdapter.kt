package com.kludgenics.cgmlogger.app

import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.kludgenics.cgmlogger.app.view.*
import io.realm.Realm
import org.jetbrains.anko.*
import com.kludgenics.cgmlogger.app.R
import com.kludgenics.cgmlogger.model.math.agp.AgpUtil
import org.joda.time.Period
import kotlin.properties.Delegates

/**
 * Created by matthiasgranberry on 5/31/15.
 */
public class AgpAdapter(val periods: Array<Period>): RecyclerView.Adapter<AgpAdapter.ViewHolder>() {

    val realm: Realm by Delegates.blockingLazy() {
        Realm.getDefaultInstance()
    }

    class ViewHolder(public val agpView: View,
                     public val chartView: AgpChartView): RecyclerView.ViewHolder(agpView) {
    }

    override fun onBindViewHolder(holder: ViewHolder, id: Int) {
        val agp = AgpUtil.getLatestCached(holder.agpView.getContext(), realm, periods[id], {
            val inner = it.inner
            val median = it.median
            val outer = it.outer
            holder.agpView.getContext().uiThread {
                holder.chartView.innerPathString = inner
                holder.chartView.medianPathString = median
                holder.chartView.outerPathString = outer
                //holder.chartView.requestLayout()
                holder.chartView.invalidate()
                notifyItemChanged(id)
                //notifyDataSetChanged()
            }
        })
        val inner = agp.inner
        val median = agp.median
        val outer = agp.outer
        holder.agpView.getContext().uiThread {
            holder.chartView.innerPathString = inner
            holder.chartView.medianPathString = median
            holder.chartView.outerPathString = outer
            holder.chartView.invalidate()
            holder.chartView.requestLayout()
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val cardView = CardView(viewGroup.getContext())
        var chart: AgpChartView? = null
        cardView.linearLayout {
            //padding = viewGroup.getContext().dip(16)
            chart = agpChartView().layoutParams(width=matchParent, height=wrapContent)
        }
        cardView.contentDescription = "Graph of blood glucose"

        return ViewHolder(cardView, chart!!)
    }

    override fun getItemCount(): Int {
        return periods.size()
    }
}
