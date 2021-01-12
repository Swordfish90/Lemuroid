package com.swordfish.lemuroid.lib.savesync

import android.app.Activity
import com.swordfish.lemuroid.lib.library.CoreID
import io.reactivex.Completable

interface SaveSyncManager {
    fun getProvider(): String
    fun getSettingsActivity(): Class<out Activity>?
    fun isSupported(): Boolean
    fun isConfigured(): Boolean
    fun getLastSyncInfo(): String
    fun getConfigInfo(): String
    fun sync(cores: Set<CoreID>): Completable
    fun computeSavesSpace(): String
    fun computeStatesSpace(core: CoreID): String
}
