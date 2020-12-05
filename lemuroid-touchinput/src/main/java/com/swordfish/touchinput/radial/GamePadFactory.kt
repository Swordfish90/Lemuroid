package com.swordfish.touchinput.radial

import com.swordfish.lemuroid.lib.library.CoreID
import com.swordfish.lemuroid.lib.library.SystemID
import com.swordfish.radialgamepad.library.config.RadialGamePadConfig
import java.lang.UnsupportedOperationException

object GamePadFactory {

    data class Config(
        val leftConfig: RadialGamePadConfig,
        val rightConfig: RadialGamePadConfig,
        val leftScaling: Float = 1.0f,
        val rightScaling: Float = 1.0f
    )

    fun getRadialPadConfig(systemId: SystemID, coreID: CoreID): Config {

        return when (systemId) {
            SystemID.SNES -> Config(
                RadialPadConfigs.SNES_LEFT,
                RadialPadConfigs.SNES_RIGHT,
            )

            SystemID.GBA -> Config(
                RadialPadConfigs.GBA_LEFT,
                RadialPadConfigs.GBA_RIGHT
            )

            SystemID.GENESIS -> Config(
                RadialPadConfigs.GENESIS_LEFT,
                RadialPadConfigs.GENESIS_RIGHT,
                1f,
                1.2f
            )

            SystemID.ATARI2600 -> Config(
                RadialPadConfigs.ATARI2600_LEFT,
                RadialPadConfigs.ATARI2600_RIGHT,
            )

            SystemID.SMS -> Config(
                RadialPadConfigs.SMS_LEFT,
                RadialPadConfigs.SMS_RIGHT,
            )

            SystemID.GG -> Config(
                RadialPadConfigs.GG_LEFT,
                RadialPadConfigs.GG_RIGHT,
            )

            SystemID.FBNEO, SystemID.MAME2003PLUS -> {
                Config(
                    RadialPadConfigs.ARCADE_LEFT,
                    RadialPadConfigs.ARCADE_RIGHT,
                    1.0f,
                    1.2f
                )
            }

            SystemID.GB, SystemID.GBC -> Config(
                RadialPadConfigs.GB_LEFT,
                RadialPadConfigs.GB_RIGHT,
            )

            SystemID.PSP -> Config(
                RadialPadConfigs.PSP_LEFT,
                RadialPadConfigs.PSP_RIGHT
            )

            SystemID.N64 -> Config(
                RadialPadConfigs.N64_LEFT,
                RadialPadConfigs.N64_RIGHT,
            )

            SystemID.PSX -> Config(
                RadialPadConfigs.PSX_LEFT,
                RadialPadConfigs.PSX_RIGHT
            )

            SystemID.NDS -> when (coreID) {
                CoreID.DESMUME -> Config(
                    RadialPadConfigs.DESMUME_LEFT,
                    RadialPadConfigs.DESMUME_RIGHT,
                )
                CoreID.MELONDS -> Config(
                    RadialPadConfigs.MELONDS_NDS_LEFT,
                    RadialPadConfigs.MELONDS_NDS_RIGHT,
                )
                else -> throw UnsupportedOperationException("This core is not supported on the system.")
            }

            SystemID.NES -> Config(
                RadialPadConfigs.NES_LEFT,
                RadialPadConfigs.NES_RIGHT,
            )
        }
    }
}
