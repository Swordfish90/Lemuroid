package com.swordfish.lemuroid.lib.storage

import android.content.Context
import java.io.File

class DirectoriesManager(private val appContext: Context) {

    fun getStatesDirectory(): File = File(appContext.filesDir, "states").apply {
        mkdirs()
    }

    fun getCoresDirectory(): File = File(appContext.filesDir, "cores").apply {
        mkdirs()
    }

    fun getSystemDirectory(): File = File(appContext.filesDir, "system").apply {
        mkdirs()
    }

    fun getSavesDirectory(): File = File(appContext.getExternalFilesDir(null), "saves").apply {
        mkdirs()
    }
}
