package come.asg.ominoy.app.shared.systems

import android.content.Context
import come.asg.ominoy.lib.library.MetaSystemID

data class MetaSystemInfo(
    val metaSystem: MetaSystemID,
    val count: Int,
) {
    fun getName(context: Context) = context.resources.getString(metaSystem.titleResId)
}
