package com.kludgenics.justgivemeachart

/**
 * Created by matthias on 4/27/16.
 */
interface DataInk<T, U>: Ink {
    var valueAdapter: ValueAdapter<T, U>?
}