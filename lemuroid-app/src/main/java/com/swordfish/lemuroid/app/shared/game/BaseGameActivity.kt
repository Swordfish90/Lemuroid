package com.swordfish.lemuroid.app.shared.game

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PointF
import android.os.Bundle
import android.view.Gravity
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.gojuno.koptional.None
import com.gojuno.koptional.Optional
import com.gojuno.koptional.rxjava2.filterSome
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import com.swordfish.lemuroid.BuildConfig
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.feature.game.GameActivity
import com.swordfish.lemuroid.app.mobile.feature.settings.RxSettingsManager
import com.swordfish.lemuroid.app.shared.GameMenuContract
import com.swordfish.lemuroid.app.shared.ImmersiveActivity
import com.swordfish.lemuroid.app.shared.rumble.RumbleManager
import com.swordfish.lemuroid.app.shared.coreoptions.CoreOption
import com.swordfish.lemuroid.app.shared.coreoptions.LemuroidCoreOption
import com.swordfish.lemuroid.app.shared.savesync.SaveSyncWork
import com.swordfish.lemuroid.app.shared.settings.ControllerConfigsManager
import com.swordfish.lemuroid.app.shared.input.InputDeviceManager
import com.swordfish.lemuroid.app.shared.input.getInputClass
import com.swordfish.lemuroid.app.tv.game.TVGameActivity
import com.swordfish.lemuroid.common.animationDuration
import com.swordfish.lemuroid.common.displayToast
import com.swordfish.lemuroid.common.dump
import com.swordfish.lemuroid.common.graphics.GraphicsUtils
import com.swordfish.lemuroid.common.kotlin.NTuple4
import com.swordfish.lemuroid.common.rx.BehaviorRelayNullableProperty
import com.swordfish.lemuroid.common.rx.BehaviorRelayProperty
import com.swordfish.lemuroid.common.rx.RXUtils
import com.swordfish.lemuroid.lib.controller.ControllerConfig
import com.swordfish.lemuroid.lib.core.CoreVariable
import com.swordfish.lemuroid.lib.core.CoreVariablesManager
import com.swordfish.lemuroid.lib.game.GameLoader
import com.swordfish.lemuroid.lib.game.GameLoaderError
import com.swordfish.lemuroid.lib.game.GameLoaderException
import com.swordfish.lemuroid.lib.library.ExposedSetting
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.SystemCoreConfig
import com.swordfish.lemuroid.lib.library.SystemID
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.saves.IncompatibleStateException
import com.swordfish.lemuroid.lib.saves.SaveState
import com.swordfish.lemuroid.lib.saves.SavesManager
import com.swordfish.lemuroid.lib.saves.StatesManager
import com.swordfish.lemuroid.lib.saves.StatesPreviewManager
import com.swordfish.lemuroid.lib.storage.RomFiles
import com.swordfish.lemuroid.app.shared.storage.cache.CacheCleanerWork
import com.swordfish.lemuroid.common.graphics.takeScreenshot
import com.swordfish.lemuroid.common.kotlin.NTuple5
import com.swordfish.lemuroid.common.kotlin.filterNotNullValues
import com.swordfish.lemuroid.common.kotlin.toIndexedMap
import com.swordfish.lemuroid.common.kotlin.zipOnKeys
import com.swordfish.lemuroid.common.view.setVisibleOrGone
import com.swordfish.lemuroid.lib.util.subscribeBy
import com.swordfish.libretrodroid.Controller
import com.swordfish.libretrodroid.GLRetroView
import com.swordfish.libretrodroid.GLRetroView.Companion.MOTION_SOURCE_ANALOG_LEFT
import com.swordfish.libretrodroid.GLRetroView.Companion.MOTION_SOURCE_ANALOG_RIGHT
import com.swordfish.libretrodroid.GLRetroView.Companion.MOTION_SOURCE_DPAD
import com.swordfish.libretrodroid.GLRetroViewData
import com.swordfish.libretrodroid.Variable
import com.swordfish.libretrodroid.VirtualFile
import com.swordfish.radialgamepad.library.math.MathUtils
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.Singles
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Thread.sleep
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.system.exitProcess

abstract class BaseGameActivity : ImmersiveActivity() {

    protected lateinit var game: Game
    protected lateinit var system: GameSystem
    protected lateinit var systemCoreConfig: SystemCoreConfig
    protected lateinit var mainContainerLayout: ConstraintLayout
    private lateinit var gameContainerLayout: FrameLayout
    protected lateinit var leftGamePadContainer: FrameLayout
    protected lateinit var rightGamePadContainer: FrameLayout
    private lateinit var loadingView: ProgressBar
    private lateinit var loadingMessageView: TextView

    @Inject lateinit var settingsManager: RxSettingsManager
    @Inject lateinit var statesManager: StatesManager
    @Inject lateinit var statesPreviewManager: StatesPreviewManager
    @Inject lateinit var savesManager: SavesManager
    @Inject lateinit var coreVariablesManager: CoreVariablesManager
    @Inject lateinit var inputDeviceManager: InputDeviceManager
    @Inject lateinit var gameLoader: GameLoader
    @Inject lateinit var controllerConfigsManager: ControllerConfigsManager
    @Inject lateinit var rumbleManager: RumbleManager

    private var defaultExceptionHandler: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()

    private val startGameTime = System.currentTimeMillis()

    private val keyEventsSubjects: PublishRelay<KeyEvent> = PublishRelay.create()
    private val motionEventsSubjects: PublishRelay<MotionEvent> = PublishRelay.create()

    protected val retroGameViewObservable = BehaviorRelay.createDefault<Optional<GLRetroView>>(None)
    protected var retroGameView: GLRetroView? by BehaviorRelayNullableProperty(retroGameViewObservable)

    val loadingObservable = BehaviorRelay.createDefault(false)
    var loading: Boolean by BehaviorRelayProperty(loadingObservable)

    val loadingMessageObservable = BehaviorRelay.createDefault("")
    var loadingMessage: String by BehaviorRelayProperty(loadingMessageObservable)

    val controllerConfigObservable = BehaviorRelay.createDefault<Map<Int, ControllerConfig>>(mapOf())
    var controllerConfigs: Map<Int, ControllerConfig> by BehaviorRelayProperty(controllerConfigObservable)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        setUpExceptionsHandler()

        mainContainerLayout = findViewById(R.id.maincontainer)
        gameContainerLayout = findViewById(R.id.gamecontainer)
        loadingView = findViewById(R.id.progress)
        loadingMessageView = findViewById(R.id.progress_message)
        leftGamePadContainer = findViewById(R.id.leftgamepad)
        rightGamePadContainer = findViewById(R.id.rightgamepad)

        game = intent.getSerializableExtra(EXTRA_GAME) as Game
        systemCoreConfig = intent.getSerializableExtra(EXTRA_SYSTEM_CORE_CONFIG) as SystemCoreConfig
        system = GameSystem.findById(game.systemId)

        loadGame()

        setupPhysicalPad()

        initializeControllers()
    }

    private fun initializeControllers() {
        retroGameViewMaybe()
            .flatMapObservable { it.getGLRetroEvents() }
            .filter { it is GLRetroView.GLRetroEvents.FrameRendered }
            .firstElement()
            .flatMapObservable { controllerConfigObservable }
            .autoDispose(scope())
            .subscribeBy(Timber::e) {
                updateControllers(it)
            }
    }

    private fun initializeRumble() {
        retroGameViewMaybe()
            .flatMapCompletable {
                rumbleManager.processRumbleEvents(systemCoreConfig, it.getRumbleEvents())
            }
            .autoDispose(scope())
            .subscribeBy(Timber::e) { }
    }

    private fun setUpExceptionsHandler() {
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            performUnsuccessfulActivityFinish(exception)
            defaultExceptionHandler?.uncaughtException(thread, exception)
        }
    }

    fun getControllerType(): Observable<Map<Int, ControllerConfig>> {
        return controllerConfigObservable
    }

    /* On some cores unserialize fails with no reason. So we need to try multiple times. */
    private fun restoreAutoSaveAsync(saveState: SaveState) {
        // PPSSPP and Mupen64 initialize some state while rendering the first frame, so we have to wait before restoring
        // the autosave. Do not change thread here. Stick to the GL one to avoid issues with PPSSPP.
        isAutoSaveEnabled()
            .filter { it }
            .flatMapObservable {
                retroGameViewMaybe().flatMapObservable { it.getGLRetroEvents() }
            }
            .filter { it is GLRetroView.GLRetroEvents.FrameRendered }
            .firstElement()
            .flatMapCompletable { getRetryRestoreQuickSave(saveState) }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { displayLoadStateErrorMessage(it) }
            .onErrorComplete()
            .autoDispose(scope())
            .subscribe()
    }

    private fun takeScreenshotPreview(): Maybe<Bitmap> {
        val sizeInDp = StatesPreviewManager.PREVIEW_SIZE_DP
        val previewSize = GraphicsUtils.convertDpToPixel(sizeInDp, applicationContext).roundToInt()
        return retroGameView?.takeScreenshot(previewSize)
            ?.retry(3) // Sometimes this fails. Let's just retry a couple of times.
            ?.onErrorComplete()
            ?: Maybe.empty()
    }

    private fun initializeRetroGameView(
        gameData: GameLoader.GameData,
        screenFilter: String,
        lowLatencyAudio: Boolean,
        enableRumble: Boolean
    ): GLRetroView {
        val data = GLRetroViewData(this).apply {
            coreFilePath = gameData.coreLibrary

            when (val gameFiles = gameData.gameFiles) {
                is RomFiles.Standard -> {
                    gameFilePath = gameFiles.files.first().absolutePath
                }
                is RomFiles.Virtual -> {
                    gameVirtualFiles = gameFiles.files
                        .map { VirtualFile(it.filePath, it.fd) }
                }
            }

            systemDirectory = gameData.systemDirectory.absolutePath
            savesDirectory = gameData.savesDirectory.absolutePath
            variables = gameData.coreVariables.map { Variable(it.key, it.value) }.toTypedArray()
            saveRAMState = gameData.saveRAMData
            shader = getShaderForSystem(screenFilter, system)
            preferLowLatencyAudio = lowLatencyAudio
            rumbleEventsEnabled = enableRumble
        }

        val retroGameView = GLRetroView(this, data)
        retroGameView.isFocusable = false
        retroGameView.isFocusableInTouchMode = false

        lifecycle.addObserver(retroGameView)
        gameContainerLayout.addView(retroGameView)

        val layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.gravity = Gravity.CENTER
        retroGameView.layoutParams = layoutParams

        gameData.quickSaveData?.let {
            restoreAutoSaveAsync(it)
        }

        if (BuildConfig.DEBUG) {
            printRetroVariables(retroGameView)
        }

        return retroGameView
    }

    private fun printRetroVariables(retroGameView: GLRetroView) {
        retroGameView.getVariables().forEach {
            Timber.i("Libretro variable: $it")
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

    private fun findControllerId(supported: Array<Controller>, controllerConfig: ControllerConfig): Int? {
        return supported
            .firstOrNull { controller ->
                sequenceOf(
                    controller.id == controllerConfig.libretroId,
                    controller.description == controllerConfig.libretroDescriptor
                ).any { it }
            }?.id
    }

    private fun handleRetroViewError(errorCode: Int) {
        Timber.e("Error in GLRetroView $errorCode")
        val gameLoaderError = when (errorCode) {
            GLRetroView.ERROR_GL_NOT_COMPATIBLE -> GameLoaderError.GL_INCOMPATIBLE
            GLRetroView.ERROR_LOAD_GAME -> GameLoaderError.LOAD_GAME
            GLRetroView.ERROR_LOAD_LIBRARY -> GameLoaderError.LOAD_CORE
            GLRetroView.ERROR_SERIALIZATION -> GameLoaderError.SAVES
            else -> GameLoaderError.GENERIC
        }
        retroGameView = null
        displayGameLoaderError(gameLoaderError, systemCoreConfig)
    }

    private fun transformExposedSetting(
        exposedSetting: ExposedSetting,
        coreOptions: List<CoreOption>
    ): LemuroidCoreOption? {
        return coreOptions
            .firstOrNull { it.variable.key == exposedSetting.key }
            ?.let { LemuroidCoreOption(exposedSetting, it) }
    }

    protected fun displayOptionsDialog() {
        if (loading) return

        val coreOptions = getCoreOptions()

        val options = systemCoreConfig.exposedSettings
            .mapNotNull { transformExposedSetting(it, coreOptions) }

        val advancedOptions = systemCoreConfig.exposedAdvancedSettings
            .mapNotNull { transformExposedSetting(it, coreOptions) }

        val intent = Intent(this, getDialogClass()).apply {
            this.putExtra(GameMenuContract.EXTRA_CORE_OPTIONS, options.toTypedArray())
            this.putExtra(GameMenuContract.EXTRA_ADVANCED_CORE_OPTIONS, advancedOptions.toTypedArray())
            this.putExtra(GameMenuContract.EXTRA_CURRENT_DISK, retroGameView?.getCurrentDisk() ?: 0)
            this.putExtra(GameMenuContract.EXTRA_DISKS, retroGameView?.getAvailableDisks() ?: 0)
            this.putExtra(GameMenuContract.EXTRA_GAME, game)
            this.putExtra(GameMenuContract.EXTRA_SYSTEM_CORE_CONFIG, systemCoreConfig)
            this.putExtra(GameMenuContract.EXTRA_AUDIO_ENABLED, retroGameView?.audioEnabled)
            this.putExtra(GameMenuContract.EXTRA_FAST_FORWARD_SUPPORTED, system.fastForwardSupport)
            this.putExtra(GameMenuContract.EXTRA_FAST_FORWARD, retroGameView?.frameSpeed ?: 1 > 1)
        }
        startActivityForResult(intent, DIALOG_REQUEST)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    protected abstract fun getDialogClass(): Class<out Activity>

    private fun getShaderForSystem(screenFiter: String, system: GameSystem): Int {
        return when (screenFiter) {
            "crt" -> GLRetroView.SHADER_CRT
            "lcd" -> GLRetroView.SHADER_LCD
            "smooth" -> GLRetroView.SHADER_DEFAULT
            "sharp" -> GLRetroView.SHADER_SHARP
            else -> when (system.id) {
                SystemID.GBA -> GLRetroView.SHADER_LCD
                SystemID.GBC -> GLRetroView.SHADER_LCD
                SystemID.GB -> GLRetroView.SHADER_LCD
                SystemID.N64 -> GLRetroView.SHADER_CRT
                SystemID.GENESIS -> GLRetroView.SHADER_CRT
                SystemID.SEGACD -> GLRetroView.SHADER_CRT
                SystemID.NES -> GLRetroView.SHADER_CRT
                SystemID.SNES -> GLRetroView.SHADER_CRT
                SystemID.FBNEO -> GLRetroView.SHADER_CRT
                SystemID.SMS -> GLRetroView.SHADER_CRT
                SystemID.PSP -> GLRetroView.SHADER_LCD
                SystemID.NDS -> GLRetroView.SHADER_LCD
                SystemID.GG -> GLRetroView.SHADER_LCD
                SystemID.ATARI2600 -> GLRetroView.SHADER_CRT
                SystemID.PSX -> GLRetroView.SHADER_CRT
                SystemID.MAME2003PLUS -> GLRetroView.SHADER_CRT
                SystemID.ATARI7800 -> GLRetroView.SHADER_CRT
                SystemID.PC_ENGINE -> GLRetroView.SHADER_CRT
                SystemID.LYNX -> GLRetroView.SHADER_LCD
                SystemID.DOS -> GLRetroView.SHADER_CRT
                SystemID.NGP -> GLRetroView.SHADER_LCD
                SystemID.NGC -> GLRetroView.SHADER_LCD
                SystemID.WS -> GLRetroView.SHADER_LCD
                SystemID.WSC -> GLRetroView.SHADER_LCD
            }
        }
    }

    private fun isAutoSaveEnabled(): Single<Boolean> {
        return settingsManager.autoSave.map { it && systemCoreConfig.statesSupported }
    }

    override fun onStart() {
        super.onStart()

        retroGameViewMaybe()
            .flatMapObservable { it.getGLRetroErrors() }
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribeBy({ Timber.e(it, "Exception in GLRetroErrors. Ironic.") }) {
                handleRetroViewError(it)
            }
    }

    override fun onResume() {
        super.onResume()

        loadingObservable
            .debounce(200, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe {
                loadingView.setVisibleOrGone(it)
                loadingMessageView.setVisibleOrGone(it)
            }

        loadingMessageObservable
            .debounce(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe {
                loadingMessageView.text = it
            }

        retroGameViewMaybe()
            .flatMapSingle { coreVariablesManager.getOptionsForCore(system.id, systemCoreConfig) }
            .autoDispose(scope())
            .subscribeBy(Timber::e) {
                updateCoreVariables(it)
            }

        retroGameViewMaybe()
            .flatMapSingle { controllerConfigsManager.getControllerConfigs(system.id, systemCoreConfig) }
            .autoDispose(scope())
            .subscribeBy(Timber::e) {
                controllerConfigs = it
            }

        initializeRumble()
    }

    private fun getCoreOptions(): List<CoreOption> {
        return retroGameView?.getVariables()
            ?.map { CoreOption.fromLibretroDroidVariable(it) } ?: listOf()
    }

    private fun updateCoreVariables(options: List<CoreVariable>) {
        val updatedVariables = options.map { Variable(it.key, it.value) }
            .toTypedArray()

        updatedVariables.forEach {
            Timber.i("Updating core variable: ${it.key} ${it.value}")
        }

        retroGameView?.updateVariables(*updatedVariables)
    }

    // Now that we wait for the first rendered frame this is probably no longer needed, but we'll keep it just to be sure
    private fun getRetryRestoreQuickSave(saveState: SaveState) = Completable.fromCallable {
        var times = 10

        while (!loadSaveState(saveState) && times > 0) {
            Thread.sleep(200)
            times--
        }
    }

    private fun setupPhysicalPad() {
        setupGamePadKeys()
        setupGamePadMotions()
        setupGamePadShortcuts()
    }

    private fun setupGamePadShortcuts() {
        inputDeviceManager.getInputMenuShortCutObservable()
            .distinctUntilChanged()
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribeBy { shortcut ->
                shortcut.toNullable()?.let {
                    displayToast(
                        resources.getString(R.string.game_toast_settings_button_using_gamepad, it.name)
                    )
                }
            }
    }

    private fun setupGamePadMotions() {
        val events = Observables.combineLatest(
            inputDeviceManager.getGamePadsPortMapperObservable(),
            motionEventsSubjects
        ).share()

        events
            .autoDispose(scope())
            .subscribeBy { (ports, event) ->
                ports(event.device)?.let {
                    sendStickMotions(event, it)
                }
            }

        events
            .flatMap { (ports, event) ->
                val port = ports(event.device)
                val axes = event.device.getInputClass().getAxesMap().entries
                Observable.fromIterable(axes).map { (axis, button) ->
                    val action = if (event.getAxisValue(axis) > 0.5) {
                        KeyEvent.ACTION_DOWN
                    } else {
                        KeyEvent.ACTION_UP
                    }
                    NTuple4(axis, button, action, port)
                }
            }
            .groupBy { (axis, _, _, _) -> axis }
            .flatMap { groups ->
                groups.distinctUntilChanged()
                    .doOnNext { (_, button, action, port) ->
                        port?.let {
                            retroGameView?.sendKeyEvent(action, button, it)
                        }
                    }
            }
            .autoDispose(scope())
            .subscribeBy { }
    }

    private fun setupGamePadKeys() {
        val pressedKeys = mutableSetOf<Int>()

        val filteredKeyEvents = keyEventsSubjects
            .filter { it.repeatCount == 0 }
            .map { Triple(it.device, it.action, it.keyCode) }
            .distinctUntilChanged()

        val shortcutKeys = inputDeviceManager.getInputMenuShortCutObservable()
            .map { it.toNullable()?.keys ?: setOf() }

        val combinedObservable = RXUtils.combineLatest(
            shortcutKeys,
            inputDeviceManager.getGamePadsPortMapperObservable(),
            inputDeviceManager.getInputBindingsObservable(),
            filteredKeyEvents
        )

        combinedObservable
            .doOnSubscribe { pressedKeys.clear() }
            .doOnDispose { pressedKeys.clear() }
            .autoDispose(scope())
            .subscribeBy { (shortcut, ports, bindings, event) ->
                val (device, action, keyCode) = event
                val port = ports(device)
                val bindKeyCode = bindings(device)[keyCode] ?: keyCode

                if (port == 0) {
                    if (bindKeyCode == KeyEvent.KEYCODE_BUTTON_MODE && action == KeyEvent.ACTION_DOWN) {
                        displayOptionsDialog()
                        return@subscribeBy
                    }

                    if (action == KeyEvent.ACTION_DOWN) {
                        pressedKeys.add(keyCode)
                    } else if (action == KeyEvent.ACTION_UP) {
                        pressedKeys.remove(keyCode)
                    }

                    if (shortcut.isNotEmpty() && pressedKeys.containsAll(shortcut)) {
                        displayOptionsDialog()
                        return@subscribeBy
                    }
                }

                port?.let {
                    retroGameView?.sendKeyEvent(action, bindKeyCode, it)
                }
            }
    }

    private fun sendStickMotions(event: MotionEvent, port: Int) {
        if (port < 0) return
        when (event.source) {
            InputDevice.SOURCE_JOYSTICK -> {
                if (controllerConfigs[port]?.mergeDPADAndLeftStickEvents == true) {
                    sendMergedMotionEvents(event, port)
                } else {
                    sendSeparateMotionEvents(event, port)
                }
            }
        }
    }

    private fun sendMergedMotionEvents(event: MotionEvent, port: Int) {
        val events = listOf(
            retrieveCoordinates(event, MotionEvent.AXIS_HAT_X, MotionEvent.AXIS_HAT_Y),
            retrieveCoordinates(event, MotionEvent.AXIS_X, MotionEvent.AXIS_Y)
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
            port
        )
    }

    private fun sendSeparateMotionEvents(event: MotionEvent, port: Int) {
        sendDPADMotion(
            event,
            MOTION_SOURCE_DPAD,
            MotionEvent.AXIS_HAT_X,
            MotionEvent.AXIS_HAT_Y,
            port
        )
        sendStickMotion(
            event,
            MOTION_SOURCE_ANALOG_LEFT,
            MotionEvent.AXIS_X,
            MotionEvent.AXIS_Y,
            port
        )
        sendStickMotion(
            event,
            MOTION_SOURCE_ANALOG_RIGHT,
            MotionEvent.AXIS_Z,
            MotionEvent.AXIS_RZ,
            port
        )
    }

    private fun sendStickMotion(
        event: MotionEvent,
        source: Int,
        xAxis: Int,
        yAxis: Int,
        port: Int
    ) {
        val coords = retrieveCoordinates(event, xAxis, yAxis)
        retroGameView?.sendMotionEvent(source, coords.x, coords.y, port)
    }

    private fun sendDPADMotion(
        event: MotionEvent,
        source: Int,
        xAxis: Int,
        yAxis: Int,
        port: Int
    ) {
        retroGameView?.sendMotionEvent(source, event.getAxisValue(xAxis), event.getAxisValue(yAxis), port)
    }

    @Deprecated("This sadly creates some issues with certain controllers and input lag on very slow devices.")
    private fun retrieveNormalizedCoordinates(event: MotionEvent, xAxis: Int, yAxis: Int): PointF {
        val rawX = event.getAxisValue(xAxis)
        val rawY = -event.getAxisValue(yAxis)

        val angle = MathUtils.angle(0f, rawX, 0f, rawY)
        val distance = MathUtils.clamp(MathUtils.distance(0f, rawX, 0f, rawY), 0f, 1f)

        return MathUtils.convertPolarCoordinatesToSquares(angle, distance)
    }

    private fun retrieveCoordinates(event: MotionEvent, xAxis: Int, yAxis: Int): PointF {
        return PointF(event.getAxisValue(xAxis), event.getAxisValue(yAxis))
    }

    override fun onGenericMotionEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            motionEventsSubjects.accept(event)
        }
        return super.onGenericMotionEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (event != null && keyCode in event.device.getInputClass().getInputKeys()) {
            keyEventsSubjects.accept(event)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (event != null && keyCode in event.device.getInputClass().getInputKeys()) {
            keyEventsSubjects.accept(event)
            return true
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun onBackPressed() {
        if (loading) return
        autoSaveAndFinish()
    }

    private fun autoSaveAndFinish() {
        getAutoSaveAndFinishCompletable()
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { loading = true }
            .doAfterTerminate { loading = false }
            .autoDispose(scope())
            .subscribe()
    }

    private fun getAutoSaveAndFinishCompletable(): Completable {
        val saveRAMCompletable = getSaveRAMCompletable(game)
        val autoSaveCompletable = getAutoSaveCompletable(game)

        return saveRAMCompletable.andThen(autoSaveCompletable)
            .doOnComplete { performSuccessfulActivityFinish() }
    }

    private fun performSuccessfulActivityFinish() {
        val resultIntent = Intent().apply {
            putExtra(PLAY_GAME_RESULT_SESSION_DURATION, System.currentTimeMillis() - startGameTime)
            putExtra(PLAY_GAME_RESULT_GAME, intent.getSerializableExtra(EXTRA_GAME))
            putExtra(PLAY_GAME_RESULT_LEANBACK, intent.getBooleanExtra(EXTRA_LEANBACK, false))
        }

        setResult(Activity.RESULT_OK, resultIntent)

        finishAndExitProcess()
    }

    private fun performUnsuccessfulActivityFinish(exception: Throwable) {
        Timber.e(exception, "Handling java exception in BaseGameActivity")
        val resultIntent = Intent().apply {
            putExtra(PLAY_GAME_RESULT_ERROR, exception.message)
        }

        setResult(Activity.RESULT_CANCELED, resultIntent)
        finish()
    }

    private fun finishAndExitProcess() {
        onFinishTriggered()
        GlobalScope.launch {
            sleep(animationDuration().toLong())
            exitProcess(0)
        }
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    open fun onFinishTriggered() { }

    private fun getAutoSaveCompletable(game: Game): Completable {
        return isAutoSaveEnabled()
            .filter { it }
            .map { getCurrentSaveState() }
            .doOnSuccess { Timber.i("Stored autosave file with size: ${it?.state?.size}") }
            .flatMapCompletable { statesManager.setAutoSave(game, systemCoreConfig.coreID, it) }
    }

    private fun getSaveRAMCompletable(game: Game): Completable {
        val retroGameView = retroGameView ?: return Completable.complete()

        return Single.fromCallable { retroGameView.serializeSRAM() }
            .doOnSuccess { Timber.i("Stored sram file with size: ${it.size}") }
            .flatMapCompletable { savesManager.setSaveRAM(game, it) }
    }

    private fun saveSlot(index: Int): Completable {
        if (loading) return Completable.complete()
        return Maybe.fromCallable { getCurrentSaveState() }
            .doAfterSuccess { Timber.i("Storing quicksave with size: ${it!!.state.size}") }
            .subscribeOn(Schedulers.io())
            .flatMapCompletable {
                statesManager.setSlotSave(game, it, systemCoreConfig.coreID, index)
            }
            .doOnError { Timber.e(it, "Error while saving slot $index") }
            .andThen(takeScreenshotPreview())
            .flatMapCompletable {
                statesPreviewManager.setPreviewForSlot(game, it, systemCoreConfig.coreID, index)
            }
            .onErrorComplete()
            .doOnSubscribe { loading = true }
            .doAfterTerminate { loading = false }
    }

    private fun loadSlot(index: Int): Completable {
        if (loading) return Completable.complete()
        return statesManager.getSlotSave(game, systemCoreConfig.coreID, index)
            .map { loadSaveState(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess { if (!it) displayToast(R.string.game_toast_load_state_failed) }
            .doOnError { displayLoadStateErrorMessage(it) }
            .onErrorComplete()
            .doOnSubscribe { loading = true }
            .doAfterTerminate { loading = false }
            .ignoreElement()
    }

    private fun getCurrentSaveState(): SaveState? {
        val retroGameView = retroGameView ?: return null
        val currentDisk = if (system.hasMultiDiskSupport) {
            retroGameView.getCurrentDisk()
        } else {
            0
        }
        return SaveState(
            retroGameView.serializeState(),
            SaveState.Metadata(currentDisk, systemCoreConfig.statesVersion)
        )
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

    private fun displayLoadStateErrorMessage(throwable: Throwable) {
        when (throwable) {
            is IncompatibleStateException ->
                displayToast(R.string.error_message_incompatible_state, Toast.LENGTH_LONG)

            else -> displayToast(R.string.game_toast_load_state_failed)
        }
    }

    private fun reset() {
        retroGameView?.reset()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == DIALOG_REQUEST) {
            Timber.i("Game menu dialog response: ${data?.extras.dump()}")
            if (data?.getBooleanExtra(GameMenuContract.RESULT_RESET, false) == true) {
                reset()
            }
            if (data?.hasExtra(GameMenuContract.RESULT_SAVE) == true) {
                saveSlot(data.getIntExtra(GameMenuContract.RESULT_SAVE, 0))
                    .autoDispose(scope())
                    .subscribe()
            }
            if (data?.hasExtra(GameMenuContract.RESULT_LOAD) == true) {
                loadSlot(data.getIntExtra(GameMenuContract.RESULT_LOAD, 0))
                    .autoDispose(scope())
                    .subscribe()
            }
            if (data?.getBooleanExtra(GameMenuContract.RESULT_QUIT, false) == true) {
                autoSaveAndFinish()
            }
            if (data?.hasExtra(GameMenuContract.RESULT_CHANGE_DISK) == true) {
                val index = data.getIntExtra(GameMenuContract.RESULT_CHANGE_DISK, 0)
                retroGameView?.changeDisk(index)
            }
            if (data?.hasExtra(GameMenuContract.RESULT_ENABLE_AUDIO) == true) {
                retroGameView?.apply {
                    this.audioEnabled = data.getBooleanExtra(
                        GameMenuContract.RESULT_ENABLE_AUDIO,
                        true
                    )
                }
            }
            if (data?.hasExtra(GameMenuContract.RESULT_ENABLE_FAST_FORWARD) == true) {
                retroGameView?.apply {
                    val fastForwardEnabled = data.getBooleanExtra(
                        GameMenuContract.RESULT_ENABLE_FAST_FORWARD,
                        false
                    )
                    this.frameSpeed = if (fastForwardEnabled) 2 else 1
                }
            }
        }
    }

    private fun loadGame() {
        val requestLoadSave = intent.getBooleanExtra(EXTRA_LOAD_SAVE, false)

        setupLoadingView()

        Singles.zip(
            settingsManager.autoSave,
            settingsManager.screenFilter,
            settingsManager.lowLatencyAudio,
            settingsManager.enableRumble,
            settingsManager.allowDirectGameLoad,
            ::NTuple5
        )
            .flatMapObservable { (autoSaveEnabled, filter, lowLatencyAudio, enableRumble, directLoad) ->
                gameLoader.load(
                    applicationContext,
                    game,
                    requestLoadSave && autoSaveEnabled,
                    systemCoreConfig,
                    directLoad
                ).map { NTuple4(it, filter, lowLatencyAudio, enableRumble) }
            }
            .subscribeOn(Schedulers.single())
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe(
                { (loadingState, filter, lowLatencyAudio, enableRumble) ->
                    displayLoadingState(loadingState)
                    if (loadingState is GameLoader.LoadingState.Ready) {
                        retroGameView = initializeRetroGameView(
                            loadingState.gameData,
                            filter,
                            lowLatencyAudio,
                            systemCoreConfig.rumbleSupported && enableRumble
                        )
                    }
                },
                {
                    displayGameLoaderError((it as GameLoaderException).error, systemCoreConfig)
                }
            )
    }

    private fun setupLoadingView() {
        retroGameViewMaybe()
            .flatMapObservable { it.getGLRetroEvents() }
            .filter { it is GLRetroView.GLRetroEvents.FrameRendered }
            .firstElement()
            .doOnSubscribe { loading = true }
            .doAfterTerminate { loading = false }
            .autoDispose(scope())
            .subscribeBy(Timber::e) { }
    }

    private fun retroGameViewMaybe() = retroGameViewObservable.filterSome().firstElement()

    private fun displayLoadingState(loadingState: GameLoader.LoadingState) {
        loadingMessage = when (loadingState) {
            is GameLoader.LoadingState.LoadingCore -> getString(R.string.game_loading_download_core)
            is GameLoader.LoadingState.LoadingGame -> getString(R.string.game_loading_preparing_game)
            else -> ""
        }
    }

    companion object {
        const val DIALOG_REQUEST = 100

        private const val EXTRA_GAME = "GAME"
        private const val EXTRA_LOAD_SAVE = "LOAD_SAVE"
        private const val EXTRA_LEANBACK = "LEANBACK"
        private const val EXTRA_SYSTEM_CORE_CONFIG = "EXTRA_SYSTEM_CORE_CONFIG"

        const val REQUEST_PLAY_GAME = 1001
        const val PLAY_GAME_RESULT_SESSION_DURATION = "PLAY_GAME_RESULT_SESSION_DURATION"
        const val PLAY_GAME_RESULT_GAME = "PLAY_GAME_RESULT_GAME"
        const val PLAY_GAME_RESULT_LEANBACK = "PLAY_GAME_RESULT_LEANBACK"
        const val PLAY_GAME_RESULT_ERROR = "PLAY_GAME_RESULT_ERROR"

        fun launchGame(
            activity: Activity,
            systemCoreConfig: SystemCoreConfig,
            game: Game,
            loadSave: Boolean,
            useLeanback: Boolean
        ) {
            val gameActivity = if (useLeanback) {
                TVGameActivity::class.java
            } else {
                GameActivity::class.java
            }
            activity.startActivityForResult(
                Intent(activity, gameActivity).apply {
                    putExtra(EXTRA_GAME, game)
                    putExtra(EXTRA_LOAD_SAVE, loadSave)
                    putExtra(EXTRA_LEANBACK, useLeanback)
                    putExtra(EXTRA_SYSTEM_CORE_CONFIG, systemCoreConfig)
                },
                REQUEST_PLAY_GAME
            )
            activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
}
