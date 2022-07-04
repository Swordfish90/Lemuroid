package com.swordfish.lemuroid.app.shared.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.game.BaseGameActivity
import com.swordfish.lemuroid.app.shared.gamecrash.GameCrashActivity
import com.swordfish.lemuroid.app.shared.savesync.SaveSyncWork
import com.swordfish.lemuroid.app.shared.storage.cache.CacheCleanerWork
import com.swordfish.lemuroid.ext.feature.review.ReviewManager
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.dao.updateAsync
import com.swordfish.lemuroid.lib.library.db.entity.Game
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class GameLaunchTaskHandler(
    private val reviewManager: ReviewManager,
    private val retrogradeDb: RetrogradeDatabase
) {

    fun handleGameStart(context: Context) {
        cancelBackgroundWork(context)
    }

    fun handleGameFinish(enableRatingFlow: Boolean, activity: Activity, resultCode: Int, data: Intent?): Completable {
        rescheduleBackgroundWork(activity.applicationContext)
        return when (resultCode) {
            Activity.RESULT_OK -> handleSuccessfulGameFinish(activity, enableRatingFlow, data)
            BaseGameActivity.RESULT_ERROR -> handleUnsuccessfulGameFinish(
                activity,
                data?.getStringExtra(BaseGameActivity.PLAY_GAME_RESULT_ERROR)!!,
                null
            )
            BaseGameActivity.RESULT_UNEXPECTED_ERROR -> handleUnsuccessfulGameFinish(
                activity,
                activity.getString(R.string.lemuroid_crash_disclamer),
                data?.getStringExtra(BaseGameActivity.PLAY_GAME_RESULT_ERROR)
            )
            else -> Completable.complete()
        }
    }

    private fun cancelBackgroundWork(context: Context) {
        SaveSyncWork.cancelAutoWork(context)
        SaveSyncWork.cancelManualWork(context)
        CacheCleanerWork.cancelCleanCacheLRU(context)
    }

    private fun rescheduleBackgroundWork(context: Context) {
        // Let's slightly delay the sync. Maybe the user wants to play another game.
        SaveSyncWork.enqueueAutoWork(context, 5)
        CacheCleanerWork.enqueueCleanCacheLRU(context)
    }

    private fun handleUnsuccessfulGameFinish(activity: Activity, message: String, messageDetail: String?): Completable {
        return Completable.fromAction {
            GameCrashActivity.launch(activity, message, messageDetail)
        }.subscribeOn(AndroidSchedulers.mainThread())
    }

    private fun handleSuccessfulGameFinish(
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
