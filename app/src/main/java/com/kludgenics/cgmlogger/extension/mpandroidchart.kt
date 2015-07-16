package com.kludgenics.cgmlogger.extension

import android.support.v7.widget.CardView
import android.view.ViewManager
import com.github.mikephil.charting.charts.BarChart
import org.jetbrains.anko.__dslAddView

/**
 * Created by matthiasgranberry on 7/15/15.
 */

fun ViewManager.barChart(init: BarChart.() -> Unit = {}) =
        __dslAddView({ BarChart(it) }, init, this)
