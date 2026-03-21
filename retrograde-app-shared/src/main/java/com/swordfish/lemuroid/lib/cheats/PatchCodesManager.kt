package com.swordfish.lemuroid.lib.cheats

import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.PatchCode
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
}
