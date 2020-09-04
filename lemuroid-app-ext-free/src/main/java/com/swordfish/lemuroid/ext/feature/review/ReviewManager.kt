package com.swordfish.lemuroid.ext.feature.review

import android.app.Activity
import android.content.Context
import io.reactivex.Completable

// The real ReviewManager is implemented in the play version. This is basically stubbed.
class ReviewManager {
    fun initialize(context: Context) {}

    fun startReviewFlow(activity: Activity, sessionTimeMillis: Long) = Completable.complete()
}