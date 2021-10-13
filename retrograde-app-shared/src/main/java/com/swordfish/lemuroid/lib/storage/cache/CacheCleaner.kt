package com.swordfish.lemuroid.lib.storage.cache

import android.content.Context
import android.os.Environment
import android.os.StatFs
import android.system.Os
import android.text.format.Formatter
import com.swordfish.lemuroid.common.kotlin.gigaBytes
import com.swordfish.lemuroid.common.kotlin.megaBytes
import com.swordfish.lemuroid.lib.storage.local.LocalStorageProvider
import com.swordfish.lemuroid.lib.storage.local.StorageAccessFrameworkProvider
import io.reactivex.Completable
import timber.log.Timber
import java.io.File
import kotlin.math.abs
import kotlin.math.roundToLong

object CacheCleaner {

    private val MIN_CACHE_SIZE = 256L.megaBytes()
    private val MAX_CACHE_SIZE = 16L.gigaBytes()

    fun getSupportedCacheSizes(): List<Long> {
        return generateSequence(MIN_CACHE_SIZE) { it * 2L }
            .takeWhile { it <= MAX_CACHE_SIZE }
            .toList()
    }

    fun getDefaultCacheSize(): Long {
        val defaultCacheSize = (getInternalMemorySize() * 0.05f).roundToLong()
        return getClosestCacheSize(defaultCacheSize)
    }

    private fun getClosestCacheSize(size: Long): Long {
        return getSupportedCacheSizes()
            .minByOrNull { abs(it - size) } ?: 0
    }

    private fun getInternalMemorySize(): Long {
        val path: File = Environment.getDataDirectory()
        val stat = StatFs(path.absolutePath)
        return stat.blockSizeLong * stat.blockCountLong
    }

    fun cleanAll(appContext: Context) = Completable.fromAction {
        Timber.i("Running cache cleanup everything task")
        appContext.cacheDir.listFiles()?.forEach { it.deleteRecursively() }
    }

    fun clean(appContext: Context, requestedMaxSize: Long) = Completable.fromAction {
        Timber.i("Running cache cleanup lru task")
        val maxByteSize = getClosestCacheSize(requestedMaxSize)

        val cacheFoldersSequence = sequenceOf(
            File(appContext.cacheDir, StorageAccessFrameworkProvider.SAF_CACHE_SUBFOLDER).walkBottomUp(),
            File(appContext.cacheDir, LocalStorageProvider.LOCAL_STORAGE_CACHE_SUBFOLDER).walkBottomUp()
        )

        val cacheFiles = cacheFoldersSequence.flatten()
            .filter { it.isFile }
            .sortedBy { retrieveLastAccess(it) }
            .toMutableList()

        val cacheSize = cacheFiles
            .map { it.length() }
            .sum()

        Timber.i("Space used by cache: ${printSize(appContext, cacheSize)} / ${printSize(appContext, maxByteSize)}")

        var spaceToBeDeleted = maxOf(cacheSize - maxByteSize, 0)

        Timber.i("Freeing cache space: ${printSize(appContext, spaceToBeDeleted)}")

        while (spaceToBeDeleted > 0) {
            val deletedFile = cacheFiles.removeAt(0)
            val size = deletedFile.length()

            if (deletedFile.delete()) {
                spaceToBeDeleted -= size
                Timber.i("Cache file deleted ${deletedFile.name}, size: ${printSize(appContext, size)}")
            }
        }
    }

    private fun printSize(appContext: Context, size: Long): String {
        return Formatter.formatFileSize(appContext, size)
    }

    private fun retrieveLastAccess(file: File) = Os.lstat(file.absolutePath).st_atime
}
