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
import com.kludgenics.cgmlogger.model.math.agp.CachedDatePeriodAgp
import org.joda.time.Period
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

/**
 * Created by matthiasgranberry on 5/31/15.
 */
public class AgpAdapter(val periods: Array<Period>): RecyclerView.Adapter<AgpAdapter.ViewHolder>(), AnkoLogger {

    val realm: Realm by Delegates.blockingLazy() {
        Realm.getDefaultInstance()
    }

    class ViewHolder(public val agpView: View,
                     public val chartView: AgpChartView,
                     public var agpFuture: Future<CachedDatePeriodAgp>? = null): RecyclerView.ViewHolder(agpView) {
    }

    override fun onBindViewHolder(holder: ViewHolder, id: Int) {
        val agp = AgpUtil.getLatestCached(holder.agpView.getContext(), realm, periods[id], {
            holder.agpFuture = it
            if (!it.isCancelled()) { // don't update the wrong view
                try {
                    val agp = it.get(20, TimeUnit.SECONDS)
                    val inner = agp.inner
                    val median = agp.median
                    val outer = agp.outer
                    if (!it.isCancelled()) {
                        holder.chartView.innerPathString = inner
                        holder.chartView.medianPathString = median
                        holder.chartView.outerPathString = outer
                        holder.chartView.invalidate()
                        notifyItemChanged(id)
                    }
                } catch (c: CancellationException) {
                } catch (e: InterruptedException) {
                } catch (e: ExecutionException) {
                    error("Error in agp callback: ${e}")
                    error("${e.getStackTraceString()}")
                }
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
        }
    }

    override fun onViewRecycled(holder: ViewHolder?) {
        super<RecyclerView.Adapter>.onViewRecycled(holder)
        val af = holder?.agpFuture
        holder?.agpFuture = null
        af?.cancel(false)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val cardView = CardView(viewGroup.getContext())
        var chart: AgpChartView? = null
        cardView.linearLayout {
            //padding = viewGroup.getContext().dip(16)
            paddingHorizontal = viewGroup.getContext().dip(8)
            paddingVertical = viewGroup.getContext().dip(5)
            chart = agpChartView().layoutParams(width=matchParent, height=wrapContent)
        }
        cardView.contentDescription = "Graph of blood glucose"

        return ViewHolder(cardView, chart!!)
    }

    override fun getItemCount(): Int {
        return periods.size()
    }
}
