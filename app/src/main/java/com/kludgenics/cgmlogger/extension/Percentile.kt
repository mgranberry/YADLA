package com.kludgenics.cgmlogger.extension
import kotlin.InlineOption.ONLY_LOCAL_RETURN

public fun percentile (percentile: Double, x: List<Double>): Double {
    val n = x.size();
    val i = n * percentile/100.0 + 0.5;
    val k = i.toInt();
    val f = i - k;
    return (1 - f)*x[k] + f*x[k+1];
}

inline public fun<T> Iterable<T>.percentiles(percentiles: DoubleArray, valueExtractor: (T) -> Double): DoubleArray {
    val sorted = map {valueExtractor (it) }.sort()
    return percentiles.map { percentile(it, sorted) }.toDoubleArray()
}

