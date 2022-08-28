package com.swordfish.lemuroid.app.shared.input

import android.view.InputDevice

internal fun inputKeysOf(vararg keyCodes: Int) = keyCodes.map { InputKey(it) }

internal fun retroKeysOf(vararg keyCodes: Int) = keyCodes.map { RetroKey(it) }

internal fun bindingsOf(vararg bindings: Pair<Int, Int>) = bindings.associate {
    InputKey(it.first) to RetroKey(it.second)
}

fun InputDevice.supportsAllKeys(inputKeys: List<InputKey>): Boolean {
    val supportedKeyCodes = inputKeys
        .map { it.keyCode }
        .toIntArray()

    return this.hasKeys(*supportedKeyCodes).all { it }
}
