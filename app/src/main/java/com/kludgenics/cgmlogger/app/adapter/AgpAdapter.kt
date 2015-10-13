package com.kludgenics.cgmlogger.app.adapter

import android.graphics.Color
import android.os.Build
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import com.kludgenics.cgmlogger.app.DetailActivity
import com.kludgenics.cgmlogger.app.R
import com.kludgenics.cgmlogger.app.view.AgpChartView
import com.kludgenics.cgmlogger.app.view.BgRiChartView
import com.kludgenics.cgmlogger.app.view.agpChartView
import com.kludgenics.cgmlogger.model.math.agp.AgpUtil
import com.kludgenics.cgmlogger.model.math.agp.CachedDatePeriodAgp
import org.jetbrains.anko.*
import org.joda.time.Period
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

/**
 * Created by matthiasgranberry on 5/31/15.
 */
public class AgpAdapter(val periods: List<Period>): RecyclerView.Adapter<AgpAdapter.ViewHolder>(), AnkoLogger {

    class ViewHolder(var agpView: CardView,
                     var chartView: AgpChartView? = null,
                     var textView: TextView? = null,
                     var bgriView: BgRiChartView? = null,
                     var agpFuture: Future<CachedDatePeriodAgp>? = null): RecyclerView.ViewHolder(agpView) {
    }

    override fun onBindViewHolder(holder: ViewHolder, id: Int) {
        //val bgri = BgiUtil.getLatestCached(DateTime(), periods[id])
        val agp = AgpUtil.getLatestCached(holder.agpView.context, periods[id], updated = {
            holder.agpFuture = it
            if (!it.isCancelled && it == holder.agpFuture) { // don't update the wrong view
                try {
                    val agp = holder.agpFuture?.get(20, TimeUnit.SECONDS)
                    val inner = agp?.inner
                    val median = agp?.median
                    val outer = agp?.outer
                    //val end = agp?.dateTime
                    val days = agp?.period
                    if (!it.isCancelled && it == holder.agpFuture && holder.adapterPosition >= 0) {
                        holder.agpView.context.onUiThread {
                            holder.chartView?.outerPathString = outer ?: ""
                            holder.chartView?.innerPathString = inner ?: ""
                            holder.chartView?.medianPathString = median ?: ""
                            holder.chartView?.requestLayout()
                            holder.chartView?.invalidate()
                            holder.textView?.text = if (days?.compareTo(1) == 0) "$days day" else "$days days"
                            notifyItemChanged(holder.layoutPosition)
                            info("notifyItemChanged(${holder.adapterPosition}) (${holder.itemId} ${agp?.date} ${agp?.period})")
                        }
                    }
                } catch (c: CancellationException) {
                    info("future cancelled for ${periods[id]}")
                } catch (e: InterruptedException) {
                    info("future interrupted for ${periods[id]}")
                } catch (e: ExecutionException) {
                    error("Error in agp callback: $e")
                    error("${e.getStackTraceString()}")
                }
            }
        })
        val inner = agp.inner
        val median = agp.median
        val outer = agp.outer
        //val end = agp.dateTime
        val days = agp.period
        holder.agpView.context.onUiThread {
            holder.chartView?.outerPathString = outer
            holder.chartView?.innerPathString = inner
            holder.chartView?.medianPathString = median
            holder.chartView?.requestLayout()
            holder.chartView?.invalidate()
            holder.textView?.text = if (days.compareTo(1) == 0) "$days day" else "$days days"
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        holder.agpFuture?.cancel(true)
        holder.agpFuture = null
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val ctx = viewGroup.context
        val holder = ViewHolder(CardView(ctx))
        with(ctx) {
            with(holder.agpView) {


                onClick {
                    configuration(fromSdk = Build.VERSION_CODES.LOLLIPOP) {
                        isTransitionGroup = true
                        transitionName = "agp"
                        val intent = intentFor<DetailActivity>("days" to periods[holder.adapterPosition].days)
                        startActivity(intent)
                    }
                    //startActivity()
                }
                setCardBackgroundColor(resources!!.getColor(R.color.cardview_light_background))
                contentDescription = "Graph of blood glucose"
                radius = dip(5).toFloat()
                verticalLayout {
                    val rippleResource = obtainStyledAttributes(
                            intArrayOf(R.attr.selectableItemBackground)).getResourceId(0, 0)
                    backgroundResource = rippleResource

                    frameLayout {
                        horizontalPadding = dip(8)
                        verticalPadding = dip(5)
                        holder.chartView = agpChartView {
                            lowLine = 70
                            targetLine = 110
                            highLine = 180
                        }.lparams(width = matchParent, height = wrapContent)
                        holder.textView = textView {
                            gravity = Gravity.CENTER
                            textSize = sp(6).toFloat()
                            textColor = Color.BLACK
                            backgroundResource = R.color.cardview_light_background
                            background.alpha = 128
                        }.lparams(width = wrapContent, height = wrapContent) {
                            gravity = (Gravity.TOP or Gravity.START)
                        }
                    }
                    /* holder.bgriView = bgriChartView {
                        hbgPathString = ""
                        lbgPathString = ""
                    }.layoutParams(width = matchParent, height = wrapContent)
                    */
                }
            }

            holder.agpView.layoutParams = ViewGroup.MarginLayoutParams(matchParent, wrapContent)
            with(holder.agpView.layoutParams as ViewGroup.MarginLayoutParams) {
                bottomMargin = dip(15)
                leftMargin = dip(15)
                rightMargin = dip(15)
            }
        }
        return holder
    }

    override fun getItemCount(): Int {
        return periods.size()
    }
}
