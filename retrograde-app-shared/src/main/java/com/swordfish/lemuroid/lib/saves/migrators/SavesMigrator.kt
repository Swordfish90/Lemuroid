package com.swordfish.lemuroid.lib.saves.migrators

import com.swordfish.lemuroid.lib.library.CoreID
import com.swordfish.lemuroid.lib.library.SystemCoreConfig
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.storage.DirectoriesManager

interface SavesMigrator {
    fun loadPreviousSaveForGame(
        game: Game,
        directoriesManager: DirectoriesManager,
    ): ByteArray?
}

fun SystemCoreConfig.getSavesMigrator(): SavesMigrator? {
    return when (this.coreID) {
        CoreID.MELONDS -> MelonDsSavesMigrator
        else -> null
    }
}
