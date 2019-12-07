package com.swordfish.lemuroid.lib.storage

import android.content.Context
import java.io.File

class DirectoriesManager(private val appContext: Context) {

    fun getStatesDirectory() = File(appContext.filesDir, "states").apply {
        mkdirs()
    }

    fun getCoresDirectory() = File(appContext.filesDir, "cores").apply {
        mkdirs()
    }

    fun getSystemDirectory() = File(appContext.filesDir, "system").apply {
        mkdirs()
    }

    fun getSavesDirectory() = File(appContext.filesDir, "saves").apply {
        mkdirs()
    }
}
