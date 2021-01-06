package com.swordfish.lemuroid.ext.feature.savesync

import android.content.Context
import com.swordfish.lemuroid.lib.library.CoreID
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import io.reactivex.Completable

class SaveSyncManager(
    private val appContext: Context,
    private val directoriesManager: DirectoriesManager
) {
    fun getProvider(): String = ""

    fun getSettingsActivity() = null

    fun isSupported(): Boolean = false

    fun isConfigured(): Boolean = false

    fun getLastSyncInfo(): String = ""

    fun getConfigInfo(): String = ""

    fun sync(cores: Set<CoreID>) = Completable.complete()

    fun computeSavesSpace() = ""

    fun computeStatesSpace(coreID: CoreID) = ""
}
