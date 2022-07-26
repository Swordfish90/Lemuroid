package com.swordfish.lemuroid.common.kotlin

inline fun <T> T?.nullIfNot(block: (T) -> Boolean): T? { // TODO FILIPPO... This needs refactoring...
    return if (this != null && block(this)) { this } else { null }
}
