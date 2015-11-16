package com.kludgenics.alrightypump

import okio.Buffer


class InvalidPacketException(message: String):
        RuntimeException(message)