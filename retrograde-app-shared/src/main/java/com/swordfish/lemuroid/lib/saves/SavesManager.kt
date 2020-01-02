package com.swordfish.lemuroid.lib.saves

import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import io.reactivex.Completable
import io.reactivex.Maybe
import java.io.File

class SavesManager(private val directoriesManager: DirectoriesManager) {

    fun getStashedState(game: Game): Maybe<ByteArray> {
        val saveFile = getStashedFile(game)
        return if (saveFile.exists()) {
            Maybe.just(saveFile.readBytes())
        } else {
            Maybe.empty()
        }
    }

    fun setSashedState(game: Game, data: ByteArray) = Completable.fromCallable {
        val saveFile = getStashedFile(game)
        saveFile.writeBytes(data)
    }

    private fun getStashedFile(game: Game): File {
        val statesDirectories = directoriesManager.getStatesDirectory()
        return File(statesDirectories, "${game.fileName}.state")
    }
}
