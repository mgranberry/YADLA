package com.kludgenics.cgmlogger.app.adapter

import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.kludgenics.cgmlogger.app.util.PathParser
import com.kludgenics.cgmlogger.app.view.DailyBgChartView
import com.kludgenics.cgmlogger.model.realm.cards.Card
import com.kludgenics.cgmlogger.model.realm.cards.ModalCard
import com.kludgenics.cgmlogger.model.realm.cards.TreatmentDetailCard
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

/**
 * Created by matthias on 10/29/15.
 */

class DailyCardAdapter(): RealmCardAdapter<DailyCardAdapter.ViewHolder>(), AnkoLogger {

    val fmt = DateTimeFormat.forPattern("EEE MMM dd")

    class ViewHolder(val trendView: CardView,
                     val chartView: DailyBgChartView,
                     val textView: TextView): RecyclerView.ViewHolder(trendView) {
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val ctx = viewGroup.context
        val cardView = CardView(ctx)
        val chartView = DailyBgChartView(ctx)
        val textView = TextView(ctx)
        return ViewHolder(cardView, chartView, textView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val metadata = results?.cards?.get(position)
        val card = Card.retrieve(metadata)
        when (card) {
            is ModalCard -> {
                info("Displaying ModalDayCard: ${card.metadata.cardtType} ${card.day} (${card.metadata.lastUpdated})")
                holder.textView.text = "${fmt.print(DateTime(card.day.time))}"
                holder.chartView.visibility = View.VISIBLE
                holder.chartView.trendPathData = PathParser.copyFromPathDataBufferBytes(card.trendline) ?: emptyArray()
                holder.chartView.requestLayout()
            }
            is TreatmentDetailCard -> {
                info("Displaying TreatmentDetailCard: ${card.metadata.cardtType} ${card.summary} ${card.date} (${card.metadata.lastUpdated})")

                holder.textView.text = card.summary
                holder.chartView.visibility = View.GONE
            }
        }
    }
}