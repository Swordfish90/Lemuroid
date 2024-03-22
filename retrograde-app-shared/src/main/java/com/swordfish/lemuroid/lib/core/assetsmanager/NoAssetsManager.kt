package com.swordfish.lemuroid.lib.core.assetsmanager

import android.content.SharedPreferences
import com.swordfish.lemuroid.lib.core.CoreUpdater
import com.swordfish.lemuroid.lib.library.CoreID
import com.swordfish.lemuroid.lib.storage.DirectoriesManager

class NoAssetsManager : CoreID.AssetsManager {
    override suspend fun clearAssets(directoriesManager: DirectoriesManager) {}

    override suspend fun retrieveAssetsIfNeeded(
        coreUpdaterApi: CoreUpdater.CoreManagerApi,
        directoriesManager: DirectoriesManager,
        sharedPreferences: SharedPreferences,
    ) {
    }
}
