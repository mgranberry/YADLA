package com.kludgenics.cgmlogger.app

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.ContentViewEvent
import com.crashlytics.android.answers.CustomEvent
import com.kludgenics.cgmlogger.app.adapter.TrendlineAdapter
import com.kludgenics.cgmlogger.app.service.TaskService
import com.kludgenics.cgmlogger.app.util.PathParser
import com.kludgenics.cgmlogger.app.view.AgpChartView
import com.kludgenics.cgmlogger.model.flatbuffers.path.AgpPathBuffer
import com.kludgenics.cgmlogger.model.flatbuffers.path.PathDataBuffer
import com.kludgenics.cgmlogger.model.math.agp.AgpUtil
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.ctx
import org.jetbrains.anko.find
import org.jetbrains.anko.onUiThread
import org.joda.time.DateTime
import org.joda.time.Period
import java.nio.ByteBuffer

public class DetailActivity : BaseActivity(), AnkoLogger {
    override protected val navigationId = R.id.nav_home
    private val period by lazy { Period.days(intent.getIntExtra("days", 1)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        //setupNavigationBar()
        setupActionBar()
        val agp = find<AgpChartView>(R.id.backdropAgp)
        with(agp) {
            val cachedAgp = AgpUtil.getLatestCached(ctx, period,
                    updated = {
                        try {
                            val newAgp = it.get()

                            val inner = PathParser.copyFromPathDataBuffer(newAgp.inner)
                            val outer = PathParser.copyFromPathDataBuffer(newAgp.outer)
                            val median = PathParser.copyFromPathDataBuffer(newAgp.median)
                            onUiThread {
                                agp.innerPathData = inner
                                agp.medianPathData = median
                                agp.outerPathData = outer
                                agp.invalidate()
                            }
                        } catch (e: Exception) {
                        }
                    })
            val inner = PathParser.copyFromPathDataBuffer(cachedAgp.inner)
            val outer = PathParser.copyFromPathDataBuffer(cachedAgp.outer)
            val median = PathParser.copyFromPathDataBuffer(cachedAgp.median)

            innerPathData = inner
            medianPathData = median
            outerPathData = outer
            highLine = 180
            targetLine = 110
            lowLine = 80
            agp.invalidate()
        }

        val recycler = find<RecyclerView>(R.id.recycler)
        recycler.adapter = TrendlineAdapter((0..period.days)
                .map { (DateTime().minus(Period.days(it))).withTimeAtStartOfDay() to Period.days(1) })
        recycler.layoutManager = LinearLayoutManager(ctx)
    }



    override fun onStart() {
        super.onStart()
        Answers.getInstance().logContentView(ContentViewEvent()
                .putContentId("detail-$period")
                .putContentName("Detail")
                .putContentType(period.toString()))
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
