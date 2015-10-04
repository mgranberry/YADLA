package com.kludgenics.cgmlogger.app

import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Gravity
import android.view.MenuItem
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.find
import org.jetbrains.anko.info
import org.jetbrains.anko.startActivity

public abstract class BaseActivity: AppCompatActivity(), AnkoLogger {
    abstract protected val navigationId: Int

    protected fun handleNavigationBarClick(menuItem: MenuItem): Boolean {
        info("nav item clicked: ${menuItem.title}")
        return when(menuItem.itemId) {
            R.id.nav_home -> true
            R.id.nav_view -> true
            R.id.nav_places -> true
            R.id.nav_meals -> true
            R.id.nav_settings -> {
                startActivity<SettingsActivity>()
                true
            }
            else -> return false
        }
    }

    protected fun setupActionBar() {
        val toolbar = findViewById(R.id.toolbar)
        if (toolbar is Toolbar) {
            setSupportActionBar(toolbar)
            toolbar.setNavigationOnClickListener {

            }
        }
        val drawer = findViewById(R.id.drawer_layout)
        if (toolbar is Toolbar && drawer is DrawerLayout) {
            setSupportActionBar(toolbar)
            toolbar.setNavigationIcon(R.drawable.ic_assessment_white_24dp)
            toolbar.setNavigationOnClickListener {
                drawer.openDrawer(Gravity.START)
            }
        }
        val ab: ActionBar? = supportActionBar

        //ab.setHomeAsUpIndicator(R.drawable.ic_photo_camera_white_24dp)
        ab?.setDisplayHomeAsUpEnabled(navigationId != R.id.nav_home)
    }


    protected fun setupNavigationBar() {
        if (navigationId != 0) {
            val navView = find<NavigationView>(R.id.nav_view)
            navView.menu?.findItem(navigationId)?.setChecked(true)
            navView.setNavigationItemSelectedListener {
                handleNavigationBarClick(it)
            }
        }
    }
}
