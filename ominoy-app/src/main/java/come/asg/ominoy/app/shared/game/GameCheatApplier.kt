package come.asg.ominoy.app.shared.game

import com.swordfish.libretrodroid.GLRetroView
import com.swordfish.libretrodroid.LibretroDroid
import come.asg.ominoy.app.mobile.feature.cheats.CheatCodeType
import come.asg.ominoy.app.mobile.feature.cheats.CheatCodeValidator
import come.asg.ominoy.lib.library.db.entity.GameCheat
import timber.log.Timber

object GameCheatApplier {
    fun apply(
        retroView: GLRetroView?,
        cheats: List<GameCheat>,
        resetEvenIfEmpty: Boolean = false,
        systemId: String? = null,
    ) {
        if (retroView == null || (cheats.isEmpty() && !resetEvenIfEmpty)) return

        val orderedCheats = cheats.sortedBy(GameCheat::id)
        retroView.queueEvent {
            runCatching { LibretroDroid.resetCheat() }
                .onFailure { Timber.w(it, "Unable to reset cheats for the active core") }
            var nativeIndex = 0
            orderedCheats.forEach { cheat ->
                val codeType = CheatCodeType.fromPersistedValue(cheat.codeType)
                val normalizedCode =
                    if (codeType != null && systemId != null) {
                        CheatCodeValidator.normalizeStored(cheat.code, codeType, systemId)
                    } else {
                        // Keep the pre-type format path for existing cheat rows from older app versions.
                        CheatCodeFormatter.normalize(cheat.code)
                    }
                if (normalizedCode == null) {
                    Timber.w("Skipping malformed cheat ${cheat.id}; it was not sent to the active core")
                    return@forEach
                }
                runCatching {
                    retroView.setCheat(nativeIndex++, cheat.enabled, normalizedCode)
                }.onFailure { error ->
                    Timber.w(error, "Active core rejected cheat ${cheat.id}; continuing with remaining cheats")
                }
            }
        }
    }
}
