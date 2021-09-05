package com.swordfish.lemuroid.app.shared.main

import android.app.Activity
import android.content.Intent
import com.swordfish.lemuroid.app.shared.game.BaseGameActivity
import com.swordfish.lemuroid.app.shared.gamecrash.GameCrashActivity
import com.swordfish.lemuroid.ext.feature.review.ReviewManager
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.dao.updateAsync
import com.swordfish.lemuroid.lib.library.db.entity.Game
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class PostGameHandler(
    private val reviewManager: ReviewManager,
    private val retrogradeDb: RetrogradeDatabase
) {

    fun handle(enableRatingFlow: Boolean, activity: Activity, resultCode: Int, data: Intent?): Completable {
        return if (resultCode == Activity.RESULT_OK) {
            handleSuccessfulGame(activity, enableRatingFlow, data)
        } else {
            handleUnsuccessfulGame(activity, data)
        }
    }

    private fun handleUnsuccessfulGame(activity: Activity, data: Intent?): Completable {
        return Completable.fromAction {
            val message = data?.getStringExtra(BaseGameActivity.PLAY_GAME_RESULT_ERROR)
            val intent = Intent(activity, GameCrashActivity::class.java).apply {
                putExtra(GameCrashActivity.EXTRA_MESSAGE, message)
            }
            activity.startActivity(intent)
        }.subscribeOn(AndroidSchedulers.mainThread())
    }

    private fun handleSuccessfulGame(
        activity: Activity,
        enableRatingFlow: Boolean,
        data: Intent?
    ): Completable {
        val duration = data?.extras?.getLong(BaseGameActivity.PLAY_GAME_RESULT_SESSION_DURATION) ?: 0L
        val game = data?.extras?.getSerializable(BaseGameActivity.PLAY_GAME_RESULT_GAME) as Game

        return Single.just(game)
            .flatMapCompletable {
                val comps = mutableListOf<Completable>().apply {
                    add(updateGamePlayedTimestamp(it))

                    if (enableRatingFlow) {
                        add(displayReviewRequest(activity, duration))
                    }
                }
                Completable.concat(comps)
            }
            .subscribeOn(Schedulers.io())
    }

    private fun displayReviewRequest(activity: Activity, durationMillis: Long): Completable {
        return Completable.timer(500, TimeUnit.MILLISECONDS)
            .andThen { reviewManager.startReviewFlow(activity, durationMillis) }
    }

    private fun updateGamePlayedTimestamp(game: Game): Completable {
        return retrogradeDb.gameDao()
            .updateAsync(game.copy(lastPlayedAt = System.currentTimeMillis()))
    }
}
