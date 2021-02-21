package com.swordfish.lemuroid.common.rx

import com.gojuno.koptional.Optional
import com.gojuno.koptional.Some
import com.gojuno.koptional.toOptional
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import kotlin.reflect.KProperty0
import kotlin.reflect.jvm.isAccessible

fun <T : Any> KProperty0<T?>.asNullableObservable(): Observable<Optional<T>> {
    return Maybe.fromCallable { getDelegate() }
        .flatMapObservable {
            delegate ->
            when (delegate) {
                is RxNullableProperty<*> -> delegate.relay as Observable<Optional<T>>
                is RxProperty<*> -> (delegate.relay as Observable<T>).map { Some(it) }
                else -> Observable.just(get().toOptional())
            }
        }
        .doOnSubscribe { isAccessible = true }
        .subscribeOn(Schedulers.io())
}

fun <T : Any> KProperty0<T?>.asObservable(): Observable<T> {
    return Maybe.fromCallable { getDelegate() }
        .flatMapObservable { delegate ->
            when (delegate) {
                is RxNullableProperty<*> ->
                    (delegate.relay as Observable<Optional<T>>)
                        .filter { it is Some }
                        .map { it.toNullable()!! }

                is RxProperty<*> -> delegate.relay as Observable<T>
                else -> throw IllegalAccessException()
            }
        }
        .doOnSubscribe { isAccessible = true }
        .subscribeOn(Schedulers.io())
}
