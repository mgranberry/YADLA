package com.kludgenics.cgmlogger.app

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceActivity
import android.preference.PreferenceFragment
import com.kludgenics.cgmlogger.app.service.TaskService
import org.jetbrains.anko.*

/**
 * Created by matthiasgranberry on 6/2/15.
 */
public class LoggerPreferencesActivity : BaseActivity() {
    override val navigationId: Int
        get() = R.id.nav_settings

    val preferencesListener = SharedPreferences.OnSharedPreferenceChangeListener {
        prefs, key ->
        val resources = getResources()
        val nightscoutPrefs = when(key) {
            resources.getString(R.string.nightscout_enable) -> {
                prefs.getBoolean(key, false) to prefs.getString(resources.getString(R.string.nightscout_uri), "")
            }
            resources.getString(R.string.nightscout_uri) -> {
                prefs.getBoolean(resources.getString(R.string.nightscout_enable), false) to prefs.getString(key, "")
            }
            else -> null
        }
        if (nightscoutPrefs is Pair<Boolean, String> && nightscoutPrefs.first == true &&
                !nightscoutPrefs.second.isNullOrBlank()) {
            TaskService.scheduleNightscoutPeriodicTasks(this)
            TaskService.scheduleNightscoutEntriesFullSync(this)
        } else
            TaskService.cancelNightscoutTasks(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActionBar()
        setupPreferences()
        //setupNavigationBar()
    }

    fun setupPreferences() {
        val preferenceFragment = LoggerPreferenceFragment();

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                preferenceFragment).commit();

        defaultSharedPreferences.registerOnSharedPreferenceChangeListener(preferencesListener)
    }

    class LoggerPreferenceFragment: PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.prefs);
        }
    }
}
