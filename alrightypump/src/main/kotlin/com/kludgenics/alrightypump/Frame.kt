package com.kludgenics.alrightypump

import okio.ByteString

/**
 * Created by matthias on 11/4/15.
 */
interface Frame {
    companion object {
        val TYPE_BYTE = 0
        val TYPE_UBYTE = 1
        val TYPE_SHORT = 2
        val TYPE_USHORT = 3
        val TYPE_INT = 4
        val TYPE_UINT = 5
        val TYPE_LONG = 6
        val TYPE_ULONG = 7
    }
    val sync: Byte
    val frameLength: Int

    val valid: Boolean
}