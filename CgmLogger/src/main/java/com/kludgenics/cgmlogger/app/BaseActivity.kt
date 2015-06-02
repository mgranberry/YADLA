package com.kludgenics.cgmlogger.app

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import org.jetbrains.anko.*
import org.jetbrains.anko.internals.noBinding

public abstract class BaseActivity: AppCompatActivity(), AnkoLogger {
    abstract protected val navigationId: Int

    protected fun handleNavigationBarClick(menuItem: MenuItem): Boolean {
        info("nav item clicked: ${menuItem.getTitle()}")
        return when(menuItem.getItemId()) {
            R.id.nav_home -> true
            R.id.nav_view -> true
            R.id.nav_places -> true
            R.id.nav_meals -> true
            else ->
                    return false
        }
    }

    protected fun setupNavigationBar() {
        if (navigationId != 0) {
            val navView = find<NavigationView>(R.id.nav_view)
            navView.getMenu()?.findItem(navigationId)?.setChecked(true)
        }
    }
}
