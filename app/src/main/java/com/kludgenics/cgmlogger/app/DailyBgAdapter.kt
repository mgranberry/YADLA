package com.kludgenics.cgmlogger.app

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup

import com.kludgenics.cgmlogger.model.glucose.BloodGlucoseRecord

import io.realm.RealmResults
import kotlin.properties.Delegates

/**
 * Created by matthiasgranberry on 6/6/15.
 */
public class DailyBgAdapter : RecyclerView.Adapter<BloodGlucoseViewHolder> {
    private var results: RealmResults<BloodGlucoseRecord> by Delegates.notNull()
    public constructor() {
    }

    public constructor(results: RealmResults<BloodGlucoseRecord>) {
        this.results = results
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BloodGlucoseViewHolder? {
        return null
    }

    override fun onBindViewHolder(holder: BloodGlucoseViewHolder, position: Int) {
    }

    override fun getItemCount(): Int {
        return results
    }
}

class BloodGlucoseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)