package com.kludgenics.cgmlogger.extension

import android.support.design.widget.*
import android.support.v7.widget.CardView
import android.view.View
import android.view.ViewManager
import org.jetbrains.anko.custom.ankoView
import java.util.concurrent.atomic.AtomicInteger

fun ViewManager.cardView(init: CardView.() -> Unit = {}) =
        ankoView({ CardView(it) }, init)

fun ViewManager.appBarLayout(init: AppBarLayout.() -> Unit = {}) =
        ankoView({ AppBarLayout(it) }, init)

fun ViewManager.collapsingToolbarLayout(init: CollapsingToolbarLayout.() -> Unit = {}) =
        ankoView({ CollapsingToolbarLayout(it)}, init)

fun ViewManager.coordinatorLayout(init: CoordinatorLayout.() -> Unit = {}) =
        ankoView({ CoordinatorLayout(it)}, init)

fun ViewManager.floatingActionButton(init: FloatingActionButton.() -> Unit = {}) =
        ankoView({ FloatingActionButton(it)}, init)

fun ViewManager.navigationView(init: NavigationView.() -> Unit = {}) =
        ankoView({ NavigationView(it)}, init)

fun ViewManager.tabLayout(init: TabLayout.() -> Unit = {}) =
        ankoView({ TabLayout(it)}, init)

fun ViewManager.textInputLayout(init: TextInputLayout.() -> Unit = {}) =
        ankoView({ TextInputLayout(it)}, init)

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
    fun generateViewId(): Int {
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
