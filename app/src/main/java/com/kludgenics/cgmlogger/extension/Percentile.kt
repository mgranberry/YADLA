package com.kludgenics.cgmlogger.extension
import android.util.Log
import kotlin.InlineOption.ONLY_LOCAL_RETURN

public fun percentile (percentile: Double, x: List<Double>): Double {
    val n = x.size();
    val p = percentile / 100.0
    val h = (n + 0.25) * p + 0.375
    val fh = Math.floor(h).toInt()
    return if (p < (0.625)/(n + 0.25))
        x[0]
    else
        if (p > (n - .375)/(n + 0.25))
            x[n - 1]
        else
            x[fh - 1] + (h - fh) * (x[fh] - x[fh - 1])
    /*val i = n * percentile/100.0 + 0.5;
    val k =i.toInt();
    val f = i - k
    Log.i("percentile", "percentiles: percentile: $percentile n:$n i:$i k:$k f:$f x:$x")
    return (1 - f)*x[k] + if (f > 0.05) f*x[k+1] else 0.0;
    */
}

inline public fun<T> Iterable<T>.percentiles(percentiles: DoubleArray, valueExtractor: (T) -> Double): DoubleArray {
    val sorted = map {valueExtractor (it) }.sort()
    return percentiles.map { percentile(it, sorted) }.toDoubleArray()
}

