package com.swordfish.lemuroid.lib.storage

import android.content.Context
import java.io.File

class DirectoriesManager(private val appContext: Context) {
    @Deprecated("Use the external states directory")
    fun getInternalStatesDirectory(): File =
        File(appContext.filesDir, "states").apply {
            mkdirs()
        }

    fun getCoresDirectory(): File =
        File(appContext.filesDir, "cores").apply {
            mkdirs()
        }

    fun getSystemDirectory(): File =
        File(appContext.filesDir, "system").apply {
            mkdirs()
            installBundledPs2Resources(this)
        }

    private fun installBundledPs2Resources(systemDirectory: File) {
        val assetsPath = "ps2-resources"
        val markerName = ".armsx2-resources-version"
        val resourcesDirectory = File(systemDirectory, "pcsx2/resources")
        val marker = File(resourcesDirectory, markerName)
        val expectedVersion = "f6df1e69255544241132d66441f2f1338fd90bfd"

        if (marker.isFile && marker.readText() == expectedVersion) return

        val rootAssets = runCatching { appContext.assets.list(assetsPath) }.getOrNull()
            ?: return
        if (rootAssets.isEmpty()) return

        fun copyAssets(assetDirectory: String, destination: File) {
            if (!destination.exists()) destination.mkdirs()
            appContext.assets.list(assetDirectory).orEmpty().forEach { name ->
                val assetPath = "$assetDirectory/$name"
                val target = File(destination, name)
                val children = appContext.assets.list(assetPath).orEmpty()
                if (children.isNotEmpty()) {
                    copyAssets(assetPath, target)
                } else {
                    appContext.assets.open(assetPath).use { input ->
                        target.outputStream().use { output -> input.copyTo(output) }
                    }
                }
            }
        }

        runCatching {
            resourcesDirectory.mkdirs()
            copyAssets(assetsPath, resourcesDirectory)
            marker.writeText(expectedVersion)
        }
    }

    fun getStatesDirectory(): File =
        File(appContext.getExternalFilesDir(null), "states").apply {
            mkdirs()
        }

    fun getStatesPreviewDirectory(): File =
        File(appContext.getExternalFilesDir(null), "state-previews").apply {
            mkdirs()
        }

    fun getSavesDirectory(): File =
        File(appContext.getExternalFilesDir(null), "saves").apply {
            mkdirs()
        }

    fun getInternalRomsDirectory(): File =
        File(appContext.getExternalFilesDir(null), "roms").apply {
            mkdirs()
        }
}
