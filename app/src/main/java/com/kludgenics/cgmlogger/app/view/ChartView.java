package com.kludgenics.cgmlogger.app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
/*import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.FillFormatter;
  */
/**
 * Created by matthiasgranberry on 5/9/15.
 */
public class ChartView extends View {
    public ChartView(Context context) {
        this(context, null);
    }

    public ChartView(Context context, AttributeSet attrs) {
        this(context, null, 0);
    }

    public ChartView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
}
