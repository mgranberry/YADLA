package com.kludgenics.glucosegrapher;

import android.content.Context;
import android.util.AttributeSet;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.FillFormatter;

/**
 * Created by matthiasgranberry on 5/9/15.
 */
public class GlucoseChart extends LineChart {
    public GlucoseChart(Context context) {
        super(context);
        LineDataSet s;
        FillFormatter formatter;
    }

    public GlucoseChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GlucoseChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
}
