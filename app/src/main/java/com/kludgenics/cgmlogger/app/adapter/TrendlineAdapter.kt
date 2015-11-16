package com.kludgenics.cgmlogger.app.adapter

import android.graphics.Color
import android.os.Build
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import com.kludgenics.cgmlogger.app.R
import com.kludgenics.cgmlogger.app.util.PathParser
import com.kludgenics.cgmlogger.app.view.DailyBgChartView
import com.kludgenics.cgmlogger.app.view.dailyBgChartView
import com.kludgenics.cgmlogger.model.flatbuffers.path.BloodGlucoseDay
import com.kludgenics.cgmlogger.model.flatbuffers.path.BloodGlucosePeriod
import com.kludgenics.cgmlogger.model.math.bgi.BgiUtil
import com.kludgenics.cgmlogger.model.math.trendline.CachedPeriod
import com.kludgenics.cgmlogger.model.math.trendline.PeriodUtil
import com.kludgenics.cgmlogger.model.math.trendline.dateTime
import com.kludgenics.cgmlogger.model.realm.glucose.BgByPeriod
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmList
import io.realm.RealmResults
import org.jetbrains.anko.*
import org.joda.time.DateTime
import org.joda.time.Period
import org.joda.time.format.DateTimeFormat
import java.nio.ByteBuffer
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

/**
 * Created by matthiasgranberry on 5/31/15.
 */
public class TrendlineAdapter(val periods: RealmResults<BgByPeriod>): RecyclerView.Adapter<TrendlineAdapter.TrendlineViewHolder>(), AnkoLogger {
    val realm = Realm.getDefaultInstance()
    val l = RealmChangeListener { notifyDataSetChanged() }
    init {
        periods.forEach { it.addChangeListener(l) }
        periods.addChangeListener(l)
    }

    val fmt = DateTimeFormat.forPattern("EEE MMM dd")

    class TrendlineViewHolder(var trendView: CardView,
                     var chartView: DailyBgChartView? = null,
                     var textView: TextView? = null,
                     var periodFuture: Future<CachedPeriod>? = null): RecyclerView.ViewHolder(trendView) {
    }

    override fun onBindViewHolder(holder: TrendlineViewHolder, id: Int) {
        try {
            val md = BloodGlucoseDay.getRootAsBloodGlucoseDay(ByteBuffer.wrap(periods[id].data))
            val trendPath = PathParser.copyFromPathDataBuffer(md.trendline())
            val date = periods[id].start;
            holder.chartView!!.context.onUiThread {
                holder.chartView?.trendPathData = trendPath
                holder.chartView?.requestLayout()
                holder.chartView?.invalidate()
                holder.textView?.text = "${fmt.print(date)} ${md.period().adrr()} ${md.period().average()}"
            }
        } catch (e: IndexOutOfBoundsException) {
            info(e)
            // empty days are the cause of this.
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
        return periods.size// + 1
    }
}
