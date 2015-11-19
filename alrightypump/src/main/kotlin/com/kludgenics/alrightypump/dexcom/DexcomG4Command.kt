package com.kludgenics.alrightypump.dexcom

import okio.Buffer

public open class DexcomG4Command(override public val command: Int,
                                  public val requestPayload: DexcomCommand) : DexcomG4Frame {

    override val header: Buffer
        get() {
            val buff = Buffer()
            buff.writeByte(sync)
            buff.writeShortLe((headerLength + payloadLength + footerLength).toInt())
            buff.writeByte(command)
            return buff
        }

    override val payload: Buffer
        get() = requestPayload.payload.clone()

    override val footer: Buffer
        get() {
            val buff = Buffer()
            buff.writeShortLe(calculatedChecksum)
            return buff
        }

    override val calculatedChecksum: Int by lazy {
        super.calculatedChecksum
    }

    override val expectedChecksum: Int
        get() = calculatedChecksum

}
