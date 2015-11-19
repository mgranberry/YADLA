package com.kludgenics.alrightypump

import okio.Buffer

/**
 * Created by matthias on 11/19/15.
 */
interface ResponseFrame : Frame {
    override val header: Buffer
        get() {
            val buffer = Buffer()
            frame.copyTo(buffer, headerRange.start, headerLength)
            return buffer
        }

    override val payload: Buffer
        get() {
            val buffer = Buffer()
            frame.copyTo(buffer, payloadRange.start, payloadLength)
            return buffer
        }

    override val footer: Buffer
        get() {
            val buffer = Buffer()
            frame.copyTo(buffer, footerRange.start, footerLength)
            return buffer
        }
}