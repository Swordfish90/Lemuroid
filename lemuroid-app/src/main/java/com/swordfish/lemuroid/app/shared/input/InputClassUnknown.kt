package com.swordfish.lemuroid.app.shared.input

import android.content.Context
import com.swordfish.lemuroid.app.shared.settings.GameMenuShortcut

object InputClassUnknown : InputClass {
    override fun getInputKeys(): Set<InputKey> = emptySet()

    override fun getAxesMap(): Map<Int, Int> = emptyMap()

    override fun getDefaultBindings(): Map<InputKey, RetroKey> = emptyMap()

    override fun isSupported(): Boolean = false

    override fun isEnabledByDefault(appContext: Context): Boolean = false

    override fun getSupportedShortcuts(): List<GameMenuShortcut> = emptyList()

    override fun getCustomizableKeys(): List<RetroKey> = emptyList()
}
