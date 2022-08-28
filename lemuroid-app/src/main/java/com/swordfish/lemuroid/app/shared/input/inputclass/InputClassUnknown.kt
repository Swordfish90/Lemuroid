package com.swordfish.lemuroid.app.shared.input.inputclass

import com.swordfish.lemuroid.app.shared.input.InputKey

object InputClassUnknown : InputClass {
    override fun getInputKeys(): Set<InputKey> {
        return emptySet()
    }

    override fun getAxesMap(): Map<Int, Int> {
        return emptyMap()
    }
}
