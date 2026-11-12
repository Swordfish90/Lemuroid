package come.asg.ominoy.lib.saves.migrators

import come.asg.ominoy.lib.library.CoreID
import come.asg.ominoy.lib.library.SystemCoreConfig
import come.asg.ominoy.lib.library.db.entity.Game
import come.asg.ominoy.lib.storage.DirectoriesManager

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
