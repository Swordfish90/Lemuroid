package com.swordfish.lemuroid.lib.core.assetsmanager

import com.swordfish.lemuroid.lib.core.CoreManager
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import io.reactivex.Completable

class NoAssetsManager : CoreManager.AssetsManager {

    override fun clearAssets(directoriesManager: DirectoriesManager) = Completable.complete()

    override fun retrieveAssetsIfNeeded(
        coreManagerApi: CoreManager.CoreManagerApi,
        directoriesManager: DirectoriesManager
    ): Completable {
        return Completable.complete()
    }
}
