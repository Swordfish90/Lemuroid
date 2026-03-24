package com.swordfish.lemuroid.lib.cheats

import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.PatchCode
import com.swordfish.libretrodroid.GLRetroView
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PatchCodesManager @Inject constructor(
    private val retrogradeDatabase: RetrogradeDatabase,
) {
    fun getCodesForGame(gameId: Int): Flow<List<PatchCode>> =
        retrogradeDatabase.patchCodeDao().getCodesForGame(gameId)

    suspend fun getEnabledCodesForGame(gameId: Int): List<PatchCode> =
        retrogradeDatabase.patchCodeDao().getEnabledCodesForGame(gameId)

    suspend fun saveCode(code: PatchCode): Long =
        retrogradeDatabase.patchCodeDao().insert(code)

    suspend fun updateCode(code: PatchCode) =
        retrogradeDatabase.patchCodeDao().update(code)

    suspend fun deleteCode(code: PatchCode) =
        retrogradeDatabase.patchCodeDao().delete(code)

    /**
     * Apply all enabled codes to GLRetroView.
     * Calls resetCheat() first to remove any previously applied codes,
     * then sends each enabled code with its index.
     */
    fun applyToRetroView(codes: List<PatchCode>, retroView: GLRetroView) {
        retroView.resetCheat()
        codes
            .filter { it.enabled }
            .forEachIndexed { index, patch ->
                retroView.sendCheat(index, true, patch.code)
            }
    }
}
