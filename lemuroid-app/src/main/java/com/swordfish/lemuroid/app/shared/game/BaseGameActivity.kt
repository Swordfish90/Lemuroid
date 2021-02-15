package com.swordfish.lemuroid.app.shared.game

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Gravity
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.gojuno.koptional.Some
import com.jakewharton.rxrelay2.PublishRelay
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.feature.game.GameActivity
import com.swordfish.lemuroid.app.mobile.feature.settings.SettingsManager
import com.swordfish.lemuroid.app.shared.GameMenuContract
import com.swordfish.lemuroid.app.shared.ImmersiveActivity
import com.swordfish.lemuroid.app.shared.coreoptions.CoreOption
import com.swordfish.lemuroid.app.shared.gamecrash.GameCrashHandler
import com.swordfish.lemuroid.app.shared.savesync.SaveSyncWork
import com.swordfish.lemuroid.app.shared.settings.GamePadManager
import com.swordfish.lemuroid.app.shared.settings.ControllerConfigsManager
import com.swordfish.lemuroid.app.tv.game.TVGameActivity
import com.swordfish.lemuroid.common.displayToast
import com.swordfish.lemuroid.common.dump
import com.swordfish.lemuroid.common.graphics.GraphicsUtils
import com.swordfish.lemuroid.common.graphics.takeScreenshot
import com.swordfish.lemuroid.common.kotlin.NTuple4
import com.swordfish.lemuroid.common.kotlin.filterNotNullValues
import com.swordfish.lemuroid.common.kotlin.toIndexedMap
import com.swordfish.lemuroid.common.kotlin.zipOnKeys
import com.swordfish.lemuroid.common.rx.RXUtils
import com.swordfish.lemuroid.common.rx.RxNullableProperty
import com.swordfish.lemuroid.common.rx.RxProperty
import com.swordfish.lemuroid.common.rx.asNullableObservable
import com.swordfish.lemuroid.common.rx.asObservable
import com.swordfish.lemuroid.lib.controller.ControllerConfig
import com.swordfish.lemuroid.lib.core.CoreVariable
import com.swordfish.lemuroid.lib.core.CoreVariablesManager
import com.swordfish.lemuroid.lib.core.CoresSelection
import com.swordfish.lemuroid.lib.game.GameLoader
import com.swordfish.lemuroid.lib.game.GameLoaderError
import com.swordfish.lemuroid.lib.game.GameLoaderException
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.SystemCoreConfig
import com.swordfish.lemuroid.lib.library.SystemID
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.saves.SaveState
import com.swordfish.lemuroid.lib.saves.SavesManager
import com.swordfish.lemuroid.lib.saves.StatesManager
import com.swordfish.lemuroid.lib.saves.StatesPreviewManager
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import com.swordfish.lemuroid.lib.storage.cache.CacheCleanerWork
import com.swordfish.lemuroid.lib.ui.setVisibleOrGone
import com.swordfish.lemuroid.lib.util.subscribeBy
import com.swordfish.libretrodroid.Controller
import com.swordfish.libretrodroid.GLRetroView
import com.swordfish.libretrodroid.GLRetroView.Companion.MOTION_SOURCE_ANALOG_LEFT
import com.swordfish.libretrodroid.GLRetroView.Companion.MOTION_SOURCE_ANALOG_RIGHT
import com.swordfish.libretrodroid.GLRetroView.Companion.MOTION_SOURCE_DPAD
import com.swordfish.libretrodroid.GLRetroViewData
import com.swordfish.libretrodroid.Variable
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Thread.sleep
import java.util.concurrent.TimeUnit
import javax.inject.Inject
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

    @Inject lateinit var settingsManager: SettingsManager
    @Inject lateinit var statesManager: StatesManager
    @Inject lateinit var statesPreviewManager: StatesPreviewManager
    @Inject lateinit var savesManager: SavesManager
    @Inject lateinit var coreVariablesManager: CoreVariablesManager
    @Inject lateinit var gamePadManager: GamePadManager
    @Inject lateinit var gameLoader: GameLoader
    @Inject lateinit var coresSelection: CoresSelection
    @Inject lateinit var controllerConfigsManager: ControllerConfigsManager

    private val startGameTime = System.currentTimeMillis()

    private val keyEventsSubjects: PublishRelay<KeyEvent> = PublishRelay.create()
    private val motionEventsSubjects: PublishRelay<MotionEvent> = PublishRelay.create()

    protected var retroGameView: GLRetroView? by RxNullableProperty(null)

    var loading: Boolean by RxProperty(true)
    var loadingMessage: String by RxProperty("")
    var controllerConfigs: Map<Int, ControllerConfig> by RxProperty(mapOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        setupCrashActivity()

        mainContainerLayout = findViewById(R.id.maincontainer)
        gameContainerLayout = findViewById(R.id.gamecontainer)
        loadingView = findViewById(R.id.progress)
        loadingMessageView = findViewById(R.id.progress_message)
        leftGamePadContainer = findViewById(R.id.leftgamepad)
        rightGamePadContainer = findViewById(R.id.rightgamepad)

        game = intent.getSerializableExtra(EXTRA_GAME) as Game
        system = GameSystem.findById(game.systemId)
        systemCoreConfig = coresSelection.getCoreConfigForSystem(system)

        loadGame()

        if (areGamePadsEnabled()) {
            setupPhysicalPad()
        }

        initializeControllers()
    }

    private fun initializeControllers() {
        this::retroGameView.asNullableObservable()
            .filter { it is Some }
            .flatMap { it.toNullable()?.getGLRetroEvents() ?: Observable.empty() }
            .filter { it is GLRetroView.GLRetroEvents.FrameRendered }
            .firstElement()
            .flatMapObservable { this::controllerConfigs.asObservable() }
            .autoDispose(scope())
            .subscribeBy({}) {
                updateControllers(it)
            }
    }

    private fun setupCrashActivity() {
        val systemHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(GameCrashHandler(this, systemHandler))
    }

    abstract fun areGamePadsEnabled(): Boolean

    fun getControllerType(): Observable<Map<Int, ControllerConfig>> {
        return this::controllerConfigs.asObservable()
    }

    /* On some cores unserialize fails with no reason. So we need to try multiple times. */
    private fun restoreAutoSaveAsync(saveState: SaveState) {
        if (!isAutoSaveEnabled()) {
            return
        }

        // PPSSPP and Mupen64 initialize some state while rendering the first frame, so we have to wait before restoring
        // the autosave. Do not change thread here. Stick to the GL one to avoid issues with PPSSPP.
        retroGameView?.getGLRetroEvents()
            ?.filter { it is GLRetroView.GLRetroEvents.FrameRendered }
            ?.firstElement()
            ?.flatMapCompletable { getRetryRestoreQuickSave(saveState) }
            ?.autoDispose(scope())
            ?.subscribe()
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
        screenFilter: String
    ) {
        val directoriesManager = DirectoriesManager(applicationContext)

        val data = GLRetroViewData(this).apply {
            coreFilePath = gameData.coreLibrary
            gameFilePath = gameData.gameFile.absolutePath
            systemDirectory = directoriesManager.getSystemDirectory().absolutePath
            savesDirectory = directoriesManager.getSavesDirectory().absolutePath
            variables = gameData.coreVariables.map { Variable(it.key, it.value) }.toTypedArray()
            saveRAMState = gameData.saveRAMData
            shader = getShaderForSystem(screenFilter, system)
        }

        retroGameView = GLRetroView(this, data)
        retroGameView?.isFocusable = false
        retroGameView?.isFocusableInTouchMode = false

        retroGameView
            ?.getGLRetroErrors()
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.autoDispose(scope())
            ?.subscribeBy({ Timber.e(it, "Exception in GLRetroErrors. Ironic.") }) {
                handleRetroViewError(it)
            }

        retroGameView?.let { lifecycle.addObserver(it) }
        gameContainerLayout.addView(retroGameView)

        lifecycle.addObserver(
            object : LifecycleObserver {
                @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
                fun onResume() {
                    coreVariablesManager.getOptionsForCore(system.id, systemCoreConfig)
                        .subscribeBy({}) {
                            onVariablesRead(it)
                        }

                    controllerConfigsManager.getControllerConfigs(system.id, systemCoreConfig)
                        .subscribeBy {
                            controllerConfigs = it
                        }
                }

                @OnLifecycleEvent(Lifecycle.Event.ON_START)
                fun onStart() {
                    coreVariablesManager.getOptionsForCore(system.id, systemCoreConfig)
                        .subscribeBy({}) {
                            updateCoreVariables(it)
                        }
                }
            }
        )

        val layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.gravity = Gravity.CENTER
        retroGameView?.layoutParams = layoutParams

        gameData.quickSaveData?.let {
            restoreAutoSaveAsync(it)
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
            .firstOrNull { it.description == controllerConfig.libretroDescriptor }
            ?.id
    }

    private fun handleRetroViewError(errorCode: Int) {
        val gameLoaderError = when (errorCode) {
            GLRetroView.ERROR_GL_NOT_COMPATIBLE -> GameLoaderError.GL_INCOMPATIBLE
            GLRetroView.ERROR_LOAD_GAME -> GameLoaderError.LOAD_GAME
            GLRetroView.ERROR_LOAD_LIBRARY -> GameLoaderError.LOAD_CORE
            GLRetroView.ERROR_SERIALIZATION -> GameLoaderError.SAVES
            else -> GameLoaderError.GENERIC
        }
        retroGameView = null
        displayGameLoaderError(gameLoaderError)
    }

    protected fun displayOptionsDialog() {
        if (loading) return

        val options = getCoreOptions()
            .filter { it.variable.key in systemCoreConfig.exposedSettings }

        val advancedOptions = getCoreOptions()
            .filter { it.variable.key in systemCoreConfig.exposedAdvancedSettings }

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
            }
        }
    }

    private fun isAutoSaveEnabled(): Boolean {
        return settingsManager.autoSave && systemCoreConfig.statesSupported
    }

    override fun onResume() {
        super.onResume()

        this::loading.asObservable()
            .debounce(200, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe {
                loadingView.setVisibleOrGone(it)
                loadingMessageView.setVisibleOrGone(it)
            }

        this::loadingMessage.asObservable()
            .debounce(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe {
                loadingMessageView.text = it
            }
    }

    private fun onVariablesRead(coreVariables: List<CoreVariable>) {
        updateCoreVariables(coreVariables)
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
        gamePadManager.getGamePadMenuShortCutObservable()
            .distinctUntilChanged()
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
            gamePadManager.getGamePadsPortMapperObservable(),
            motionEventsSubjects
        )
            .share()

        events
            .autoDispose(scope())
            .subscribeBy { (ports, event) -> sendStickMotions(event, ports(event.device)) }

        events
            .flatMap { (ports, event) ->
                val port = ports(event.device)
                val axes = GamePadManager.TRIGGER_MOTIONS_TO_KEYS.entries
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
                        retroGameView?.sendKeyEvent(action, button, port)
                    }
            }
            .autoDispose(scope())
            .subscribeBy { }
    }

    private fun setupGamePadKeys() {
        val pressedKeys = mutableSetOf<Int>()

        val filteredKeyEvents = keyEventsSubjects
            .map { Triple(it.device, it.action, it.keyCode) }
            .distinctUntilChanged()

        val shortcutKeys = gamePadManager.getGamePadMenuShortCutObservable()
            .map { it.toNullable()?.keys ?: setOf() }

        val combinedObservable = RXUtils.combineLatest(
            shortcutKeys,
            gamePadManager.getGamePadsPortMapperObservable(),
            gamePadManager.getGamePadsBindingsObservable(),
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

                retroGameView?.sendKeyEvent(action, bindKeyCode, port)
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
        val xAxises = setOf(MotionEvent.AXIS_HAT_X, MotionEvent.AXIS_X)
        val yAxises = setOf(MotionEvent.AXIS_HAT_Y, MotionEvent.AXIS_Y)
        val xVal = xAxises.map { event.getAxisValue(it) }.maxBy { kotlin.math.abs(it) }!!
        val yVal = yAxises.map { event.getAxisValue(it) }.maxBy { kotlin.math.abs(it) }!!
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
        sendStickMotion(
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
        retroGameView?.sendMotionEvent(
            source,
            event.getAxisValue(xAxis),
            event.getAxisValue(yAxis),
            port
        )
    }

    override fun onGenericMotionEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            motionEventsSubjects.accept(event)
        }
        return super.onGenericMotionEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (event != null && keyCode in GamePadManager.INPUT_KEYS) {
            keyEventsSubjects.accept(event)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (event != null && keyCode in GamePadManager.INPUT_KEYS) {
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
            .doOnComplete { performActivityFinish() }
    }

    private fun performActivityFinish() {
        val resultIntent = Intent().apply {
            putExtra(PLAY_GAME_RESULT_SESSION_DURATION, System.currentTimeMillis() - startGameTime)
            putExtra(PLAY_GAME_RESULT_GAME, intent.getSerializableExtra(EXTRA_GAME))
            putExtra(PLAY_GAME_RESULT_LEANBACK, intent.getBooleanExtra(EXTRA_LEANBACK, false))
        }

        rescheduleBackgroundWork()

        setResult(Activity.RESULT_OK, resultIntent)

        finishAndExitProcess()
    }

    private fun finishAndExitProcess() {
        val animationTime = resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
        GlobalScope.launch {
            sleep(animationTime)
            exitProcess(0)
        }
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun cancelBackgroundWork() {
        SaveSyncWork.cancelAutoWork(applicationContext)
        SaveSyncWork.cancelManualWork(applicationContext)
        CacheCleanerWork.cancelCleanCacheLRU(applicationContext)
    }

    private fun rescheduleBackgroundWork() {
        // Let's slightly delay the sync. Maybe the user wants to play another game.
        SaveSyncWork.enqueueAutoWork(applicationContext, 5)
        CacheCleanerWork.enqueueCleanCacheLRU(applicationContext)
    }

    private fun getAutoSaveCompletable(game: Game): Completable {
        return Single.fromCallable { isAutoSaveEnabled() }
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
            .doOnError { displayToast(R.string.game_toast_load_state_failed) }
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
            SaveState.Metadata(currentDisk)
        )
    }

    private fun loadSaveState(saveState: SaveState): Boolean {
        val retroGameView = retroGameView ?: return false
        if (system.hasMultiDiskSupport &&
            retroGameView.getAvailableDisks() > 1 &&
            retroGameView.getCurrentDisk() != saveState.metadata.diskIndex
        ) {
            retroGameView.changeDisk(saveState.metadata.diskIndex)
        }
        return retroGameView.unserializeState(saveState.state)
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

    fun loadGame() {
        val requestLoadSave = intent.getBooleanExtra(EXTRA_LOAD_SAVE, false)

        cancelBackgroundWork()

        val core = coresSelection.getCoreConfigForSystem(system)
        val loadState = requestLoadSave && settingsManager.autoSave && core.statesSupported

        gameLoader.load(applicationContext, game, loadState)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { loading = true }
            .doAfterTerminate { loading = false }
            .autoDispose(scope())
            .subscribe(
                {
                    displayLoadingState(it)
                    if (it is GameLoader.LoadingState.Ready) {
                        initializeRetroGameView(it.gameData, settingsManager.screenFilter)
                    }
                },
                {
                    displayGameLoaderError((it as GameLoaderException).error)
                }
            )
    }

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

        const val REQUEST_PLAY_GAME = 1001
        const val PLAY_GAME_RESULT_SESSION_DURATION = "PLAY_GAME_RESULT_SESSION_DURATION"
        const val PLAY_GAME_RESULT_GAME = "PLAY_GAME_RESULT_GAME"
        const val PLAY_GAME_RESULT_LEANBACK = "PLAY_GAME_RESULT_LEANBACK"

        fun launchGame(activity: Activity, game: Game, loadSave: Boolean, useLeanback: Boolean) {
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
                },
                REQUEST_PLAY_GAME
            )
            activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
}
