package com.swordfish.lemuroid.app.shared.game

import android.content.Context
import com.swordfish.lemuroid.app.utils.android.getGLSLVersion
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.SystemID
import com.swordfish.libretrodroid.ShaderConfig

object ShaderChooser {
    fun getShaderForSystem(
        context: Context,
        hdMode: Boolean,
        forceLegacyHdMode: Boolean,
        screenFilter: String,
        system: GameSystem,
    ): ShaderConfig {
        val useNewHdMode = context.getGLSLVersion() >= 3 && hdMode && !forceLegacyHdMode
        return when {
            useNewHdMode -> getHDShaderForSystem(system)
            hdMode -> getLegacyHDShaderForSystem(system)
            else ->
                when (screenFilter) {
                    "crt" -> ShaderConfig.CRT
                    "lcd" -> ShaderConfig.LCD
                    "smooth" -> ShaderConfig.Default
                    "sharp" -> ShaderConfig.Sharp
                    else -> getDefaultShaderForSystem(system)
                }
        }
    }

    private fun getDefaultShaderForSystem(system: GameSystem): ShaderConfig {
        return when (system.id) {
            SystemID.GBA -> ShaderConfig.LCD
            SystemID.GBC -> ShaderConfig.LCD
            SystemID.GB -> ShaderConfig.LCD
            SystemID.N64 -> ShaderConfig.CRT
            SystemID.GENESIS -> ShaderConfig.CRT
            SystemID.SEGACD -> ShaderConfig.CRT
            SystemID.NES -> ShaderConfig.CRT
            SystemID.SNES -> ShaderConfig.CRT
            SystemID.FBNEO -> ShaderConfig.CRT
            SystemID.SMS -> ShaderConfig.CRT
            SystemID.PSP -> ShaderConfig.LCD
            SystemID.NDS -> ShaderConfig.LCD
            SystemID.GG -> ShaderConfig.LCD
            SystemID.ATARI2600 -> ShaderConfig.CRT
            SystemID.PSX -> ShaderConfig.CRT
            SystemID.MAME2003PLUS -> ShaderConfig.CRT
            SystemID.ATARI7800 -> ShaderConfig.CRT
            SystemID.PC_ENGINE -> ShaderConfig.CRT
            SystemID.LYNX -> ShaderConfig.LCD
            SystemID.DOS -> ShaderConfig.CRT
            SystemID.NGP -> ShaderConfig.LCD
            SystemID.NGC -> ShaderConfig.LCD
            SystemID.WS -> ShaderConfig.LCD
            SystemID.WSC -> ShaderConfig.LCD
            SystemID.NINTENDO_3DS -> ShaderConfig.LCD
        }
    }

    private fun getLegacyHDShaderForSystem(system: GameSystem): ShaderConfig.CUT {
        val upscale8BitsMobile =
            ShaderConfig.CUT(
                blendMinContrastEdge = 0.00f,
                blendMaxContrastEdge = 0.50f,
                blendMaxSharpness = 0.85f,
            )

        val upscale8Bits =
            ShaderConfig.CUT(
                blendMinContrastEdge = 0.00f,
                blendMaxContrastEdge = 0.50f,
                blendMaxSharpness = 0.75f,
            )

        val upscale16BitsMobile =
            ShaderConfig.CUT(
                blendMinContrastEdge = 0.10f,
                blendMaxContrastEdge = 0.60f,
                blendMaxSharpness = 0.85f,
            )

        val upscale16Bits =
            ShaderConfig.CUT(
                blendMinContrastEdge = 0.10f,
                blendMaxContrastEdge = 0.60f,
                blendMaxSharpness = 0.75f,
            )

        val upscale32Bits =
            ShaderConfig.CUT(
                blendMinContrastEdge = 0.25f,
                blendMaxContrastEdge = 0.75f,
                blendMaxSharpness = 0.75f,
            )

        val modern =
            ShaderConfig.CUT(
                blendMinContrastEdge = 0.25f,
                blendMaxContrastEdge = 0.75f,
                blendMaxSharpness = 0.50f,
            )

        return when (system.id) {
            SystemID.GBA -> upscale16BitsMobile
            SystemID.GBC -> upscale8BitsMobile
            SystemID.GB -> upscale8BitsMobile
            SystemID.N64 -> upscale32Bits
            SystemID.GENESIS -> upscale16Bits
            SystemID.SEGACD -> upscale16Bits
            SystemID.NES -> upscale8Bits
            SystemID.SNES -> upscale16Bits
            SystemID.FBNEO -> upscale32Bits
            SystemID.SMS -> upscale8Bits
            SystemID.PSP -> modern
            SystemID.NDS -> upscale32Bits
            SystemID.GG -> upscale8BitsMobile
            SystemID.ATARI2600 -> upscale8Bits
            SystemID.PSX -> upscale32Bits
            SystemID.MAME2003PLUS -> upscale32Bits
            SystemID.ATARI7800 -> upscale8Bits
            SystemID.PC_ENGINE -> upscale16Bits
            SystemID.LYNX -> upscale8BitsMobile
            SystemID.DOS -> upscale32Bits
            SystemID.NGP -> upscale8BitsMobile
            SystemID.NGC -> upscale8BitsMobile
            SystemID.WS -> upscale16BitsMobile
            SystemID.WSC -> upscale16BitsMobile
            SystemID.NINTENDO_3DS -> modern
        }
    }

    private fun getHDShaderForSystem(system: GameSystem): ShaderConfig.CUT2 {
        val upscale8BitsMobile =
            ShaderConfig.CUT2(
                blendMinContrastEdge = 0.00f,
                blendMaxContrastEdge = 0.50f,
                blendMaxSharpness = 0.85f,
            )

        val upscale8Bits =
            ShaderConfig.CUT2(
                blendMinContrastEdge = 0.00f,
                blendMaxContrastEdge = 0.50f,
                blendMaxSharpness = 0.75f,
            )

        val upscale16BitsMobile =
            ShaderConfig.CUT2(
                blendMinContrastEdge = 0.10f,
                blendMaxContrastEdge = 0.60f,
                blendMaxSharpness = 0.85f,
            )

        val upscale16Bits =
            ShaderConfig.CUT2(
                blendMinContrastEdge = 0.10f,
                blendMaxContrastEdge = 0.60f,
                blendMaxSharpness = 0.75f,
            )

        val upscale32Bits =
            ShaderConfig.CUT2(
                blendMinContrastEdge = 0.25f,
                blendMaxContrastEdge = 0.75f,
                blendMaxSharpness = 0.75f,
            )

        val modern =
            ShaderConfig.CUT2(
                blendMinContrastEdge = 0.25f,
                blendMaxContrastEdge = 0.75f,
                blendMaxSharpness = 0.50f,
            )

        return when (system.id) {
            SystemID.GBA -> upscale16BitsMobile
            SystemID.GBC -> upscale8BitsMobile
            SystemID.GB -> upscale8BitsMobile
            SystemID.N64 -> upscale32Bits
            SystemID.GENESIS -> upscale16Bits
            SystemID.SEGACD -> upscale16Bits
            SystemID.NES -> upscale8Bits
            SystemID.SNES -> upscale16Bits
            SystemID.FBNEO -> upscale32Bits
            SystemID.SMS -> upscale8Bits
            SystemID.PSP -> modern
            SystemID.NDS -> upscale32Bits
            SystemID.GG -> upscale8BitsMobile
            SystemID.ATARI2600 -> upscale8Bits
            SystemID.PSX -> upscale32Bits
            SystemID.MAME2003PLUS -> upscale32Bits
            SystemID.ATARI7800 -> upscale8Bits
            SystemID.PC_ENGINE -> upscale16Bits
            SystemID.LYNX -> upscale8BitsMobile
            SystemID.DOS -> upscale32Bits
            SystemID.NGP -> upscale8BitsMobile
            SystemID.NGC -> upscale8BitsMobile
            SystemID.WS -> upscale16BitsMobile
            SystemID.WSC -> upscale16BitsMobile
            SystemID.NINTENDO_3DS -> modern
        }
    }
}
