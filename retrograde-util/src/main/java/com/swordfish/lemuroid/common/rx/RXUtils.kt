package com.swordfish.lemuroid.common.rx

import io.reactivex.disposables.Disposable

fun Disposable?.safeDispose(): Disposable? {
    this?.let { if (!it.isDisposed) it.dispose() }
    return null
}
