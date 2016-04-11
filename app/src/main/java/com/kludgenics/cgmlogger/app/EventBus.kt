package com.kludgenics.cgmlogger.app

import com.squareup.otto.Bus
import com.squareup.otto.ThreadEnforcer

/**
 * Created by matthias on 3/24/16.
 */
object EventBus {
    private val instance: Bus = Bus(ThreadEnforcer.ANY)
    fun register(receiver: Any) = instance.register(receiver)
    fun unregister(receiver: Any) = instance.unregister(receiver)
    fun post(event: Any) = instance.post(event)
}