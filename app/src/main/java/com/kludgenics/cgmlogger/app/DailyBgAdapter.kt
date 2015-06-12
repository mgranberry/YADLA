package com.kludgenics.cgmlogger.app

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.kludgenics.cgmlogger.model.glucose.BgByDay

import com.kludgenics.cgmlogger.model.glucose.BloodGlucoseRecord

import io.realm.RealmResults
import org.joda.time.DateTime
import kotlin.properties.Delegates

/**
 * Created by matthiasgranberry on 6/6/15.
 */
public class DailyBgAdapter : RecyclerView.Adapter<BloodGlucoseViewHolder> {
    private var results: RealmResults<BgByDay> by Delegates.notNull()
    private val count: Int get() = results.size()
    public constructor() {
    }

    public constructor(results: RealmResults<BgByDay>) {
        this.results = results
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BloodGlucoseViewHolder? {
        return null
    }

    override fun onBindViewHolder(holder: BloodGlucoseViewHolder, position: Int) {
    }

    override fun getItemCount(): Int {
        return count
    }
}

class BloodGlucoseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)