package com.swordfish.lemuroid.lib.storage

import android.content.Context
import android.content.SharedPreferences
import com.fredporciuncula.flow.preferences.FlowSharedPreferences
import java.io.File

import com.swordfish.lemuroid.lib.injection.PerApp
import javax.inject.Inject

@PerApp
class DirectoriesManager @Inject constructor(
    private val appContext: Context,
    private val sharedPreferences: SharedPreferences
) {
    private val flowSharedPreferences = FlowSharedPreferences(sharedPreferences)

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

    fun getCustomSavesDirectoryUri(): String? {
        val uri = flowSharedPreferences.getString("pref_key_save_storage_folder", "").get()
        return if (uri.isEmpty()) null else uri
    }

    fun getInternalRomsDirectory(): File =
        File(appContext.getExternalFilesDir(null), "roms").apply {
            mkdirs()
        }
}
