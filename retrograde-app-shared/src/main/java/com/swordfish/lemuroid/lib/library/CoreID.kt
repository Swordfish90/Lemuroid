package com.swordfish.lemuroid.lib.library

import android.content.SharedPreferences
import com.swordfish.lemuroid.lib.core.CoreUpdater
import com.swordfish.lemuroid.lib.core.assetsmanager.NoAssetsManager
import com.swordfish.lemuroid.lib.core.assetsmanager.PPSSPPAssetsManager
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import io.reactivex.Completable

enum class CoreID(
    val coreName: String,
    val coreDisplayName: String,
    val libretroFileName: String
) {
    STELLA(
        "stella",
        "Stella",
        "libstella_libretro_android.so"
    ),
    FCEUMM(
        "fceumm",
        "FCEUmm",
        "libfceumm_libretro_android.so"
    ),
    SNES9X(
        "snes9x",
        "Snes9x",
        "libsnes9x_libretro_android.so"
    ),
    GENESIS_PLUS_GX(
        "genesis_plus_gx",
        "Genesis Plus GX",
        "libgenesis_plus_gx_libretro_android.so"
    ),
    GAMBATTE(
        "gambatte",
        "Gambatte",
        "libgambatte_libretro_android.so"
    ),
    MGBA(
        "mgba",
        "mGBA",
        "libmgba_libretro_android.so"
    ),
    MUPEN64_PLUS_NEXT(
        "mupen64plus_next_gles3",
        "Mupen64Plus",
        "libmupen64plus_next_gles3_libretro_android.so"
    ),
    PCSX_REARMED(
        "pcsx_rearmed",
        "PCSXReARMed",
        "libpcsx_rearmed_libretro_android.so"
    ),
    PPSSPP(
        "ppsspp",
        "PPSSPP",
        "libppsspp_libretro_android.so"
    ),
    FBNEO(
        "fbneo",
        "FBNeo",
        "libfbneo_libretro_android.so"
    ),
    MAME2003PLUS(
        "mame2003_plus",
        "MAME2003 Plus",
        "libmame2003_plus_libretro_android.so"
    ),
    DESMUME(
        "desmume",
        "DeSmuME",
        "libdesmume_libretro_android.so"
    ),
    MELONDS(
        "melonds",
        "MelonDS",
        "libmelonds_libretro_android.so"
    ),
    HANDY(
        "handy",
        "Handy",
        "libhandy_libretro_android.so"
    ),
    MEDNAFEN_PCE_FAST(
        "mednafen_pce_fast",
        "PCEFast",
        "libmednafen_pce_fast_libretro_android.so"
    ),
    PROSYSTEM(
        "prosystem",
        "ProSystem",
        "libprosystem_libretro_android.so"
    );

    companion object {
        fun getAssetManager(coreID: CoreID): AssetsManager {
            return when (coreID) {
                PPSSPP -> PPSSPPAssetsManager()
                else -> NoAssetsManager()
            }
        }
    }

    interface AssetsManager {
        fun retrieveAssetsIfNeeded(
            coreUpdaterApi: CoreUpdater.CoreManagerApi,
            directoriesManager: DirectoriesManager,
            sharedPreferences: SharedPreferences
        ): Completable

        fun clearAssets(directoriesManager: DirectoriesManager): Completable
    }
}

fun findByName(query: String): CoreID? =
    CoreID.values().firstOrNull { it.coreName == query }
