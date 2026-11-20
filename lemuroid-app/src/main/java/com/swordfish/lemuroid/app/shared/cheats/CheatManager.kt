package com.swordfish.lemuroid.app.shared.cheats

import com.swordfish.lemuroid.lib.cheats.CheatFormatter
import com.swordfish.lemuroid.lib.library.db.entity.Cheat
import com.swordfish.libretrodroid.GLRetroView
import com.swordfish.libretrodroid.LibretroDroid
import timber.log.Timber

object CheatManager {
    fun filterApplicableCheats(cheats: List<Cheat>): List<Cheat> {
        val enabledCheats = cheats.filter { it.enabled }
        val enabledMasterCodeIds = enabledCheats.filter { it.isMasterCode }.map { it.id }.toSet()

        val validCheats =
            enabledCheats.filter { cheat ->
                if (cheat.isMasterCode) {
                    true
                } else if (cheat.masterCodeId != null) {
                    enabledMasterCodeIds.contains(cheat.masterCodeId)
                } else {
                    true
                }
            }

        return validCheats.sortedWith(
            compareByDescending<Cheat> { it.isMasterCode }
                .thenBy { it.orderIndex }
                .thenBy { it.id },
        )
    }

    fun applyCheats(
        retroGameView: GLRetroView?,
        systemId: String,
        cheats: List<Cheat>,
    ) {
        if (retroGameView == null) return

        retroGameView.queueEvent {
            try {
                LibretroDroid.resetCheat()
                val applicable = filterApplicableCheats(cheats)
                Timber.i("Applying ${applicable.size} cheats (entries) for system $systemId")
                var globalIndex = 0
                for (cheat in applicable) {
                    val codeLines = CheatFormatter.formatLinesForCore(systemId, cheat.cheatType, cheat.code)
                    for (codeLine in codeLines) {
                        if (codeLine.isNotEmpty()) {
                            Timber.d(
                                "retro_cheat_set index=$globalIndex master=${cheat.isMasterCode} " +
                                    "type=${cheat.cheatType} code=$codeLine",
                            )
                            LibretroDroid.setCheat(globalIndex, true, codeLine)
                            globalIndex++
                        }
                    }
                }
                Timber.i("Total retro_cheat_set calls: $globalIndex")
            } catch (e: Throwable) {
                Timber.e(e, "Error applying cheats to core")
            }
        }
    }

    fun clearCheats(retroGameView: GLRetroView?) {
        if (retroGameView == null) return

        retroGameView.queueEvent {
            try {
                Timber.i("Clearing all cheats from core")
                LibretroDroid.resetCheat()
            } catch (e: Throwable) {
                Timber.e(e, "Error clearing cheats from core")
            }
        }
    }
}
