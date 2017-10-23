package com.codebutler.odyssey.app.feature.home

import android.support.v17.leanback.app.BaseSupportFragment
import android.util.Log
import com.codebutler.odyssey.R
import com.codebutler.odyssey.app.feature.common.SimpleErrorFragment
import com.codebutler.odyssey.app.feature.game.GameActivity
import com.codebutler.odyssey.lib.core.CoreManager
import com.codebutler.odyssey.lib.library.GameSystem
import com.codebutler.odyssey.lib.library.db.OdysseyDatabase
import com.codebutler.odyssey.lib.library.db.entity.Game
import com.codebutler.odyssey.lib.library.provider.GameLibraryProviderRegistry
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.kotlin.autoDisposeWith
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import java.io.File

class GameLauncher(
        private val libraryProviderRegistry: GameLibraryProviderRegistry,
        private val coreManager: CoreManager,
        private val odysseyDb: OdysseyDatabase) {

    companion object {
        private const val TAG = "GameLauncher"
    }

    fun launchGame(fragment: BaseSupportFragment, game: Game) {
        fragment.progressBarManager.show()

        val gameSystem = GameSystem.findById(game.systemId)!!
        val provider = libraryProviderRegistry.getProvider(game)

        val gameObservable = provider.getGameRom(game).toObservable()
        val coreObservable = coreManager.downloadCore(gameSystem.coreFileName).toObservable()
        Observable.combineLatest(
                gameObservable,
                coreObservable,
                BiFunction<File, File, Pair<File, File>> { gameResponse, coreResponse ->
                    gameResponse to coreResponse
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .autoDisposeWith(AndroidLifecycleScopeProvider.from(fragment))
                .subscribe(
                        { (gameFile, coreFile) ->
                            fragment.progressBarManager.hide()

                            Completable.fromCallable {
                                odysseyDb.gameDao().update(game.copy(lastPlayedAt = System.currentTimeMillis()))
                            }
                                    .subscribeOn(Schedulers.io())
                                    .subscribe()

                            fragment.startActivity(GameActivity.newIntent(
                                    context = fragment.context,
                                    coreFilePath = coreFile.absolutePath,
                                    gameFilePath = gameFile.absolutePath))
                        },
                        { error ->
                            Log.e(TAG, "Download failed", error)
                            fragment.progressBarManager.hide()
                            val errorFragment = SimpleErrorFragment.create(error.toString())
                            fragment.fragmentManager.beginTransaction()
                                    .replace(R.id.content, errorFragment)
                                    .addToBackStack(null)
                                    .commit()
                        })
    }
}
