package com.swordfish.touchinput.utils

import io.reactivex.Observable

fun <T> observableOf(vararg ts: T): Observable<T> = Observable.fromArray(*ts)