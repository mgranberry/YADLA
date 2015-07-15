package com.kludgenics.cgmlogger.app

import android.graphics.Color
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
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
import com.kludgenics.cgmlogger.extension.*
import com.kludgenics.cgmlogger.model.math.agp.dateTime

/**
 * Created by matthiasgranberry on 5/31/15.
 */
public class AgpAdapter(val periods: Array<Period>): RecyclerView.Adapter<AgpAdapter.ViewHolder>(), AnkoLogger {

    val realm: Realm by Delegates.blockingLazy() {
        Realm.getDefaultInstance()
    }

    class ViewHolder(public val agpView: View,
                     public val chartView: AgpChartView,
                     public val textView: TextView? = null,
                     public var agpFuture: Future<CachedDatePeriodAgp>? = null): RecyclerView.ViewHolder(agpView) {
    }

    override fun onBindViewHolder(holder: ViewHolder, id: Int) {
        val agp = AgpUtil.getLatestCached(holder.agpView.getContext(), realm, periods[id], {
            holder.agpFuture = it
            if (!it.isCancelled() && it == holder.agpFuture) { // don't update the wrong view
                try {
                    val agp = holder.agpFuture?.get(20, TimeUnit.SECONDS)
                    val inner = agp?.inner
                    val median = agp?.median
                    val outer = agp?.outer
                    val end = agp?.dateTime
                    val days = agp?.period
                    if (!it.isCancelled() && it == holder.agpFuture && holder.getAdapterPosition() >= 0) {
                        holder.chartView.getContext().uiThread {
                            holder.chartView.innerPathString = inner ?: ""
                            holder.chartView.medianPathString = median ?: ""
                            holder.chartView.outerPathString = outer ?: ""
                            holder.chartView.invalidate()
                            holder.textView?.text = "$days-day AGP"
                            notifyItemChanged(holder.getLayoutPosition())
                            info("notifyItemChanged(${holder?.getAdapterPosition()}) (${holder.getItemId()} ${agp?.date} ${agp?.period})")
                        }
                    }
                } catch (c: CancellationException) {
                    info("future cancelled for ${periods[id]}")
                } catch (e: InterruptedException) {
                    info("future interrupted for ${periods[id]}")
                } catch (e: ExecutionException) {
                    error("Error in agp callback: ${e}")
                    error("${e.getStackTraceString()}")
                }
            }
        })
        val inner = agp.inner
        val median = agp.median
        val outer = agp.outer
        val end = agp.dateTime
        val days = agp.period
        holder.agpView.getContext().uiThread {
            holder.chartView.innerPathString = inner
            holder.chartView.medianPathString = median
            holder.chartView.outerPathString = outer
            holder.chartView.invalidate()
            holder.textView?.text = "$days-day AGP"

        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super<RecyclerView.Adapter>.onViewRecycled(holder)
        holder.agpFuture?.cancel(true)
        holder.agpFuture = null
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val ctx = viewGroup.getContext()
        var chart: AgpChartView? = null
        var textView: TextView? = null
        val card: CardView = CardView(ctx)
        with(ctx) {
            with(card) {
                setCardBackgroundColor(resources!!.getColor(R.color.cardview_light_background))
                contentDescription = "Graph of blood glucose"
                setRadius(dip(5).toFloat())
                frameLayout {
                    paddingHorizontal = dip(8)
                    paddingVertical = dip(5)
                    chart = agpChartView {}.layoutParams(width = matchParent, height = wrapContent)
                    textView = textView {
                        gravity = Gravity.CENTER
                        textSize = sp(8).toFloat()
                        textColor = Color.BLACK
                        backgroundResource = R.color.cardview_light_background
                        background.setAlpha(128)
                    }.layoutParams(width = wrapContent, height = wrapContent) {
                        gravity = (Gravity.TOP or Gravity.CENTER_HORIZONTAL)
                    }
                }
            }
            card.layoutParams = ViewGroup.MarginLayoutParams(matchParent, wrapContent)
            with(card.layoutParams as ViewGroup.MarginLayoutParams) {
                bottomMargin = dip(15)
                leftMargin = dip(15)
                rightMargin = dip(15)
            }
        }
        return ViewHolder(card, chart!!, textView)
    }

    override fun getItemCount(): Int {
        return periods.size()
    }
}
