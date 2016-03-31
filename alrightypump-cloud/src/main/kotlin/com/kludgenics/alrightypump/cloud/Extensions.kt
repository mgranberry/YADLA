package com.kludgenics.alrightypump.cloud

import java.util.*

/**
 * Created by matthias on 3/30/16.
 */
fun <T> Sequence<T>.chunked(size: Int): Sequence<List<T>> {
    val iterator = this.iterator()

    return object : Sequence<List<T>> {
        override fun iterator() = object : AbstractIterator<List<T>>() {
            override fun computeNext() {
                val next = ArrayList<T>(size)
                while (iterator.hasNext() && next.size < size) {
                    next.add(iterator.next())
                }
                if (next.isEmpty()) done() else setNext(next)
            }
        }
    }
}

fun <T> Sequence<T>.mutableChunked(size: Int): Sequence<MutableList<T>> {
    val iterator = this.iterator()

    return object : Sequence<MutableList<T>> {
        override fun iterator() = object : AbstractIterator<MutableList<T>>() {
            override fun computeNext() {
                val next = ArrayList<T>(size)
                while (iterator.hasNext() && next.size < size) {
                    next.add(iterator.next())
                }
                if (next.isEmpty()) done() else setNext(next)
            }
        }
    }
}