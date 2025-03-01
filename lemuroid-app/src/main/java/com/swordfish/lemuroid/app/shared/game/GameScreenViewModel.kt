package com.swordfish.lemuroid.app.shared.game

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.PointF
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.unit.Density
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.swordfish.lemuroid.BuildConfig
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.feature.settings.SettingsManager
import com.swordfish.lemuroid.app.shared.input.InputDeviceManager
import com.swordfish.lemuroid.app.shared.input.InputKey
import com.swordfish.lemuroid.app.shared.input.inputclass.getInputClass
import com.swordfish.lemuroid.app.shared.rumble.RumbleManager
import com.swordfish.lemuroid.app.shared.settings.ControllerConfigsManager
import com.swordfish.lemuroid.app.shared.settings.HDModeQuality
import com.swordfish.lemuroid.app.shared.settings.HapticFeedbackMode
import com.swordfish.lemuroid.common.coroutines.MutableStateProperty
import com.swordfish.lemuroid.common.coroutines.launchOnState
import com.swordfish.lemuroid.common.coroutines.safeCollect
import com.swordfish.lemuroid.common.graphics.GraphicsUtils
import com.swordfish.lemuroid.common.graphics.takeScreenshot
import com.swordfish.lemuroid.common.kotlin.NTuple2
import com.swordfish.lemuroid.common.kotlin.NTuple4
import com.swordfish.lemuroid.common.kotlin.filterNotNullValues
import com.swordfish.lemuroid.common.kotlin.toIndexedMap
import com.swordfish.lemuroid.common.kotlin.zipOnKeys
import com.swordfish.lemuroid.common.longAnimationDuration
import com.swordfish.lemuroid.common.view.disableTouchEvents
import com.swordfish.lemuroid.lib.controller.ControllerConfig
import com.swordfish.lemuroid.lib.core.CoreVariable
import com.swordfish.lemuroid.lib.core.CoreVariablesManager
import com.swordfish.lemuroid.lib.game.GameLoader
import com.swordfish.lemuroid.lib.game.GameLoaderException
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.SystemCoreConfig
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.saves.IncompatibleStateException
import com.swordfish.lemuroid.lib.saves.SaveState
import com.swordfish.lemuroid.lib.saves.SavesManager
import com.swordfish.lemuroid.lib.saves.StatesManager
import com.swordfish.lemuroid.lib.saves.StatesPreviewManager
import com.swordfish.lemuroid.lib.storage.RomFiles
import com.swordfish.libretrodroid.Controller
import com.swordfish.libretrodroid.GLRetroView
import com.swordfish.libretrodroid.GLRetroView.Companion.MOTION_SOURCE_ANALOG_LEFT
import com.swordfish.libretrodroid.GLRetroView.Companion.MOTION_SOURCE_ANALOG_RIGHT
import com.swordfish.libretrodroid.GLRetroView.Companion.MOTION_SOURCE_DPAD
import com.swordfish.libretrodroid.GLRetroViewData
import com.swordfish.libretrodroid.Variable
import com.swordfish.libretrodroid.VirtualFile
import com.swordfish.touchinput.radial.LemuroidTouchConfigs
import com.swordfish.touchinput.radial.sensors.TiltConfiguration
import com.swordfish.touchinput.radial.sensors.TiltSensor
import com.swordfish.touchinput.radial.settings.TouchControllerID
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager
import gg.jam.jampadcompose.inputevents.InputEvent
import gg.jam.jampadcompose.inputstate.InputState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds

@OptIn(FlowPreview::class)
class GameScreenViewModel(
    private val appContext: Context,
    private val game: Game,
    private val settingsManager: SettingsManager,
    private val inputDeviceManager: InputDeviceManager,
    private val controllerConfigsManager: ControllerConfigsManager,
    private val system: GameSystem,
    private val systemCoreConfig: SystemCoreConfig,
    sharedPreferences: SharedPreferences,
    private val statesManager: StatesManager,
    private val statesPreviewManager: StatesPreviewManager,
    private val legacySavesManager: SavesManager,
    private val coreVariablesManager: CoreVariablesManager,
    private val rumbleManager: RumbleManager,
) : ViewModel(), DefaultLifecycleObserver {

    class Factory(
        private val appContext: Context,
        private val game: Game,
        private val settingsManager: SettingsManager,
        private val inputDeviceManager: InputDeviceManager,
        private val controllerConfigsManager: ControllerConfigsManager,
        private val system: GameSystem,
        private val systemCoreConfig: SystemCoreConfig,
        private val sharedPreferences: SharedPreferences,
        private val statesManager: StatesManager,
        private val statesPreviewManager: StatesPreviewManager,
        private val legacySavesManager: SavesManager,
        private val coreVariablesManager: CoreVariablesManager,
        private val rumbleManager: RumbleManager,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GameScreenViewModel(
                appContext,
                game,
                settingsManager,
                inputDeviceManager,
                controllerConfigsManager,
                system,
                systemCoreConfig,
                sharedPreferences,
                statesManager,
                statesPreviewManager,
                legacySavesManager,
                coreVariablesManager,
                rumbleManager,
            ) as T
        }
    }

    data class SingleAxisEvent(val axis: Int, val action: Int, val keyCode: Int, val port: Int)

    val retroGameViewFlow = MutableStateFlow<GLRetroView?>(null)
    var retroGameView: GLRetroView? by MutableStateProperty(retroGameViewFlow)

    private val keyEventsFlow: MutableSharedFlow<KeyEvent?> = MutableSharedFlow()
    private val motionEventsFlow: MutableSharedFlow<MotionEvent> = MutableSharedFlow()

    private val tiltSensor = TiltSensor(appContext)
    private var loadingMenuJob: Job? = null

    private val controllerConfigsState = MutableStateFlow<Map<Int, ControllerConfig>>(mapOf())
    private val touchControllerSettingsManager = TouchControllerSettingsManager(sharedPreferences)

    private val uiState = MutableStateFlow(UiState.Loading("") as UiState)
    private val uiEffects = MutableSharedFlow<UiEffect>()

    private val touchControlId = MutableStateFlow<TouchControllerID>(TouchControllerID.PSX)
    private val screenOrientation = MutableStateFlow(TouchControllerSettingsManager.Orientation.PORTRAIT)
    val loadingState = MutableStateFlow(false)

    private val tiltConfiguration = MutableStateFlow<TiltConfiguration>(TiltConfiguration.Disabled)

    private inline fun withLoading(block: () -> Unit) {
        loadingState.value = true
        block()
        loadingState.value = false
    }

    fun getUiState(): Flow<UiState> {
        return uiState
    }

    fun getUiEffects(): Flow<UiEffect> {
        return uiEffects
    }

    fun getTiltConfiguration(): Flow<TiltConfiguration> {
        return tiltConfiguration
    }

    fun getSimulatedTiltEvents(): Flow<InputState> {
        return tiltConfiguration
            .flatMapLatest { config ->
                if (config is TiltConfiguration.Disabled) {
                    return@flatMapLatest flow { awaitCancellation() }
                }

                tiltSensor.setSensitivity(settingsManager.tiltSensitivity())
                tiltSensor.getTiltEvents()
                    .onStart { tiltSensor.shouldRun = true }
                    .onCompletion { tiltSensor.shouldRun = false }
                    .map { config.process(it) }
                    .distinctUntilChanged()
            }
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
            val gameData: GameLoader.GameData, // TODO FILIPPO... Get rid of this, lot of useless memory for the state.
            val retroViewData: GLRetroViewData,
            val menuPressed: Boolean,
            val showEditControls: Boolean,
            val hapticFeedbackMode: HapticFeedbackMode,
        ) : UiState
    }

    fun createRetroView(
        context: Context,
        lifecycle: LifecycleOwner,
        data: GLRetroViewData,
        gameData: GameLoader.GameData,
    ): GLRetroView {
        val result = GLRetroView(context, data)
            .apply {
                isFocusable = false
                isFocusableInTouchMode = false
            }

        if (!system.hasTouchScreen) {
            result.disableTouchEvents()
        }

        lifecycle.lifecycle.addObserver(result)

        viewModelScope.launch {
            gameData.quickSaveData?.let {
                restoreAutoSaveAsync(it)
            }
        }

        if (BuildConfig.DEBUG) {
            runCatching {
                printRetroVariables(result)
            }
        }

        retroGameViewFlow.value = result

        return result
    }

    sealed interface UiEffect {
        data class ShowMenu(
            val currentTiltConfiguration: TiltConfiguration,
            val tiltConfigurations: List<TiltConfiguration>
        ) : UiEffect

        data class ShowToast(val message: String) : UiEffect
        data object Finish : UiEffect
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
        val hapticFeedbackMode = HapticFeedbackMode.parse(settingsManager.hapticFeedbackMode())

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
                    val retroViewData = buildRetroViewData(
                        systemCoreConfig,
                        loadingState.gameData,
                        hdMode,
                        hdModeQuality,
                        filter,
                        lowLatencyAudio,
                        enableRumble,
                        enableMicrophone,
                    )
                    UiState.Running(
                        gameData = loadingState.gameData,
                        retroViewData = retroViewData,
                        menuPressed = false,
                        showEditControls = false,
                        hapticFeedbackMode = hapticFeedbackMode,
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
                if (loadingState.value) return@launch
                showMenu()
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

    fun getTouchControllerConfig(): Flow<ControllerConfig> {
        return controllerConfigsState
            .map { it[0] }
            .filterNotNull()
            .distinctUntilChanged()
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        tiltSensor.isAllowedToRun = true
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        tiltSensor.isAllowedToRun = false
    }

    fun changeTiltConfiguration(tiltConfig: TiltConfiguration) {
        tiltConfiguration.value = tiltConfig
    }

    suspend fun saveSlot(index: Int) {
        if (loadingState.value) return
        withLoading {
            getCurrentSaveState()?.let {
                statesManager.setSlotSave(game, it, systemCoreConfig.coreID, index)
                runCatching {
                    takeScreenshotPreview(index)
                }
            }
        }
    }

    suspend fun loadSlot(index: Int) {
        if (loadingState.value) return
        withLoading {
            try {
                statesManager.getSlotSave(game, systemCoreConfig.coreID, index)?.let {
                    val loaded =
                        withContext(Dispatchers.IO) {
                            loadSaveState(it)
                        }
                    withContext(Dispatchers.Main) {
                        if (!loaded) {
                            uiEffects.emit(UiEffect.ShowToast(appContext.getString(R.string.game_toast_load_state_failed)))
                        }
                    }
                }
            } catch (e: Throwable) {
                val errorMessageId = when (e) {
                    is IncompatibleStateException -> R.string.error_message_incompatible_state
                    else -> R.string.game_toast_load_state_failed
                }
                viewModelScope.launch {
                    uiEffects.emit(UiEffect.ShowToast(appContext.getString(errorMessageId)))
                }
            }
        }
    }

    suspend fun reset() =
        withLoading {
            try {
                delay(appContext.longAnimationDuration().toLong())
                retroGameViewFlow().reset()
            } catch (e: Throwable) {
                Timber.e(e, "Error in reset")
            }
        }

    private fun getCurrentSaveState(): SaveState? {
        val retroGameView = retroGameView ?: return null
        val currentDisk =
            if (system.hasMultiDiskSupport) {
                retroGameView.getCurrentDisk()
            } else {
                0
            }
        return SaveState(
            retroGameView.serializeState(),
            SaveState.Metadata(currentDisk, systemCoreConfig.statesVersion),
        )
    }

    private suspend fun takeScreenshotPreview(index: Int) {
        val sizeInDp = StatesPreviewManager.PREVIEW_SIZE_DP
        val previewSize = GraphicsUtils.convertDpToPixel(sizeInDp, appContext).roundToInt()
        val preview = retroGameView?.takeScreenshot(previewSize, 3)
        if (preview != null) {
            statesPreviewManager.setPreviewForSlot(game, preview, systemCoreConfig.coreID, index)
        }
    }

    private fun loadSaveState(saveState: SaveState): Boolean {
        val retroGameView = retroGameView ?: return false

        if (systemCoreConfig.statesVersion != saveState.metadata.version) {
            throw IncompatibleStateException()
        }

        if (system.hasMultiDiskSupport &&
            retroGameView.getAvailableDisks() > 1 &&
            retroGameView.getCurrentDisk() != saveState.metadata.diskIndex
        ) {
            retroGameView.changeDisk(saveState.metadata.diskIndex)
        }

        return retroGameView.unserializeState(saveState.state)
    }

    // On some cores unserialize fails with no reason. So we need to try multiple times.
    private suspend fun restoreAutoSaveAsync(saveState: SaveState) {
        // PPSSPP and Mupen64 initialize some state while rendering the first frame, so we have to wait before restoring
        // the autosave. Do not change thread here. Stick to the GL one to avoid issues with PPSSPP.
        if (!isAutoSaveEnabled()) return

        try {
            waitGLEvent<GLRetroView.GLRetroEvents.FrameRendered>()
            restoreQuickSave(saveState)
        } catch (e: Throwable) {
            Timber.e(e, "Error while loading auto-save")
        }
    }

    // Now that we wait for the first rendered frame this is probably no longer needed, but we'll keep it just to be sure
    private suspend fun restoreQuickSave(saveState: SaveState) {
        var times = 10

        while (!loadSaveState(saveState) && times > 0) {
            delay(200)
            times--
        }
    }

    private suspend inline fun <reified T> waitGLEvent() {
        val retroView = retroGameViewFlow()
        retroView.getGLRetroEvents()
            .filterIsInstance<T>()
            .first()
    }

    suspend fun retroGameViewFlow() = retroGameViewFlow
        .filterNotNull()
        .first()

    private suspend fun isAutoSaveEnabled(): Boolean {
        return systemCoreConfig.statesSupported && settingsManager.autoSave()
    }

    fun requestFinish() {
        if (loadingState.value) return
        viewModelScope.launch {
            autoSaveAndFinish()
        }
    }

    private suspend fun autoSaveAndFinish() {
        withLoading {
            saveSRAM(game)
            saveAutoSave(game)

            // TODO FILIPPO... Does this stay inside or outside the loading.
            uiEffects.emit(UiEffect.Finish)
        }
    }

    private suspend fun saveAutoSave(game: Game) {
        if (!isAutoSaveEnabled()) return
        val state = getCurrentSaveState()

        if (state != null) {
            statesManager.setAutoSave(game, systemCoreConfig.coreID, state)
            Timber.i("Stored autosave file with size: ${state?.state?.size}")
        }
    }

    private suspend fun saveSRAM(game: Game) {
        val retroGameView = retroGameView ?: return
        val sramState = retroGameView.serializeSRAM()
        legacySavesManager.setSaveRAM(game, sramState)
        Timber.i("Stored sram file with size: ${sramState.size}")
    }

    fun handleVirtualInputEvent(events: List<InputEvent>) {
        val menuEvent = events.firstOrNull { it is InputEvent.Button && it.id == KeyEvent.KEYCODE_BUTTON_MODE }
        if (menuEvent != null) {
            onMenuPressed((menuEvent as InputEvent.Button).pressed)
        }

        events.forEach { event ->
            when (event) {
                is InputEvent.Button -> {
                    handleVirtualInputButton(event)
                }

                is InputEvent.DiscreteDirection -> {
                    handleVirtualInputDirection(event.id, event.direction.x, -event.direction.y)
                }

                is InputEvent.ContinuousDirection -> {
                    handleVirtualInputDirection(event.id, event.direction.x, -event.direction.y)
                }
            }
        }
    }

    private fun handleVirtualInputButton(event: InputEvent.Button) {
        val action = if (event.pressed) KeyEvent.ACTION_DOWN else KeyEvent.ACTION_UP
        retroGameView?.sendKeyEvent(action, event.id)
    }

    private fun handleVirtualInputDirection(id: Int, xAxis: Float, yAxis: Float) {
        when (id) {
            LemuroidTouchConfigs.MOTION_SOURCE_DPAD -> {
                retroGameView?.sendMotionEvent(GLRetroView.MOTION_SOURCE_DPAD, xAxis, yAxis)
            }

            LemuroidTouchConfigs.MOTION_SOURCE_LEFT_STICK -> {
                retroGameView?.sendMotionEvent(
                    MOTION_SOURCE_ANALOG_LEFT,
                    xAxis,
                    yAxis,
                )
            }

            LemuroidTouchConfigs.MOTION_SOURCE_RIGHT_STICK -> {
                retroGameView?.sendMotionEvent(
                    MOTION_SOURCE_ANALOG_RIGHT,
                    xAxis,
                    yAxis,
                )
            }

            LemuroidTouchConfigs.MOTION_SOURCE_DPAD_AND_LEFT_STICK -> {
                retroGameView?.sendMotionEvent(
                    MOTION_SOURCE_ANALOG_LEFT,
                    xAxis,
                    yAxis,
                )
                retroGameView?.sendMotionEvent(MOTION_SOURCE_DPAD, xAxis, yAxis)
            }

            LemuroidTouchConfigs.MOTION_SOURCE_RIGHT_DPAD -> {
                retroGameView?.sendMotionEvent(
                    MOTION_SOURCE_ANALOG_RIGHT,
                    xAxis,
                    yAxis,
                )
            }
        }
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)

        owner.launchOnState(Lifecycle.State.CREATED) {
            initializeControllerConfigsFlow()
        }

        owner.launchOnState(Lifecycle.State.CREATED) {
            initializeGamePadShortcutsFlow()
        }

        owner.launchOnState(Lifecycle.State.CREATED) {
            initializeGamePadKeysFlow()
        }

        owner.launchOnState(Lifecycle.State.CREATED) {
            initializeVirtualGamePadMotionsFlow()
        }

        owner.launchOnState(Lifecycle.State.CREATED) {
            initializeGamePadMotionsFlow()
        }

        owner.launchOnState(Lifecycle.State.RESUMED) {
            initializeCoreVariablesFlow()
        }

        owner.launchOnState(Lifecycle.State.RESUMED) {
            initializeControllersConfigFlow()
        }

        owner.launchOnState(Lifecycle.State.RESUMED) {
            initializeRumbleFlow()
        }
    }

    private suspend fun initializeControllerConfigsFlow() {
        waitGLEvent<GLRetroView.GLRetroEvents.FrameRendered>()
        controllerConfigsState.safeCollect {
            updateControllers(it)
        }
    }

    private suspend fun initializeGamePadShortcutsFlow() {
        inputDeviceManager.getInputMenuShortCutObservable()
            .distinctUntilChanged()
            .safeCollect { shortcut ->
                shortcut?.let {
                    val message = appContext.resources
                        .getString(R.string.game_toast_settings_button_using_gamepad, it.name)

                    uiEffects.emit(UiEffect.ShowToast(message))
                }
            }
    }

    private fun updateControllers(controllers: Map<Int, ControllerConfig>) {
        retroGameView
            ?.getControllers()?.toIndexedMap()
            ?.zipOnKeys(controllers, this::findControllerId)
            ?.filterNotNullValues()
            ?.forEach { (port, controllerId) ->
                Timber.i("Controls setting $port to $controllerId")
                retroGameView?.setControllerType(port, controllerId)
            }
    }

    private fun findControllerId(
        supported: Array<Controller>,
        controllerConfig: ControllerConfig,
    ): Int? {
        return supported
            .firstOrNull { controller ->
                sequenceOf(
                    controller.id == controllerConfig.libretroId,
                    controller.description == controllerConfig.libretroDescriptor,
                ).any { it }
            }?.id
    }

    private suspend fun initializeGamePadKeysFlow() {
        val pressedKeys = mutableSetOf<Int>()

        val filteredKeyEvents =
            keyEventsFlow
                .filterNotNull()
                .filter { it.repeatCount == 0 }
                .map { Triple(it.device, it.action, it.keyCode) }
                .distinctUntilChanged()

        val shortcutKeys =
            inputDeviceManager.getInputMenuShortCutObservable()
                .map { it?.keys ?: setOf() }

        val combinedObservable =
            combine(
                shortcutKeys,
                inputDeviceManager.getGamePadsPortMapperObservable(),
                inputDeviceManager.getInputBindingsObservable(),
                filteredKeyEvents,
                ::NTuple4,
            )

        combinedObservable
            .onStart { pressedKeys.clear() }
            .onCompletion { pressedKeys.clear() }
            .safeCollect { (shortcut, ports, bindings, event) ->
                val (device, action, keyCode) = event
                val port = ports(device)
                val bindKeyCode = bindings(device)[InputKey(keyCode)]?.keyCode ?: keyCode

                if (bindKeyCode == KeyEvent.KEYCODE_BACK && action == KeyEvent.ACTION_DOWN) {
                    requestFinish()
                    return@safeCollect
                }

                if (port == 0) {
                    if (bindKeyCode == KeyEvent.KEYCODE_BUTTON_MODE && action == KeyEvent.ACTION_DOWN) {
                        showMenu()
                        return@safeCollect
                    }

                    if (action == KeyEvent.ACTION_DOWN) {
                        pressedKeys.add(keyCode)
                    } else if (action == KeyEvent.ACTION_UP) {
                        pressedKeys.remove(keyCode)
                    }

                    if (shortcut.isNotEmpty() && pressedKeys.containsAll(shortcut)) {
                        showMenu()
                        return@safeCollect
                    }
                }

                port?.let {
                    retroGameView?.sendKeyEvent(action, bindKeyCode, it)
                }
            }
    }

    private suspend fun initializeVirtualGamePadMotionsFlow() {
        val events =
            combine(
                inputDeviceManager.getGamePadsPortMapperObservable(),
                motionEventsFlow,
                ::NTuple2,
            )

        events
            .mapNotNull { (ports, event) ->
                ports(event.device)?.let { it to event }
            }
            .map { (port, event) ->
                val axes = event.device.getInputClass().getAxesMap().entries

                axes.map { (axis, button) ->
                    val action =
                        if (event.getAxisValue(axis) > 0.5) {
                            KeyEvent.ACTION_DOWN
                        } else {
                            KeyEvent.ACTION_UP
                        }
                    SingleAxisEvent(axis, action, button, port)
                }.toSet()
            }
            .scan(emptySet<SingleAxisEvent>()) { prev, next ->
                next.minus(prev).forEach {
                    retroGameView?.sendKeyEvent(it.action, it.keyCode, it.port)
                }
                next
            }
            .safeCollect { }
    }

    private suspend fun initializeGamePadMotionsFlow() {
        val events =
            combine(
                inputDeviceManager.getGamePadsPortMapperObservable(),
                motionEventsFlow,
                ::NTuple2,
            )

        events
            .safeCollect { (ports, event) ->
                ports(event.device)?.let {
                    sendStickMotions(event, it)
                }
            }
    }

    private fun sendStickMotions(
        event: MotionEvent,
        port: Int,
    ) {
        if (port < 0) return
        when (event.source) {
            InputDevice.SOURCE_JOYSTICK -> {
                if (controllerConfigsState.value[port]?.mergeDPADAndLeftStickEvents == true) {
                    sendMergedMotionEvents(event, port)
                } else {
                    sendSeparateMotionEvents(event, port)
                }
            }
        }
    }

    private fun sendMergedMotionEvents(
        event: MotionEvent,
        port: Int,
    ) {
        val events =
            listOf(
                retrieveCoordinates(event, MotionEvent.AXIS_HAT_X, MotionEvent.AXIS_HAT_Y),
                retrieveCoordinates(event, MotionEvent.AXIS_X, MotionEvent.AXIS_Y),
            )

        val xVal = events.maxByOrNull { abs(it.x) }?.x ?: 0f
        val yVal = events.maxByOrNull { abs(it.y) }?.y ?: 0f

        retroGameView?.sendMotionEvent(MOTION_SOURCE_DPAD, xVal, yVal, port)
        retroGameView?.sendMotionEvent(MOTION_SOURCE_ANALOG_LEFT, xVal, yVal, port)

        sendStickMotion(
            event,
            MOTION_SOURCE_ANALOG_RIGHT,
            MotionEvent.AXIS_Z,
            MotionEvent.AXIS_RZ,
            port,
        )
    }

    private fun sendStickMotion(event: MotionEvent, source: Int, xAxis: Int, yAxis: Int, port: Int) {
        val coords = retrieveCoordinates(event, xAxis, yAxis)
        retroGameView?.sendMotionEvent(source, coords.x, coords.y, port)
    }

    private fun sendDPADMotion(event: MotionEvent, source: Int, xAxis: Int, yAxis: Int, port: Int) {
        retroGameView?.sendMotionEvent(
            source,
            event.getAxisValue(xAxis),
            event.getAxisValue(yAxis),
            port
        )
    }

    private fun sendSeparateMotionEvents(event: MotionEvent, port: Int) {
        sendDPADMotion(
            event,
            MOTION_SOURCE_DPAD,
            MotionEvent.AXIS_HAT_X,
            MotionEvent.AXIS_HAT_Y,
            port,
        )
        sendStickMotion(
            event,
            MOTION_SOURCE_ANALOG_LEFT,
            MotionEvent.AXIS_X,
            MotionEvent.AXIS_Y,
            port,
        )
        sendStickMotion(
            event,
            MOTION_SOURCE_ANALOG_RIGHT,
            MotionEvent.AXIS_Z,
            MotionEvent.AXIS_RZ,
            port,
        )
    }

    private fun retrieveCoordinates(event: MotionEvent, xAxis: Int, yAxis: Int): PointF {
        return PointF(event.getAxisValue(xAxis), event.getAxisValue(yAxis))
    }

    fun sendKeyEvent(keyCode: Int, event: KeyEvent): Boolean {
        if (InputKey(keyCode) in event.device.getInputClass().getInputKeys()) {
            viewModelScope.launch {
                keyEventsFlow.emit(event)
            }
            return true
        }
        return false
    }

    fun sendMotionEvent(event: MotionEvent): Boolean {
        viewModelScope.launch {
            motionEventsFlow.emit(event)
        }
        return true
    }

    private suspend fun initializeCoreVariablesFlow() {
        try {
            waitRetroGameViewInitialized()
            val options = coreVariablesManager.getOptionsForCore(system.id, systemCoreConfig)
            updateCoreVariables(options)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun updateCoreVariables(options: List<CoreVariable>) {
        val updatedVariables =
            options.map { Variable(it.key, it.value) }
                .toTypedArray()

        updatedVariables.forEach {
            Timber.i("Updating core variable: ${it.key} ${it.value}")
        }

        retroGameView?.updateVariables(*updatedVariables)
    }

    private suspend fun waitRetroGameViewInitialized() {
        retroGameViewFlow()
    }

    private suspend fun initializeControllersConfigFlow() {
        try {
            waitRetroGameViewInitialized()
            val controllers = controllerConfigsManager.getControllerConfigs(system.id, systemCoreConfig)
            controllerConfigsState.value = controllers
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun showMenu() {
        val tiltConfiguration = tiltConfiguration.value
        val tiltConfigurations = controllerConfigsState.value[0]
            ?.tiltConfigurations
            ?: emptyList()

        viewModelScope.launch {
            val effect = UiEffect.ShowMenu(
                tiltConfigurations = tiltConfigurations,
                currentTiltConfiguration = tiltConfiguration
            )
            uiEffects.emit(effect)
        }
    }

    private suspend fun initializeRumbleFlow() {
        val retroGameView = retroGameViewFlow()
        val rumbleEvents = retroGameView.getRumbleEvents()
        rumbleManager.collectAndProcessRumbleEvents(systemCoreConfig, rumbleEvents)
    }

    private fun printRetroVariables(retroGameView: GLRetroView) {
        viewModelScope.launch {
            // Some cores do not immediately call SET_VARIABLES so we might need to wait a little bit
            delay(1.seconds)
            retroGameView.getVariables().forEach {
                Timber.i("Libretro variable: $it")
            }
        }
    }

    companion object {
        const val MENU_LOADING_ANIMATION_MILLIS = 500
    }
}
