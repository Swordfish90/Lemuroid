package com.swordfish.lemuroid.app.shared.main

import android.app.Activity
import com.swordfish.lemuroid.ext.feature.review.ReviewManager
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.dao.updateAsync
import com.swordfish.lemuroid.lib.library.db.entity.Game
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class PostGameHandler(
    private val reviewManager: ReviewManager,
    private val retrogradeDb: RetrogradeDatabase
) {

    fun handleAfterGame(
        activity: Activity,
        enableRatingFlow: Boolean,
        game: Game,
        duration: Long
    ): Completable {

        return Single.just(game)
            .flatMapCompletable { game ->
                val comps = mutableListOf<Completable>().apply {
                    add(updateGamePlayedTimestamp(game))

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
