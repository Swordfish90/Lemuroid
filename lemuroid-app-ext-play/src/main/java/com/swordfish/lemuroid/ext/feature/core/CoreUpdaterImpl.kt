/*
 * CoreManager.kt
 *
 * Copyright (C) 2017 Retrograde Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.swordfish.lemuroid.ext.feature.core

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import com.swordfish.lemuroid.common.files.safeDelete
import com.swordfish.lemuroid.lib.core.CoreUpdater
import com.swordfish.lemuroid.lib.library.CoreID
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import timber.log.Timber
import java.io.File
import java.util.zip.ZipInputStream

/**
 * Play-store variant of CoreUpdaterImpl.
 *
 * Previously relied on Google Play Dynamic Feature Modules (SplitInstallManager)
 * to deliver per-core native libraries.  That approach is replaced here with a
 * direct download from the official RetroArch nightly buildbot so that cores are
 * always up-to-date and independent of Play delivery infrastructure.
 *
 * URL format:
 *   https://buildbot.libretro.com/nightly/android/latest/{ABI}/{coreName}_libretro_android.so.zip
 *
 * The zip contains a single entry "{coreName}_libretro_android.so" which is
 * saved to disk as "lib{coreName}_libretro_android.so" (adding the "lib" prefix)
 * to match what GameLoader expects via CoreID.libretroFileName.
 */
class CoreUpdaterImpl(
    private val directoriesManager: DirectoriesManager,
    retrofit: Retrofit,
) : CoreUpdater {

    companion object {
        /**
         * Cache sub-directory name.  Bump this string to force all users to
         * re-download cores; the old directory is removed automatically.
         */
        private const val CORES_CACHE_DIR = "retroarch-nightly"

        private const val BUILDBOT_BASE_URL =
            "https://buildbot.libretro.com/nightly/android/latest"
    }

    private val api = retrofit.create(CoreUpdater.CoreManagerApi::class.java)

    override suspend fun downloadCores(
        context: Context,
        coreIDs: List<CoreID>,
    ) {
        val sharedPreferences =
            SharedPreferencesHelper.getSharedPreferences(context.applicationContext)

        coreIDs.asFlow()
            .onEach { retrieveAssets(it, sharedPreferences) }
            .onEach { retrieveCore(context, it) }
            .collect()
    }

    // -------------------------------------------------------------------------
    // Asset retrieval (e.g. PPSSPP shaders / system files)
    // -------------------------------------------------------------------------

    private suspend fun retrieveAssets(
        coreID: CoreID,
        sharedPreferences: SharedPreferences,
    ) {
        CoreID.getAssetManager(coreID)
            .retrieveAssetsIfNeeded(api, directoriesManager, sharedPreferences)
    }

    // -------------------------------------------------------------------------
    // Core (.so) retrieval
    // -------------------------------------------------------------------------

    private suspend fun retrieveCore(
        context: Context,
        coreID: CoreID,
    ) {
        // 1. Prefer a .so already bundled inside the APK.
        findBundledLibrary(context, coreID)?.let {
            Timber.d("Core ${coreID.coreName} found bundled at ${it.absolutePath}")
            return
        }

        // 2. Download from the RetroArch buildbot.
        downloadCoreFromBuildbot(coreID)
    }

    private suspend fun findBundledLibrary(
        context: Context,
        coreID: CoreID,
    ): File? =
        withContext(Dispatchers.IO) {
            File(context.applicationInfo.nativeLibraryDir)
                .walkBottomUp()
                .firstOrNull { it.name == coreID.libretroFileName }
        }

    private suspend fun downloadCoreFromBuildbot(coreID: CoreID) {
        val mainCoresDirectory = directoriesManager.getCoresDirectory()

        // Remove stale cache directories to free storage.
        runCatching { evictOutdatedCaches(mainCoresDirectory, CORES_CACHE_DIR) }

        val coresDirectory = File(mainCoresDirectory, CORES_CACHE_DIR).apply { mkdirs() }

        // GameLoader looks for "lib{coreName}_libretro_android.so".
        val destFile = File(coresDirectory, coreID.libretroFileName)

        if (destFile.exists()) {
            Timber.d("Core ${coreID.coreName} already cached: ${destFile.absolutePath}")
            return
        }

        val abi = selectBestAbi()
        // Buildbot zip names have no "lib" prefix.
        val zipFileName = "${coreID.coreName}_libretro_android.so.zip"
        val url = "$BUILDBOT_BASE_URL/$abi/$zipFileName"

        Timber.i("Downloading ${coreID.coreName} from RetroArch buildbot: $url")

        try {
            downloadAndExtractZip(url, destFile)
        } catch (e: Throwable) {
            destFile.safeDelete()
            Timber.e(e, "Failed to download core ${coreID.coreName}")
            throw e
        }
    }

    /**
     * Downloads a .so.zip from the buildbot, extracts the first .so entry,
     * and writes it to [destFile] (with the "lib" prefix).
     */
    private suspend fun downloadAndExtractZip(url: String, destFile: File) {
        val response = api.downloadZip(url)

        if (!response.isSuccessful) {
            val msg = response.errorBody()?.string() ?: "HTTP ${response.code()}"
            throw Exception("Buildbot download failed: $msg")
        }

        val zip: ZipInputStream = response.body()
            ?: throw Exception("Empty response body for $url")

        withContext(Dispatchers.IO) {
            zip.use { zis ->
                while (true) {
                    val entry = zis.nextEntry ?: break
                    if (!entry.isDirectory && entry.name.endsWith(".so")) {
                        Timber.d("Extracting '${entry.name}' -> '${destFile.name}'")
                        destFile.outputStream().use { out -> zis.copyTo(out) }
                        break
                    }
                    zis.closeEntry()
                }
            }
        }

        check(destFile.exists() && destFile.length() > 0L) {
            "Extracted file is missing or empty for ${destFile.name}"
        }

        Timber.i("Core saved: ${destFile.absolutePath} (${destFile.length()} bytes)")
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Selects the best ABI for the buildbot URL.
     * RetroArch buildbot directory names match Android ABI names exactly.
     */
    private fun selectBestAbi(): String {
        val supported = Build.SUPPORTED_ABIS.toList()
        val preferenceOrder = listOf("arm64-v8a", "x86_64", "armeabi-v7a", "x86")
        return preferenceOrder.firstOrNull { it in supported }
            ?: supported.first()
    }

    /** Removes every cores sub-directory that is not [currentCacheDir]. */
    private fun evictOutdatedCaches(mainCoresDirectory: File, currentCacheDir: String) {
        mainCoresDirectory.listFiles()
            ?.filter { it.isDirectory && it.name != currentCacheDir }
            ?.forEach {
                Timber.d("Evicting outdated cores cache: ${it.absolutePath}")
                it.deleteRecursively()
            }
    }
}
