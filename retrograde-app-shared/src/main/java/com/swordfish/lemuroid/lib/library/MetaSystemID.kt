package com.swordfish.lemuroid.lib.library

import com.swordfish.lemuroid.common.graphics.ColorUtils
import com.swordfish.lemuroid.lib.R

fun GameSystem.metaSystemID() = MetaSystemID.fromSystemID(id)

/** Meta systems represents a collection of systems which appear the same to the user. It's currently
 *  only for Arcade (without separating FBNeo, MAME2000 or MAME2003). */
enum class MetaSystemID(val titleResId: Int, val imageResId: Int, val systemIDs: List<SystemID>) {
    NES(
        R.string.game_system_title_nes,
        R.drawable.game_system_nes,
        listOf(SystemID.NES),
    ),
    SNES(
        R.string.game_system_title_snes,
        R.drawable.game_system_snes,
        listOf(SystemID.SNES),
    ),
    GENESIS(
        R.string.game_system_title_genesis,
        R.drawable.game_system_genesis,
        listOf(SystemID.GENESIS, SystemID.SEGACD),
    ),
    GB(
        R.string.game_system_title_gb,
        R.drawable.game_system_gb,
        listOf(SystemID.GB),
    ),
    GBC(
        R.string.game_system_title_gbc,
        R.drawable.game_system_gbc,
        listOf(SystemID.GBC),
    ),
    GBA(
        R.string.game_system_title_gba,
        R.drawable.game_system_gba,
        listOf(SystemID.GBA),
    ),
    N64(
        R.string.game_system_title_n64,
        R.drawable.game_system_n64,
        listOf(SystemID.N64),
    ),
    SMS(
        R.string.game_system_title_sms,
        R.drawable.game_system_sms,
        listOf(SystemID.SMS),
    ),
    PSP(
        R.string.game_system_title_psp,
        R.drawable.game_system_psp,
        listOf(SystemID.PSP),
    ),
    NDS(
        R.string.game_system_title_nds,
        R.drawable.game_system_ds,
        listOf(SystemID.NDS),
    ),
    GG(
        R.string.game_system_title_gg,
        R.drawable.game_system_gg,
        listOf(SystemID.GG),
    ),
    ATARI2600(
        R.string.game_system_title_atari2600,
        R.drawable.game_system_atari2600,
        listOf(SystemID.ATARI2600),
    ),
    PSX(
        R.string.game_system_title_psx,
        R.drawable.game_system_psx,
        listOf(SystemID.PSX),
    ),
    ARCADE(
        R.string.game_system_title_arcade,
        R.drawable.game_system_arcade,
        listOf(SystemID.FBNEO, SystemID.MAME2003PLUS),
    ),
    ATARI7800(
        R.string.game_system_title_atari7800,
        R.drawable.game_system_atari7800,
        listOf(SystemID.ATARI7800),
    ),
    LYNX(
        R.string.game_system_title_lynx,
        R.drawable.game_system_lynx,
        listOf(SystemID.LYNX),
    ),
    PC_ENGINE(
        R.string.game_system_title_pce,
        R.drawable.game_system_pce,
        listOf(SystemID.PC_ENGINE),
    ),
    NGP(
        R.string.game_system_title_ngp,
        R.drawable.game_system_ngp,
        listOf(SystemID.NGP, SystemID.NGC),
    ),
    WS(
        R.string.game_system_title_ws,
        R.drawable.game_system_ws,
        listOf(SystemID.WS, SystemID.WSC),
    ),
    DOS(
        R.string.game_system_title_dos,
        R.drawable.game_system_dos,
        listOf(SystemID.DOS),
    ),
    NINTENDO_3DS(
        R.string.game_system_title_3ds,
        R.drawable.game_system_3ds,
        listOf(SystemID.NINTENDO_3DS),
    ),
    ;

    fun color(): Int {
        return ColorUtils.color(ordinal.toFloat() / values().size)
    }

    companion object {
        fun fromSystemID(systemID: SystemID): MetaSystemID {
            return when (systemID) {
                SystemID.FBNEO -> ARCADE
                SystemID.MAME2003PLUS -> ARCADE
                SystemID.ATARI2600 -> ATARI2600
                SystemID.GB -> GB
                SystemID.GBC -> GBC
                SystemID.GBA -> GBA
                SystemID.GENESIS -> GENESIS
                SystemID.SEGACD -> GENESIS
                SystemID.GG -> GG
                SystemID.N64 -> N64
                SystemID.NDS -> NDS
                SystemID.NES -> NES
                SystemID.PSP -> PSP
                SystemID.PSX -> PSX
                SystemID.SMS -> SMS
                SystemID.SNES -> SNES
                SystemID.PC_ENGINE -> PC_ENGINE
                SystemID.LYNX -> LYNX
                SystemID.ATARI7800 -> ATARI7800
                SystemID.DOS -> DOS
                SystemID.NGP -> NGP
                SystemID.NGC -> NGP
                SystemID.WS -> WS
                SystemID.WSC -> WS
                SystemID.NINTENDO_3DS -> NINTENDO_3DS
            }
        }
    }
}
