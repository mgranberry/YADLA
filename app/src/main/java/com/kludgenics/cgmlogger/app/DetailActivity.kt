package com.kludgenics.cgmlogger.app

import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import com.google.android.gms.location.DetectedActivity
import com.kludgenics.cgmlogger.app.service.LocationIntentService
import com.kludgenics.cgmlogger.app.view.AgpChartView
import com.kludgenics.cgmlogger.extension.*
import com.kludgenics.cgmlogger.model.activity.PlayServicesActivity
import com.kludgenics.cgmlogger.model.glucose.BloodGlucoseRecord
import com.kludgenics.cgmlogger.util.FileUtil
import com.kludgenics.cgmlogger.app.R
import com.kludgenics.cgmlogger.app.service.TaskService
import com.kludgenics.cgmlogger.model.math.agp.*
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

public class DetailActivity : BaseActivity(), AnkoLogger {
    override protected val navigationId = R.id.nav_home

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        //setupNavigationBar()
        setupActionBar()
        val agp = find<AgpChartView>(R.id.backdropAgp)
        with(agp) {
            val cachedAgp = AgpUtil.getLatestCached(ctx, Period.days(intent.getIntExtra("days", 1)), updated = {
                try {
                    val newAgp = it.get()
                    val inner = newAgp.inner
                    val median = newAgp.median
                    val outer = newAgp.outer
                    onUiThread {
                        agp.innerPathString = inner
                        agp.medianPathString = median
                        agp.outerPathString = outer
                        agp.invalidate()
                    }
                } catch (e: Exception) {
                }
            })
            innerPathString = cachedAgp.inner
            medianPathString = cachedAgp.median
            outerPathString = cachedAgp.outer
            highLine = 180
            targetLine = 110
            lowLine = 80
            agp.invalidate()
        }

        val recycler = find<RecyclerView>(R.id.recycler)
        //recycler.setAdapter(AgpAdapter(listOf(1,3,7,14,30,60,90).map{Period.days(it)}))
        //recycler.setAdapter(AgpAdapter((1 .. 90).map{Period.days(it)}))
        recycler.adapter = TrendlineAdapter((0..intent.getIntExtra("days", 0)).map{(DateTime().minus(Period.days(it))).withTimeAtStartOfDay() to Period.days(1)})
        recycler.layoutManager = LinearLayoutManager(ctx)
    }



    override fun onStart() {
        super.onStart()
        TaskService.syncNow(this)
    }

    override fun onStop() {
        super.onStop()
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
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item!!.itemId

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

}
