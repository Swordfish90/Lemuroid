package com.swordfish.lemuroid.app.shared.game

import android.content.Context
import java.io.File
import java.io.RandomAccessFile
import java.nio.channels.FileChannel
import java.nio.channels.FileLock

object GameProcessLock {
    private const val LOCK_FILE_NAME = "game_process.lock"

    @Volatile
    private var channel: FileChannel? = null

    @Volatile
    private var lock: FileLock? = null

    fun acquire(appContext: Context) {
        if (lock?.isValid == true) return

        release()
        val lockFile = File(appContext.filesDir, LOCK_FILE_NAME)
        val newChannel = RandomAccessFile(lockFile, "rw").channel
        val newLock = runCatching { newChannel.tryLock() }.getOrNull()

        if (newLock == null) {
            newChannel.close()
        } else {
            channel = newChannel
            lock = newLock
        }
    }

    fun release() {
        runCatching { lock?.release() }
        runCatching { channel?.close() }
        lock = null
        channel = null
    }

    fun isHeldByAnotherProcess(appContext: Context): Boolean {
        val lockFile = File(appContext.filesDir, LOCK_FILE_NAME)
        RandomAccessFile(lockFile, "rw").channel.use { ch ->
            val testLock = ch.tryLock()
            return if (testLock != null) {
                testLock.release()
                false
            } else {
                true
            }
        }
    }
}
