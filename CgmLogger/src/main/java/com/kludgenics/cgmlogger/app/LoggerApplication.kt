package com.kludgenics.cgmlogger.app

import android.app.Application
import android.app.usage.UsageEvents

import com.kludgenics.cgmlogger.util.EventBus

/**
 * Created by matthiasgranberry on 5/28/15.
 */
public class LoggerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        EventBus.getSubject().onNext("Application started")
    }
}
