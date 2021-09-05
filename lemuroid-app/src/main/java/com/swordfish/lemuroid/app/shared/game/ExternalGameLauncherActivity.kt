package com.swordfish.lemuroid.app.shared.game

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.ImmersiveActivity
import com.swordfish.lemuroid.app.shared.library.LibraryIndexMonitor
import com.swordfish.lemuroid.app.shared.main.PostGameHandler
import com.swordfish.lemuroid.app.shared.savesync.SaveSyncMonitor
import com.swordfish.lemuroid.app.tv.channel.ChannelUpdateWork
import com.swordfish.lemuroid.app.tv.shared.TVHelper
import com.swordfish.lemuroid.app.utils.android.displayErrorDialog
import com.swordfish.lemuroid.app.utils.livedata.CombinedLiveData
import com.swordfish.lemuroid.common.animationDuration
import com.swordfish.lemuroid.lib.core.CoresSelection
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.ui.setVisibleOrGone
import com.swordfish.lemuroid.lib.util.subscribeBy
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * This activity is used as an entry point when launching games from external shortcuts. This activity
 * still runs in the main process so it can peek into background job status and wait for them to
 * complete.
 */
class ExternalGameLauncherActivity : ImmersiveActivity() {

    @Inject lateinit var retrogradeDatabase: RetrogradeDatabase
    @Inject lateinit var postGameHandler: PostGameHandler
    @Inject lateinit var coresSelection: CoresSelection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_loading)
        if (savedInstanceState == null) {

            val gameId = intent.data?.pathSegments?.let { it[it.size - 1].toInt() }!!

            val publisher = LiveDataReactiveStreams.toPublisher(this, getLoadingLiveData())

            val loadingSubject = BehaviorSubject.createDefault(true)

            Observable.fromPublisher(publisher)
                .filter { !it }
                .firstElement()
                .flatMapSingle {
                    retrogradeDatabase.gameDao()
                        .selectById(gameId).subscribeOn(Schedulers.io())
                        .flatMapSingle { game ->
                            coresSelection.getCoreConfigForSystem(GameSystem.findById(game.systemId))
                                .map { game to it }
                        }
                }
                .subscribeOn(Schedulers.io())
                .delay(animationDuration().toLong(), TimeUnit.MILLISECONDS)
                .doOnSubscribe { loadingSubject.onNext(true) }
                .doAfterTerminate { loadingSubject.onNext(false) }
                .observeOn(AndroidSchedulers.mainThread())
                .autoDispose(scope())
                .subscribeBy(
                    { displayErrorMessage() },
                    { (game, systemCoreConfig) ->
                        BaseGameActivity.launchGame(
                            this,
                            systemCoreConfig,
                            game,
                            true,
                            TVHelper.isTV(applicationContext)
                        )
                    }
                )

            loadingSubject
                .debounce(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .autoDispose(scope())
                .subscribeBy {
                    findViewById<View>(R.id.progressBar).setVisibleOrGone(it)
                }
        }
    }

    private fun displayErrorMessage() {
        displayErrorDialog(R.string.game_loader_error_load_game, R.string.ok) { finish() }
    }

    private fun getLoadingLiveData(): LiveData<Boolean> {
        return CombinedLiveData(
            LibraryIndexMonitor(applicationContext).getLiveData(),
            SaveSyncMonitor(applicationContext).getLiveData()
        ) { libraryIndex, saveSync ->
            libraryIndex || saveSync
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            BaseGameActivity.REQUEST_PLAY_GAME -> {
                val isLeanback = data?.extras?.getBoolean(BaseGameActivity.PLAY_GAME_RESULT_LEANBACK) == true

                val updateChannelCallback = if (isLeanback) {
                    Completable.fromCallable { ChannelUpdateWork.enqueue(applicationContext) }
                } else {
                    Completable.complete()
                }

                postGameHandler.handle(false, this, resultCode, data)
                    .andThen { updateChannelCallback }
                    .doAfterTerminate { finish() }
                    .subscribeBy(Timber::e) { }
            }
        }
    }
}
