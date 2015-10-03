package com.kludgenics.cgmlogger.app

import android.graphics.Color
import android.os.Build
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
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
import com.kludgenics.cgmlogger.model.glucose.BloodGlucoseRecord
import com.kludgenics.cgmlogger.model.math.agp.dateTime
import com.kludgenics.cgmlogger.model.math.bgi.Bgi
import com.kludgenics.cgmlogger.model.math.bgi.BgiUtil
import com.kludgenics.cgmlogger.model.math.trendline.CachedPeriod
import com.kludgenics.cgmlogger.model.math.trendline.PeriodUtil
import com.kludgenics.cgmlogger.model.math.trendline.dateTime
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

/**
 * Created by matthiasgranberry on 5/31/15.
 */
public class TrendlineAdapter(val periods: List<Pair<DateTime,Period>>): RecyclerView.Adapter<TrendlineAdapter.ViewHolder>(), AnkoLogger {

    val fmt = DateTimeFormat.forPattern("EEE MMM dd")

    data class ViewHolder(var trendView: CardView,
                          var chartView: DailyBgChartView? = null,
                          var textView: TextView? = null,
                          var periodFuture: Future<CachedPeriod>? = null): RecyclerView.ViewHolder(trendView) {
    }

    override fun onBindViewHolder(holder: ViewHolder, id: Int) {
        //val bgri = BgiUtil.getLatestCached(DateTime(), periods[id])
        val per = PeriodUtil.getLatestCached(holder.trendView.getContext(), periods[id].first, periods[id].second, {
            holder.periodFuture = it
            if (!it.isCancelled() && it == holder.periodFuture) { // don't update the wrong view
                try {
                    val per = holder.periodFuture?.get(20, TimeUnit.SECONDS)
                    val trendLine = per?.trendLine
                    val date = per?.dateTime
                    if (!it.isCancelled() && it == holder.periodFuture && holder.getAdapterPosition() >= 0) {
                        holder.trendView.getContext().uiThread {
                            holder.chartView?.trendPathString = trendLine ?: ""
                            holder.chartView?.requestLayout()
                            holder.chartView?.invalidate()

                            if (date != null) {
                                holder.textView?.text = fmt.print(date)
                            }
                            notifyItemChanged(holder.getLayoutPosition())
                            info("notifyItemChanged(${holder.getAdapterPosition()}) (${holder.getItemId()} ${per?.date} ${per?.period})")
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
        val trendLine = per.trendLine
        val date = per.dateTime;
        holder.chartView!!.context.onUiThread {
            holder.chartView?.trendPathString = trendLine
            holder.chartView?.requestLayout()
            holder.chartView?.invalidate()
            holder.textView?.text = fmt.print(date)
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super<RecyclerView.Adapter>.onViewRecycled(holder)
        holder.periodFuture?.cancel(true)
        holder.periodFuture = null
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val ctx = viewGroup.getContext()
        val holder = ViewHolder(CardView(ctx))
        with(ctx) {
            with(holder.trendView) {
                onClick {
                    configuration(fromSdk = Build.VERSION_CODES.LOLLIPOP) {
                        setTransitionGroup(true)
                        setTransitionName("trendline")
                    }
                    //startActivity()
                }
                setCardBackgroundColor(resources!!.getColor(R.color.cardview_light_background))
                contentDescription = "Graph of blood glucose"
                setRadius(dip(5).toFloat())
                verticalLayout {
                    frameLayout {
                        horizontalPadding = dip(8)
                        verticalPadding = dip(5)
                        holder.chartView = dailyBgChartView {
                            lowLine = 80
                            targetLine = 110
                            highLine = 180
                        }.lparams(width = matchParent, height = wrapContent)
                        holder.textView = textView {
                            gravity = Gravity.CENTER
                            textSize = sp(6).toFloat()
                            textColor = Color.BLACK
                            backgroundResource = R.color.cardview_light_background
                            background.setAlpha(128)
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
        return holder
    }

    override fun getItemCount(): Int {
        return periods.size()
    }
}
