package com.kludgenics.cgmlogger.model.math

import rx.Observable
import rx.Subscriber
import java.util.ArrayList
import rx.lang.kotlin.*
import kotlin.Collection.*
/**
 * Created by matthiasgranberry on 5/31/15.
 */
public class PercentileTransformer<T>(private val valueExtractor: (T) -> Double,
                                      private val percentiles: Array<Double>): Observable.Transformer<T, List<Pair<Double, Double>>> {
    private fun percentile (percentile: Double, x: List<Double>): Double {
        val n = x.size();
        val i = n * percentile/100.0 + 0.5;
        val k = i.toInt();
        val f = i - k;
        return (1 - f)*x[k] + f*x[k+1];
    }

    override fun call(t: Observable<T>): Observable<List<Pair<Double, Double>>> {
        return t.filter { it != null }
                .map { valueExtractor(it) }
                .toSortedList()
                .map { values ->
                    percentiles.map { it to percentile(it, values) }
                }
    }
}


