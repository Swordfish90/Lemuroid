package com.codebutler.odyssey.core.kotlin

fun <E> Collection<E>.containsAny(vararg elements: E): Boolean = elements.any { element -> this.contains(element) }
