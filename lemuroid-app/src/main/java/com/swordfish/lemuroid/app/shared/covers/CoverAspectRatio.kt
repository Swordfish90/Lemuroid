package com.swordfish.lemuroid.app.shared.covers

import com.swordfish.lemuroid.lib.library.SystemID

object CoverAspectRatio {
    /** PAL/Western box fronts; systems without a reference use the artwork's own proportions. */
    fun forSystem(
        systemId: String,
        imageWidth: Int = 0,
        imageHeight: Int = 0,
    ): Float =
        when (systemId) {
            SystemID.NES.dbname -> 127f / 178f
            SystemID.SNES.dbname, SystemID.N64.dbname -> 178f / 125f
            SystemID.GB.dbname, SystemID.GBC.dbname, SystemID.GBA.dbname -> 1f
            SystemID.GENESIS.dbname -> 123f / 172f
            SystemID.SMS.dbname -> 2453f / 3440f
            SystemID.GG.dbname -> 22f / 31f
            SystemID.PSX.dbname -> 245f / 251f
            SystemID.PSP.dbname -> 573f / 992f
            SystemID.LYNX.dbname -> 35f / 43f
            else -> if (imageWidth > 0 && imageHeight > 0) imageWidth.toFloat() / imageHeight else 1f
        }
}
