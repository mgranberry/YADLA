package com.kludgenics.cgmlogger.util

import rx.*
import rx.functions.Action0
import rx.functions.Action1
import rx.subjects.PublishSubject
import rx.subjects.Subject

/**
 * Created by matthiasgranberry on 5/26/15.
 */

public object EventBus {
    private val subject: PublishSubject<Any> = PublishSubject.create()
    public fun get(): PublishSubject<Any> { return subject }
}