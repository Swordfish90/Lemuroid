package com.swordfish.lemuroid.app.mobile.feature.gamemenu.states

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.feature.gamemenu.GameMenuActivity
import com.swordfish.lemuroid.app.shared.gamemenu.GameMenuHelper
import com.swordfish.lemuroid.lib.saves.StatesManager
import com.swordfish.lemuroid.lib.saves.StatesPreviewManager
import kotlinx.coroutines.flow.flow

class GameMenuStatesViewModel(
    private val application: Application,
    private val gameMenuRequest: GameMenuActivity.GameMenuRequest,
    private val statesManager: StatesManager,
    private val disableMissingEntries: Boolean,
    private val statesPreviewManager: StatesPreviewManager,
) : ViewModel() {
    class Factory(
        private val application: Application,
        private val gameMenuRequest: GameMenuActivity.GameMenuRequest,
        private val statesManager: StatesManager,
        private val disableMissingEntries: Boolean,
        private val statesPreviewManager: StatesPreviewManager,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GameMenuStatesViewModel(
                application,
                gameMenuRequest,
                statesManager,
                disableMissingEntries,
                statesPreviewManager,
            ) as T
        }
    }

    data class StateEntry(
        val title: String,
        val description: String,
        val enabled: Boolean,
        val preview: Bitmap?,
    )

    data class State(val entries: List<StateEntry> = emptyList())

    val uiStates =
        flow {
            val slotsInfo = statesManager.getSavedSlotsInfo(gameMenuRequest.game, gameMenuRequest.coreConfig.coreID)

            val entries =
                slotsInfo.mapIndexed { index, slotInfo ->
                    val title =
                        application.applicationContext.getString(
                            R.string.game_menu_state,
                            (index + 1).toString(),
                        )
                    val description = GameMenuHelper.getSaveStateDescription(slotInfo)
                    val isEnabled = !disableMissingEntries || slotInfo.exists
                    val preview =
                        GameMenuHelper.getSaveStateBitmap(
                            application.applicationContext,
                            statesPreviewManager,
                            slotInfo,
                            gameMenuRequest.game,
                            gameMenuRequest.coreConfig.coreID,
                            index,
                        )

                    StateEntry(title, description, isEnabled, preview)
                }

            emit(State(entries))
        }
}
