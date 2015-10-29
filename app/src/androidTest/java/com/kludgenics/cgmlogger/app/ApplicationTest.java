package com.kludgenics.cgmlogger.app;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.google.flatbuffers.FlatBufferBuilder;
import com.kludgenics.cgmlogger.app.util.PathParser;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

}