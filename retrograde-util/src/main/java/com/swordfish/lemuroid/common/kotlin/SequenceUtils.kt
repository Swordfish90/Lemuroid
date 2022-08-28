package com.swordfish.lemuroid.common.kotlin

fun <T> lazySequenceOf(vararg producers: () -> T): Sequence<T> {
    return producers.asSequence()
        .map { it() }
}
