package com.kludgenics.alrightypump.dexcom

import com.kludgenics.alrightypump.CRC
import com.kludgenics.alrightypump.Frame

/**
 * Created by matthias on 11/18/15.
 */
interface DexcomG4Frame : Frame {
    override val sync: Int get() = 0x01
    override val headerLength: Long get() = 4L
    override val payloadLength: Long get() = payload?.size() ?: 0
    override val footerLength: Long get() = 2L
    val command: Int
    override val calculatedChecksum: Int get() {

        var checkSum = CRC.updateChecksum(header!!, header!!.size(), 0)
        val pl = payload
        return if (pl != null)
            CRC.updateChecksum(pl, pl.size(), checkSum)
        else checkSum
    }
}