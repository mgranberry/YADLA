package com.kludgenics.cgmlogger.app.adapter

import android.graphics.Color
import android.os.Build
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import com.kludgenics.cgmlogger.app.R
import com.kludgenics.cgmlogger.app.view.DailyBgChartView
import com.kludgenics.cgmlogger.app.view.dailyBgChartView
import com.kludgenics.cgmlogger.model.math.bgi.BgiUtil
import com.kludgenics.cgmlogger.model.math.trendline.CachedPeriod
import com.kludgenics.cgmlogger.model.math.trendline.PeriodUtil
import com.kludgenics.cgmlogger.model.math.trendline.dateTime
import org.jetbrains.anko.*
import org.joda.time.DateTime
import org.joda.time.Period
import org.joda.time.format.DateTimeFormat
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

/**
 * Created by matthiasgranberry on 5/31/15.
 */
public class TrendlineAdapter(val periods: List<Pair<DateTime, Period>>): RecyclerView.Adapter<TrendlineAdapter.TrendlineViewHolder>(), AnkoLogger {

    val fmt = DateTimeFormat.forPattern("EEE MMM dd")

    class TrendlineViewHolder(var trendView: CardView,
                     var chartView: DailyBgChartView? = null,
                     var textView: TextView? = null,
                     var periodFuture: Future<CachedPeriod>? = null): RecyclerView.ViewHolder(trendView) {
    }

    override fun onBindViewHolder(holder: TrendlineViewHolder, id: Int) {
        val bgri = BgiUtil.getLatestCached(periods[id].first, periods[id].second)
        val per = PeriodUtil.getLatestCached(holder.trendView.context, periods[id].first, periods[id].second, {
            holder.periodFuture = it
            if (!it.isCancelled && it == holder.periodFuture) { // don't update the wrong view
                try {
                    val per = holder.periodFuture?.get(20, TimeUnit.SECONDS)
                    val trendLine = per?.trendLine
                    val date = per?.dateTime
                    if (!it.isCancelled && it == holder.periodFuture && holder.adapterPosition >= 0) {
                        holder.trendView.context.onUiThread {
                            holder.chartView?.trendPathString = trendLine ?: ""
                            holder.chartView?.requestLayout()
                            holder.chartView?.invalidate()

                            if (date != null) {
                                holder.textView?.text = "${fmt.print(date)}: ${bgri?.adrr}"
                            }
                            notifyItemChanged(holder.layoutPosition)
                            info("notifyItemChanged(${holder.adapterPosition}) (${holder.itemId} ${per?.date} ${per?.period})")
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
        val trendLine = per.trendLine
        val date = per.dateTime;
        holder.chartView!!.context.onUiThread {
            holder.chartView?.trendPathString = trendLine
            holder.chartView?.requestLayout()
            holder.chartView?.invalidate()
            holder.textView?.text = "${fmt.print(date)}: ${bgri?.adrr}"
        }
    }

    override fun onViewRecycled(holder: TrendlineViewHolder) {
        super.onViewRecycled(holder)
        holder.periodFuture?.cancel(true)
        holder.periodFuture = null
    }

    override fun getItemViewType(position: Int): Int {
        return 1 // if (position == 0) 0 else 1
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): TrendlineViewHolder {
        val ctx = viewGroup.context
        val holder = TrendlineViewHolder(CardView(ctx))

        if (viewType == 1) {
            with(ctx) {
                with(holder.trendView) {
                    onClick {
                        configuration(fromSdk = Build.VERSION_CODES.LOLLIPOP) {
                            isTransitionGroup = true
                            transitionName = "trendline"
                        }
                        //startActivity()
                    }
                    setCardBackgroundColor(resources!!.getColor(R.color.cardview_light_background))
                    contentDescription = "Graph of blood glucose"
                    radius = dip(5).toFloat()
                    verticalLayout {
                        frameLayout {
                            horizontalPadding = dip(8)
                            verticalPadding = dip(5)
                            holder.chartView = dailyBgChartView {
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
                holder.trendView.layoutParams = ViewGroup.MarginLayoutParams(matchParent, wrapContent)
                with(holder.trendView.layoutParams as ViewGroup.MarginLayoutParams) {
                    bottomMargin = dip(15)
                    leftMargin = dip(15)
                    rightMargin = dip(15)
                }
            }
        } else {
            with(ctx) {

            }
        }
        return holder
    }

    override fun getItemCount(): Int {
        return periods.size()// + 1
    }
}
