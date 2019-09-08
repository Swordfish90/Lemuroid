package com.codebutler.retrograde.lib.game

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.codebutler.retrograde.lib.injection.AndroidWorkerInjection
import com.codebutler.retrograde.lib.injection.WorkerKey
import com.codebutler.retrograde.lib.library.GameLibrary
import dagger.Binds
import dagger.android.AndroidInjector
import dagger.multibindings.IntoMap
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class GameSaveWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    @Inject lateinit var gameLibrary: GameLibrary
    @Inject lateinit var gameLoader: GameLoader

    override fun doWork(): Result {
        AndroidWorkerInjection.inject(this)

        val gameId = inputData.getInt(DATA_GAME_ID, -1)
        val saveFile = File(inputData.getString(DATA_SAVE_FILE))

        val game = gameLoader.loadGame(gameId).blockingGet()
        val saveData = saveFile.readBytes()

        return try {
            gameLibrary.setGameSave(game, saveData)
                    .blockingAwait()
            saveFile.delete()
            Result.SUCCESS
        } catch (ex: Exception) {
            if (this.runAttemptCount < MAX_RETRIES) {
                Timber.tag(TAG).e(ex, "Failed to save game. Attempt: %s", (this.runAttemptCount + 1))
                Result.RETRY
            } else {
                Timber.tag(TAG).e(ex, "Failed to save game, giving up")
                Result.FAILURE
            }
        }
    }

    companion object {
        private const val TAG = "GameSaveWorker"

        private const val MAX_RETRIES = 4

        private const val DATA_GAME_ID = "game_id"
        private const val DATA_SAVE_FILE = "save_file"

        fun newRequest(gameId: Int, saveFilePath: String) =
            OneTimeWorkRequestBuilder<GameSaveWorker>()
                .setInputData(workDataOf(
                    DATA_GAME_ID to gameId,
                    DATA_SAVE_FILE to saveFilePath
                ))
                .build()
    }

    @dagger.Module(subcomponents = [Subcomponent::class])
    abstract class Module {
        @Binds
        @IntoMap
        @WorkerKey(GameSaveWorker::class)
        abstract fun bindMyWorkerFactory(builder: Subcomponent.Builder): AndroidInjector.Factory<out Worker>
    }

    @dagger.Subcomponent
    interface Subcomponent : AndroidInjector<GameSaveWorker> {
        @dagger.Subcomponent.Builder
        abstract class Builder : AndroidInjector.Builder<GameSaveWorker>()
    }
}
