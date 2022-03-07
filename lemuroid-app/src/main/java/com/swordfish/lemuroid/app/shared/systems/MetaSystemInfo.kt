package com.swordfish.lemuroid.app.shared.systems

import android.content.Context
import com.swordfish.lemuroid.lib.library.MetaSystemID

data class MetaSystemInfo(val metaSystem: MetaSystemID, val count: Int) {
    fun getName(context: Context) = context.resources.getString(metaSystem.titleResId)
}
