package com.swordfish.lemuroid.ext.feature.review

import android.app.Activity
import android.content.Context
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import io.reactivex.Completable
import timber.log.Timber
import java.util.concurrent.TimeUnit

class ReviewManager {
    private var reviewManager: ReviewManager? = null
    private var reviewInfo: ReviewInfo? = null

    fun initialize(context: Context) {
        reviewManager = ReviewManagerFactory.create(context)

        val task = reviewManager?.requestReviewFlow()
        task?.addOnCompleteListener {
            if (task.isSuccessful) {
                Timber.i("Review retrieval was successful")
                reviewInfo = task.result
            }
        }
    }

    fun startReviewFlow(activity: Activity, sessionTimeMillis: Long): Completable {
        // Only sessions which lasted more than 10 minutes considered good sessions
        if (sessionTimeMillis < MIN_GAME_SESSION_LENGTH) {
            return Completable.complete()
        }
        return createReviewCompletable(activity).onErrorComplete()
    }

    private fun createReviewCompletable(activity: Activity) = Completable.create { emitter ->
        val task = reviewInfo?.let {
            reviewManager?.launchReviewFlow(activity, it)
        }

        if (task == null) {
            emitter.onComplete()
            return@create
        }

        task.addOnCompleteListener {
            emitter.onComplete()
        }
    }

    companion object {
        private val MIN_GAME_SESSION_LENGTH = TimeUnit.MINUTES.toMillis(5)
    }
}
