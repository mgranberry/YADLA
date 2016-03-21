package com.kludgenics.cgmlogger.app

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import org.jetbrains.anko.AnkoLogger

public class MainActivity :  AppCompatActivity(), AnkoLogger {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    companion object {
        const val TWITTER_KEY = "XpH1SOqMSaH3v8P7A9e0RFBHm";
        const val TWITTER_SECRET = "BYYqmgAxxSyzfxbkYomajXZNvthMmvMLrdhhOChwHiUqGtln94";
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }



    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
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
