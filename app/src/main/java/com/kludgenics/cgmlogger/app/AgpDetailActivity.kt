package com.kludgenics.cgmlogger.app
import android.os.Bundle
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import org.jetbrains.anko.AnkoLogger

/**
 * Created by matthiasgranberry on 5/31/15.
 */
public class AgpDetailActivity : BaseActivity(), AnkoLogger {
    override val navigationId: Int
        get() = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        Answers.getInstance().logCustom(CustomEvent("AgpDetailActivity"))
    }
}
