package com.swordfish.lemuroid.app.shared.game

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.coreoptions.CoreOption
import com.swordfish.lemuroid.app.shared.GameMenuContract
import com.swordfish.lemuroid.app.mobile.feature.settings.SettingsManager
import com.swordfish.lemuroid.app.shared.ImmersiveActivity
import com.swordfish.lemuroid.app.shared.settings.GamePadManager
import com.swordfish.lemuroid.common.dump
import com.swordfish.lemuroid.common.kotlin.NTuple4
import com.swordfish.lemuroid.lib.core.CoreVariable
import com.swordfish.lemuroid.lib.core.CoreVariablesManager
import com.swordfish.lemuroid.lib.game.GameLoaderError
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.SystemCoreConfig
import com.swordfish.lemuroid.lib.library.SystemID
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.saves.SaveState
import com.swordfish.lemuroid.lib.saves.SavesManager
import com.swordfish.lemuroid.lib.saves.StatesManager
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import com.swordfish.lemuroid.lib.ui.setVisibleOrGone
import com.swordfish.lemuroid.lib.util.subscribeBy
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
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.properties.Delegates

abstract class BaseGameActivity : ImmersiveActivity() {

    protected lateinit var game: Game
    protected lateinit var system: GameSystem
    protected lateinit var systemCoreConfig: SystemCoreConfig
    protected lateinit var containerLayout: ConstraintLayout
    protected lateinit var gameViewLayout: FrameLayout
    protected lateinit var overlayLayout: FrameLayout
    private lateinit var loadingView: ProgressBar

    @Inject lateinit var settingsManager: SettingsManager
    @Inject lateinit var statesManager: StatesManager
    @Inject lateinit var savesManager: SavesManager
    @Inject lateinit var coreVariablesManager: CoreVariablesManager
    @Inject lateinit var gamePadManager: GamePadManager

    private val loadingSubject: BehaviorRelay<Boolean> = BehaviorRelay.create()

    private val keyEventsSubjects: PublishRelay<KeyEvent> = PublishRelay.create()
    private val motionEventsSubjects: PublishRelay<MotionEvent> = PublishRelay.create()

    protected var retroGameView: GLRetroView? = null

    var loading: Boolean by Delegates.observable(false) { _, _, value ->
        loadingSubject.accept(value)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        containerLayout = findViewById(R.id.game_container)
        gameViewLayout = findViewById(R.id.gameview_layout)
        overlayLayout = findViewById(R.id.overlay_layout)
        loadingView = findViewById(R.id.progress)

        game = intent.getSerializableExtra(EXTRA_GAME) as Game
        system = GameSystem.findById(game.systemId)
        systemCoreConfig = intent.getSerializableExtra(EXTRA_CORE_CONFIG) as SystemCoreConfig

        val directoriesManager = DirectoriesManager(applicationContext)

        initializeRetroGameView(directoriesManager, settingsManager.screenFilter)

        retrieveAutoSaveData()?.let {
            restoreAutoSaveAsync(it)
        }

        if (areGamePadsEnabled()) {
            setupPhysicalPad()
        }
    }

    abstract fun areGamePadsEnabled(): Boolean

    // If the activity is garbage collected we are losing its state. To avoid overwriting the previous autosave we just
    // reload the previous one. This is far from perfect but definitely improves the current behaviour.
    private fun retrieveAutoSaveData(): SaveState? {
        if (intent.getBooleanExtra(EXTRA_LOAD_AUTOSAVE, false)) {
            return getAndResetTransientQuickSave() ?: statesManager.getAutoSave(game, systemCoreConfig.coreID)
                .blockingGet()
        }
        return null
    }

    private fun retrieveSRAMData(): ByteArray? {
        if (intent.getBooleanExtra(EXTRA_LOAD_SRAM, false)) {
            return getAndResetTransientSaveRAMState() ?: savesManager.getSaveRAM(game).blockingGet()
        }
        return null
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

    private fun initializeRetroGameView(
        directoriesManager: DirectoriesManager,
        screenFilter: String
    ) {
        val coreVariables =
            (intent.getSerializableExtra(EXTRA_CORE_VARIABLES) as Array<CoreVariable>? ?: arrayOf())
                .map { Variable(it.key, it.value) }
                .toTypedArray()

        val data = GLRetroViewData(this).apply {
            coreFilePath = intent.getStringExtra(EXTRA_CORE_PATH)!!
            gameFilePath = intent.getStringExtra(EXTRA_GAME_PATH)!!
            systemDirectory = directoriesManager.getSystemDirectory().absolutePath
            savesDirectory = directoriesManager.getSavesDirectory().absolutePath
            variables = coreVariables
            saveRAMState = retrieveSRAMData()
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

        gameViewLayout.addView(retroGameView)

        val layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.gravity = Gravity.CENTER

        retroGameView?.layoutParams = layoutParams
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

    fun displayToast(id: Int) {
        Toast.makeText(this, id, Toast.LENGTH_SHORT).show()
    }

    fun displayToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    protected fun displayOptionsDialog() {
        val intent = Intent(this, getDialogClass()).apply {
            val options = getCoreOptions().filter { it.variable.key in systemCoreConfig.exposedSettings }
            this.putExtra(GameMenuContract.EXTRA_CORE_OPTIONS, options.toTypedArray())
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

        loadingSubject
            .throttleLast(200, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe { loadingView.setVisibleOrGone(it) }

        coreVariablesManager.getOptionsForCore(system.id, systemCoreConfig)
            .autoDispose(scope())
            .subscribeBy({}) {
                onVariablesRead(it)
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

    override fun onStart() {
        super.onStart()
        coreVariablesManager.getOptionsForCore(system.id, systemCoreConfig)
            .autoDispose(scope())
            .subscribeBy({}) {
                updateCoreVariables(it)
            }
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
        val gamePadShortcut = getGamePadMenuShortCutObservable()
            .distinctUntilChanged()
            .doOnNext {
                displayToast(
                    resources.getString(
                        R.string.game_toast_settings_button_using_gamepad,
                        it.label
                    )
                )
            }

        val firstPlayerPressedKeys = Observables.combineLatest(
            gamePadManager.getGamePadsPortMapperObservable(),
            keyEventsSubjects
        )
            .filter { (ports, key) -> ports(key.device) == 0 }
            .map { (_, key) -> key }
            .scan(mutableSetOf<Int>()) { keys, event ->
                if (event.action == KeyEvent.ACTION_DOWN) {
                    keys.add(event.keyCode)
                } else if (event.action == KeyEvent.ACTION_UP) {
                    keys.remove(event.keyCode)
                }
                keys
            }

        Observables.combineLatest(gamePadShortcut, firstPlayerPressedKeys)
            .autoDispose(scope())
            .subscribeBy { (shortcut, pressedKeys) ->
                if (pressedKeys.containsAll(shortcut.keys)) {
                    displayOptionsDialog()
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
                    .doOnNext { (_, button, action, port) -> retroGameView?.sendKeyEvent(action, button, port) }
            }
            .autoDispose(scope())
            .subscribeBy { }
    }

    private fun setupGamePadKeys() {
        val bindKeys = Observables.combineLatest(
            gamePadManager.getGamePadsPortMapperObservable(),
            gamePadManager.getGamePadsBindingsObservable(),
            keyEventsSubjects
        )
            .map { (ports, bindings, event) ->
                val port = ports(event.device)
                val bindKeyCode = bindings(event.device)[event.keyCode] ?: event.keyCode
                Triple(event.action, port, bindKeyCode)
            }
            .share()

        bindKeys
            .autoDispose(scope())
            .subscribeBy { (action, port, keyCode) ->
                retroGameView?.sendKeyEvent(action, keyCode, port)
            }

        bindKeys
            .filter { (action, port, keyCode) ->
                port == 0 && keyCode == KeyEvent.KEYCODE_BUTTON_MODE && action == KeyEvent.ACTION_DOWN
            }
            .autoDispose(scope())
            .subscribeBy { displayOptionsDialog() }
    }

    private fun sendStickMotions(event: MotionEvent, port: Int) {
        if (port < 0) return
        when (event.source) {
            InputDevice.SOURCE_JOYSTICK -> {
                if (system.mergeDPADAndLeftStickEvents) {
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

    private fun getGamePadMenuShortCutObservable(): Observable<GameMenuShortcut> {
        return gamePadManager
            .getGamePadsObservable()
            .flatMapMaybe {
                Maybe.fromCallable {
                    it.getOrNull(0)?.let { GameMenuShortcut.getBestShortcutForInputDevice(it) }
                }
            }
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
            .doOnComplete { finish() }
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
            .flatMapCompletable { statesManager.setSlotSave(game, it, systemCoreConfig.coreID, index) }
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

    companion object {
        const val EXTRA_GAME = "game"
        const val EXTRA_CORE_CONFIG = "core_config"
        const val EXTRA_CORE_PATH = "core_path"
        const val EXTRA_GAME_PATH = "game_path"
        const val EXTRA_CORE_VARIABLES = "core_variables"
        const val EXTRA_LOAD_SRAM = "load_sram"
        const val EXTRA_LOAD_AUTOSAVE = "load_autosave"

        const val DIALOG_REQUEST = 100

        private var transientStashedState: SaveState? = null
        private var transientSRAMState: ByteArray? = null

        /** A full savestate may not fit a bundle, so we need to ask for forgiveness and pass it statically. */
        fun setTransientQuickSave(state: SaveState?) {
            transientStashedState = state
        }

        fun setTransientSaveRAMState(data: ByteArray?) {
            transientSRAMState = data
        }

        fun getAndResetTransientQuickSave(): SaveState? {
            val result = transientStashedState
            transientStashedState = null
            return result
        }

        fun getAndResetTransientSaveRAMState(): ByteArray? {
            val result = transientSRAMState
            transientSRAMState = null
            return result
        }
    }
}
