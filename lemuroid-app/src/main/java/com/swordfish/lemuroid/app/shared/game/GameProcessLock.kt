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
        if (lock != null) return
        val lockFile = File(appContext.filesDir, LOCK_FILE_NAME)
        channel = RandomAccessFile(lockFile, "rw").channel
        lock = channel?.tryLock()
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
