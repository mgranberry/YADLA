package com.kludgenics.cgmlogger.extension

import android.app.Activity
import android.content.Context
import android.support.design.widget.*
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.CardView
import android.view.View
import android.view.ViewManager
import org.jetbrains.anko.__dslAddView
import java.util.concurrent.atomic.AtomicInteger

fun ViewManager.cardView(init: CardView.() -> Unit = {}) =
        __dslAddView({ CardView(it) }, init, this)

fun ViewManager.appBarLayout(init: AppBarLayout.() -> Unit = {}) =
        __dslAddView({ AppBarLayout(it) }, init, this)

fun ViewManager.collapsingToolbarLayout(init: CollapsingToolbarLayout.() -> Unit = {}) =
        __dslAddView({ CollapsingToolbarLayout(it) }, init, this)

fun ViewManager.coordinatorLayout(init: CoordinatorLayout.() -> Unit = {}) =
        __dslAddView({ CoordinatorLayout(it) }, init, this)

fun ViewManager.floatingActionButton(init: FloatingActionButton.() -> Unit = {}) =
        __dslAddView({ FloatingActionButton(it) }, init, this)

fun ViewManager.navigationView(init: NavigationView.() -> Unit = {}) =
        __dslAddView({ NavigationView(it) }, init, this)

fun ViewManager.tabLayout(init: TabLayout.() -> Unit = {}) =
        __dslAddView({ TabLayout(it) }, init, this)

fun ViewManager.textInputLayout(init: TextInputLayout.() -> Unit = {}) =
        __dslAddView({ TextInputLayout(it) }, init, this)

fun View.snackbar(text: CharSequence, duration: Int = Snackbar.LENGTH_SHORT, init: Snackbar.() -> Unit = {}): Snackbar {
    val snack = Snackbar.make(this, text, duration)
    snack.init()
    snack.show()
    return snack
}

fun View.snackbar(text: Int, duration: Int = Snackbar.LENGTH_SHORT, init: Snackbar.() -> Unit = {}): Snackbar {
    val snack = Snackbar.make(this, text, duration)
    snack.init()
    snack.show()
    return snack
}

private object ViewCounter {
    private var viewCounter = AtomicInteger(1)
    public fun generateViewId(): Int {
        while (true) {
            val result = viewCounter.get()
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            var newValue = result + 1
            if (newValue > 16777215) newValue = 1 // Roll over to 1, not 0.
            if (viewCounter.compareAndSet(result, newValue)) {
                return result
            }
        }
    }
}

fun View.generateViewIdCompat(): Int {
    if (android.os.Build.VERSION.SDK_INT >= 19)
        return View.generateViewId()
    else
        return ViewCounter.generateViewId()
}
