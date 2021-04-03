package com.swordfish.lemuroid.ext.utils

import com.google.android.play.core.tasks.Task
import io.reactivex.Completable
import io.reactivex.Single

fun <T> Task<T>.toSingle() = Single.create<T> { emitter ->
    this
        .addOnSuccessListener { emitter.onSuccess(it) }
        .addOnFailureListener { emitter.onError(it) }
}

fun <T> Task<T>.toCompletable(): Completable = this.toSingle().ignoreElement()
