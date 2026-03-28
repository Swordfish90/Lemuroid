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
 * Downloads libretro cores from the official RetroArch nightly buildbot.
 *
 * URL format:
 *   https://buildbot.libretro.com/nightly/android/latest/{ABI}/{coreName}_libretro_android.so.zip
 *
 * Each zip contains a single .so file named "{coreName}_libretro_android.so".
 * After extraction it is stored as "lib{coreName}_libretro_android.so" so that
 * GameLoader can find it via CoreID.libretroFileName.
 */
class CoreUpdaterImpl(
    private val directoriesManager: DirectoriesManager,
    retrofit: Retrofit,
) : CoreUpdater {

    companion object {
        /**
         * Sub-directory name inside the cores cache folder.
         * Change this string to force a fresh download of all cores;
         * old directories are cleaned up automatically.
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
        // 1. Prefer a .so already bundled inside the APK / split APK.
        findBundledLibrary(context, coreID)?.let {
            Timber.d("Core ${coreID.coreName} found bundled at ${it.absolutePath}")
            return
        }

        // 2. Otherwise download from the official RetroArch buildbot.
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

        // Lemuroid expects the file named "lib{coreName}_libretro_android.so".
        val destFile = File(coresDirectory, coreID.libretroFileName)

        if (destFile.exists()) {
            Timber.d("Core ${coreID.coreName} already cached: ${destFile.absolutePath}")
            return
        }

        val abi = selectBestAbi()
        // Buildbot archives are named "{coreName}_libretro_android.so.zip" (no "lib" prefix).
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
     * Downloads a .so.zip from the buildbot, extracts the first .so entry inside,
     * and writes it to [destFile].
     *
     * The stored filename uses the "lib" prefix ([CoreID.libretroFileName]) regardless
     * of what is inside the zip, because that is what Lemuroid's GameLoader looks for.
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
     * Returns the best ABI string understood by the RetroArch buildbot.
     * Buildbot directory names match Android ABI names exactly:
     * arm64-v8a, armeabi-v7a, x86_64, x86.
     */
    private fun selectBestAbi(): String {
        val supported = Build.SUPPORTED_ABIS.toList()
        val preferenceOrder = listOf("arm64-v8a", "x86_64", "armeabi-v7a", "x86")
        return preferenceOrder.firstOrNull { it in supported }
            ?: supported.first()
    }

    /** Deletes every cores sub-directory that is not [currentCacheDir]. */
    private fun evictOutdatedCaches(mainCoresDirectory: File, currentCacheDir: String) {
        mainCoresDirectory.listFiles()
            ?.filter { it.isDirectory && it.name != currentCacheDir }
            ?.forEach {
                Timber.d("Evicting outdated cores cache: ${it.absolutePath}")
                it.deleteRecursively()
            }
    }
}
