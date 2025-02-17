package com.swordfish.lemuroid.app.shared.game

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.unit.Density
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.feature.settings.SettingsManager
import com.swordfish.lemuroid.app.shared.input.InputDeviceManager
import com.swordfish.lemuroid.app.shared.settings.ControllerConfigsManager
import com.swordfish.lemuroid.app.shared.settings.HDModeQuality
import com.swordfish.lemuroid.lib.game.GameLoader
import com.swordfish.lemuroid.lib.game.GameLoaderException
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.SystemCoreConfig
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.storage.RomFiles
import com.swordfish.libretrodroid.GLRetroViewData
import com.swordfish.libretrodroid.Variable
import com.swordfish.libretrodroid.VirtualFile
import com.swordfish.touchinput.radial.settings.TouchControllerID
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


@OptIn(FlowPreview::class)
class GameScreenViewModel(
    private val appContext: Context,
    private val settingsManager: SettingsManager,
    private val inputDeviceManager: InputDeviceManager,
    sharedPreferences: SharedPreferences,
) : ViewModel() {

    class Factory(
        private val appContext: Context,
        private val settingsManager: SettingsManager,
        private val inputDeviceManager: InputDeviceManager,
        private val sharedPreferences: SharedPreferences,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GameScreenViewModel(
                appContext,
                settingsManager,
                inputDeviceManager,
                sharedPreferences,
            ) as T
        }
    }

    private val touchControllerSettingsManager = TouchControllerSettingsManager(sharedPreferences)

    private val uiState = MutableStateFlow(UiState.Loading("") as UiState)
    private val uiEffects = MutableSharedFlow<UiEffect>()

    private val touchControlId = MutableStateFlow<TouchControllerID>(TouchControllerID.PSX)
    private val screenOrientation =
        MutableStateFlow<TouchControllerSettingsManager.Orientation>(TouchControllerSettingsManager.Orientation.PORTRAIT)

    private var loadingMenuJob: Job? = null

    fun getUiState(): Flow<UiState> {
        return uiState
    }

    fun getUiEffects(): Flow<UiEffect> {
        return uiEffects
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getTouchControlsSettings(density: Density, insets: WindowInsets): Flow<TouchControllerSettingsManager.Settings?> {
        return combine(
            touchControlId,
            screenOrientation
        ) { touchControlId, orientation -> touchControlId to orientation }
            .flatMapLatest { (touchControlId, orientation) ->
                touchControllerSettingsManager.observeSettings(touchControlId, orientation, density, insets)
            }
    }

    sealed interface UiState {
        data class Loading(val message: String) : UiState
        data class Error(val throwable: Throwable) : UiState
        data class Running(
            val gameData: GameLoader.GameData,
            val gameViewData: GLRetroViewData,
            val menuPressed: Boolean,
            val showEditControls: Boolean,
        ) : UiState
    }

    sealed interface UiEffect {
        object ShowMenu : UiEffect
    }

    suspend fun loadGame(
        applicationContext: Context,
        game: Game,
        systemCoreConfig: SystemCoreConfig,
        gameLoader: GameLoader,
        requestLoadSave: Boolean
    ) {
        val autoSaveEnabled = settingsManager.autoSave()
        val filter = settingsManager.screenFilter()
        val hdMode = settingsManager.hdMode()
        val hdModeQuality = settingsManager.hdModeQuality()
        val lowLatencyAudio = settingsManager.lowLatencyAudio()
        val enableRumble = settingsManager.enableRumble()
        val directLoad = settingsManager.allowDirectGameLoad()

        val hasMicrophonePermission = ContextCompat.checkSelfPermission(
            applicationContext,
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        val enableMicrophone = systemCoreConfig.supportsMicrophone && hasMicrophonePermission

        val loadingStatesFlow =
            gameLoader.load(
                applicationContext,
                game,
                requestLoadSave && autoSaveEnabled,
                systemCoreConfig,
                directLoad,
            )

        // TODO FILIPPO... Maybe it's simpler to return a flow instead of the mutable flow here.
        loadingStatesFlow
            .flowOn(Dispatchers.IO)
            .catch {
                uiState.value = UiState.Error(it as GameLoaderException)
            }
            .debounce(200)
            .collect { loadingState ->
                uiState.value = if (loadingState is GameLoader.LoadingState.Ready) {
                    UiState.Running(
                        menuPressed = false,
                        showEditControls = false,
                        gameData = loadingState.gameData,
                        gameViewData = buildRetroViewData(
                            systemCoreConfig,
                            loadingState.gameData,
                            hdMode,
                            hdModeQuality,
                            filter,
                            lowLatencyAudio, enableRumble, enableMicrophone,
                        )
                    )
                } else {
                    UiState.Loading(getLoadingMessage(loadingState))
                }
            }
    }

    private fun getLoadingMessage(loadingState: GameLoader.LoadingState): String {
        return when (loadingState) {
            is GameLoader.LoadingState.LoadingCore ->
                appContext.getString(
                    com.swordfish.lemuroid.ext.R.string.game_loading_download_core,
                )

            is GameLoader.LoadingState.LoadingGame ->
                appContext.getString(
                    R.string.game_loading_preparing_game
                )

            else -> ""
        }
    }

    private fun buildRetroViewData(
        systemCoreConfig: SystemCoreConfig,
        gameData: GameLoader.GameData,
        hdMode: Boolean,
        hdModeQuality: HDModeQuality,
        screenFilter: String,
        lowLatencyAudio: Boolean,
        requestRumble: Boolean,
        requestMicrophone: Boolean
    ): GLRetroViewData {
        return GLRetroViewData(appContext).apply {
            coreFilePath = gameData.coreLibrary

            when (val gameFiles = gameData.gameFiles) {
                is RomFiles.Standard -> {
                    gameFilePath = gameFiles.files.first().absolutePath
                }

                is RomFiles.Virtual -> {
                    gameVirtualFiles = gameFiles.files.map { VirtualFile(it.filePath, it.fd) }
                }
            }

            systemDirectory = gameData.systemDirectory.absolutePath
            savesDirectory = gameData.savesDirectory.absolutePath
            variables = gameData.coreVariables.map { Variable(it.key, it.value) }.toTypedArray()
            saveRAMState = gameData.saveRAMData
            shader =
                ShaderChooser.getShaderForSystem(
                    appContext,
                    hdMode,
                    hdModeQuality,
                    screenFilter,
                    GameSystem.findById(gameData.game.systemId),
                )
            preferLowLatencyAudio = lowLatencyAudio
            rumbleEventsEnabled = requestRumble
            skipDuplicateFrames = systemCoreConfig.skipDuplicateFrames
            enableMicrophone = requestMicrophone
        }
    }

    fun onMenuPressed(pressed: Boolean) {
        val currentState = uiState.value
        if (currentState !is UiState.Running) {
            return
        }

        uiState.value = currentState.copy(menuPressed = pressed)

        if (pressed) {
            loadingMenuJob?.cancel()
            loadingMenuJob = viewModelScope.launch {
                delay(MENU_LOADING_ANIMATION_MILLIS.toLong())
                uiEffects.emit(UiEffect.ShowMenu)
            }
        } else {
            loadingMenuJob?.cancel()
            loadingMenuJob = null
        }
    }

    fun showEditControls(show: Boolean) {
        val currentState = uiState.value
        if (currentState !is UiState.Running) {
            return
        }

        uiState.value = currentState.copy(showEditControls = show)
    }

    fun updateTouchControllerSettings(touchControllerSettings: TouchControllerSettingsManager.Settings) {
        val currentState = uiState.value
        if (currentState !is UiState.Running) {
            return
        }

        viewModelScope.launch {
            touchControllerSettingsManager.storeSettings(
                touchControlId.value,
                screenOrientation.value,
                touchControllerSettings
            )
        }
    }

    fun resetTouchControls() {
        val currentState = uiState.value
        if (currentState !is UiState.Running) {
            return
        }

        viewModelScope.launch {
            touchControllerSettingsManager.resetSettings(
                touchControlId.value,
                screenOrientation.value,
            )
        }
    }

    fun onScreenOrientationChanged(orientation: TouchControllerSettingsManager.Orientation) {
        screenOrientation.value = orientation
    }

    fun isTouchControllerVisible(): Flow<Boolean> {
        return inputDeviceManager
            .getEnabledInputsObservable()
            .map { it.isEmpty() }
    }

    companion object {
        const val MENU_LOADING_ANIMATION_MILLIS = 500
    }
}
