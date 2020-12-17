package com.swordfish.lemuroid.app.shared.main

import android.app.Activity
import android.content.Context
import com.swordfish.lemuroid.ext.feature.review.ReviewManager
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.dao.updateAsync
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.storage.cache.CacheCleanerWork
import io.reactivex.Completable
import io.reactivex.rxkotlin.subscribeBy
import java.util.concurrent.TimeUnit

class PostGameHandler(
    private val applicationContext: Context,
    private val reviewManager: ReviewManager,
    private val retrogradeDb: RetrogradeDatabase
) {

    fun handleAfterGame(activity: Activity, leanback: Boolean, game: Game, duration: Long) {
        CacheCleanerWork.enqueueCleanCacheLRU(applicationContext)
        updateGamePlayedTimestamp(game)

        if (!leanback) {
            displayReviewRequest(activity, duration)
        }
    }

    private fun displayReviewRequest(activity: Activity, durationMillis: Long) {
        Completable.timer(500, TimeUnit.MILLISECONDS)
            .andThen { reviewManager.startReviewFlow(activity, durationMillis) }
            .subscribeBy { }
    }

    private fun updateGamePlayedTimestamp(game: Game) {
        retrogradeDb.gameDao()
            .updateAsync(game.copy(lastPlayedAt = System.currentTimeMillis()))
            .subscribeBy { }
    }
}
