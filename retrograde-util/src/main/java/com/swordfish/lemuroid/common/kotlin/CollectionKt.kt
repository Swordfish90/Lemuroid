/*
 * CollectionKt.kt
 *
 * Copyright (C) 2017 Retrograde Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.swordfish.lemuroid.common.kotlin

fun <K, V> Map<K, V?>.filterNotNullValues(): Map<K, V> {
    val destination = mutableMapOf<K, V>()
    for ((key, value) in this) {
        if (value != null) {
            destination[key] = value
        }
    }
    return destination
}

inline fun <X, Y, Z, H> Map<X, Y>.zipOnKeys(
    other: Map<X, Z>,
    f: (Y, Z) -> H,
): Map<X, H> {
    return this.keys.intersect(other.keys)
        .map { key ->
            key to f(this[key]!!, other[key]!!)
        }
        .toMap()
}

fun <E> Array<E>.toIndexedMap(): Map<Int, E> =
    this
        .mapIndexed { index, e -> index to e }
        .toMap()

inline fun <T, K> Iterable<T>.associateByNotNull(keySelector: (T) -> K?): Map<K, T> {
    return this.map { keySelector(it) to it }
        .filter { (key, _) -> key != null }
        .associate { (key, value) -> key!! to value }
}
