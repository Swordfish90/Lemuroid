package com.swordfish.lemuroid.app.shared.game.viewmodel

import com.swordfish.touchinput.radial.sensors.TiltConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GameViewModelSideEffects(private val scope: CoroutineScope) {
    sealed interface UiEffect {
        data class ShowMenu(
            val currentTiltConfiguration: TiltConfiguration,
            val tiltConfigurations: List<TiltConfiguration>,
        ) : UiEffect

        data class ShowToast(val message: String) : UiEffect

        data object SuccessfulFinish : UiEffect

        data class FailureFinish(val message: String) : UiEffect

        data object LoadQuickSave : UiEffect

        data object SaveQuickSave : UiEffect

        data object ToggleFastForward : UiEffect
    }

    private val uiEffects = MutableSharedFlow<UiEffect>()

    fun getUiEffects(): Flow<UiEffect> {
        return uiEffects
    }

    fun showToast(message: String) {
        scope.launch {
            withContext(Dispatchers.Main) {
                uiEffects.emit(UiEffect.ShowToast(message))
            }
        }
    }

    fun showMenu(
        tilt: GameViewModelTilt,
        inputs: GameViewModelInput,
    ) {
        scope.launch {
            val currentTiltConfiguration = tilt.getTiltConfiguration().firstOrNull() ?: return@launch
            val tiltConfigurations = inputs.getAllTiltConfigurations()

            withContext(Dispatchers.Main) {
                uiEffects.emit(UiEffect.ShowMenu(currentTiltConfiguration, tiltConfigurations))
            }
        }
    }

    fun loadQuickSave() {
        scope.launch {
            withContext(Dispatchers.Main) {
                uiEffects.emit(UiEffect.LoadQuickSave)
            }
        }
    }

    fun requestSuccessfulFinish() {
        scope.launch {
            withContext(Dispatchers.Main) {
                uiEffects.emit(UiEffect.SuccessfulFinish)
            }
        }
    }

    fun requestFailureFinish(message: String) {
        scope.launch {
            withContext(Dispatchers.Main) {
                uiEffects.emit(UiEffect.FailureFinish(message))
            }
        }
    }

    fun saveQuickSave() {
        scope.launch {
            withContext(Dispatchers.Main) {
                uiEffects.emit(UiEffect.SaveQuickSave)
            }
        }
    }

    fun toggleFastForward() {
        scope.launch {
            withContext(Dispatchers.Main) {
                uiEffects.emit(UiEffect.ToggleFastForward)
            }
        }
    }
}
