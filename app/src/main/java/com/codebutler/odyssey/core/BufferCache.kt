package com.codebutler.odyssey.core

class BufferCache {

    private var buffer: ByteArray? = null

    fun getBuffer(size: Int): ByteArray {
        var buffer = buffer
        if (buffer == null || buffer.size != size) {
            buffer = ByteArray(size)
            this@BufferCache.buffer = buffer
        }
        return buffer
    }
}
