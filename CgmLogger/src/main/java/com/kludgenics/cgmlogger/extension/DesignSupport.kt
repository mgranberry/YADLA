package com.kludgenics.cgmlogger.extension

import android.content.Context
import android.support.design.widget.*
import android.view.View
import android.view.ViewManager
import org.jetbrains.anko.__dslAddView

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

fun View.snackbar(text: CharSequence, duration: Int = Snackbar.LENGTH_SHORT, init: Snackbar.() -> Unit = {}) = {
    val snackbar = Snackbar.make(this, text, duration)
    snackbar.init()
    snackbar.show()
}

fun View.snackbar(text: Int, duration: Int = Snackbar.LENGTH_SHORT, init: Snackbar.() -> Unit = {}) = {
    val snackbar = Snackbar.make(this, text, duration)
    snackbar.init()
    snackbar.show()
}

