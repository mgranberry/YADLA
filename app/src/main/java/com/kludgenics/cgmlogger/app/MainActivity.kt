package com.kludgenics.cgmlogger.app

import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.android.gms.location.DetectedActivity
import com.kludgenics.cgmlogger.app.service.LocationIntentService
import com.kludgenics.cgmlogger.app.view.AgpChartView
import com.kludgenics.cgmlogger.extension.*
import com.kludgenics.cgmlogger.model.activity.PlayServicesActivity
import com.kludgenics.cgmlogger.model.glucose.BloodGlucoseRecord
import com.kludgenics.cgmlogger.model.math.agp.AgpUtil
import com.kludgenics.cgmlogger.model.math.agp.CachedDatePeriodAgp
import com.kludgenics.cgmlogger.model.math.agp.svg
import com.kludgenics.cgmlogger.model.math.agp.DailyAgp
import com.kludgenics.cgmlogger.util.FileUtil
import com.kludgenics.cgmlogger.app.R
import com.kludgenics.cgmlogger.app.service.TaskService
import com.kludgenics.cgmlogger.model.math.bgi.Bgi
import com.kludgenics.cgmlogger.model.math.bgi.BgiUtil
import com.kludgenics.cgmlogger.model.math.bgi.svg
import io.realm.Realm
import org.jetbrains.anko.*
import org.joda.time.DateTime
import org.joda.time.LocalTime
import org.joda.time.Partial
import org.joda.time.Period
import java.io.File
import java.util.*
import kotlin.properties.Delegates

public class MainActivity : BaseActivity(), AnkoLogger {
    override protected val navigationId = R.id.nav_home

    override fun onCreate(savedInstanceState: Bundle?) {
        super<BaseActivity>.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupNavigationBar()
        setupActionBar()

        val fab = find<FloatingActionButton>(R.id.fab)
        fab.onClick {
        }
        startService(intentFor<LocationIntentService>().setAction(LocationIntentService.ACTION_START_LOCATION_UPDATES))
        /// / Set up the drawer.

        val recycler = find<RecyclerView>(R.id.recycler)
        //recycler.setAdapter(AgpAdapter(listOf(1,3,7,14,30,60,90).map{Period.days(it)}))
        recycler.setAdapter(AgpAdapter((1 .. 90).map{Period.days(it)}))
        //recycler.setAdapter(TrendlineAdapter((0..120).map{(DateTime() - Period.days(it)).withTimeAtStartOfDay() to Period.days(1)}))
        recycler.setLayoutManager(LinearLayoutManager(ctx))
    }



    override fun onStart() {
        super<BaseActivity>.onStart()
        TaskService.syncNow(this)
    }

    override fun onStop() {
        super<BaseActivity>.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        /*if (!mNavigationDrawerFragment!!.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu)
            restoreActionBar()
            return true
        }*/
        return super<BaseActivity>.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item!!.getItemId()

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true
        }

        return super<BaseActivity>.onOptionsItemSelected(item)
    }

}
