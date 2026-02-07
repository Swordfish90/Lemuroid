package com.swordfish.lemuroid.app.shared.game.saves

import com.swordfish.lemuroid.lib.library.CoreID
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.saves.SaveState
import com.swordfish.lemuroid.lib.saves.SavesManager
import com.swordfish.lemuroid.lib.saves.StatesManager
import com.swordfish.lemuroid.lib.injection.PerApp
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

data class AutoSavePayload(
    val game: Game,
    val coreID: CoreID,
    val sram: ByteArray?,
    val autoSave: SaveState?,
)

@PerApp
class AutoSaveCoordinator @Inject constructor(
    private val savesManager: SavesManager,
    private val statesManager: StatesManager,
) {
    private val pending = AtomicReference<AutoSavePayload?>(null)
    private val stopRequested = AtomicBoolean(false)

    fun setPending(payload: AutoSavePayload) {
        pending.set(payload)
    }

    fun popPending(): AutoSavePayload? = pending.getAndSet(null)

    fun hasPending(): Boolean = pending.get() != null

    fun requestStop() {
        stopRequested.set(true)
    }

    fun shouldStop(): Boolean = stopRequested.get()

    suspend fun write(payload: AutoSavePayload) {
        payload.sram?.let { savesManager.setSaveRAM(payload.game, it) }
        payload.autoSave?.let { statesManager.setAutoSave(payload.game, payload.coreID, it) }
    }
}
