package com.swordfish.lemuroid.app.shared.game

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.InputDevice
import android.view.InputEvent
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.jakewharton.rxrelay2.PublishRelay
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.coreoptions.CoreOption
import com.swordfish.lemuroid.app.shared.GameMenuContract
import com.swordfish.lemuroid.app.mobile.feature.settings.SettingsManager
import com.swordfish.lemuroid.app.shared.ImmersiveActivity
import com.swordfish.lemuroid.app.shared.settings.GamePadManager
import com.swordfish.lemuroid.common.dump
import com.swordfish.lemuroid.lib.core.CoreVariable
import com.swordfish.lemuroid.app.utils.android.displayErrorDialog
import com.swordfish.lemuroid.lib.core.CoreVariablesManager
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.saves.SavesManager
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import com.swordfish.lemuroid.lib.util.subscribeBy
import com.swordfish.libretrodroid.GLRetroView
import com.swordfish.libretrodroid.GLRetroView.Companion.MOTION_SOURCE_ANALOG_LEFT
import com.swordfish.libretrodroid.GLRetroView.Companion.MOTION_SOURCE_ANALOG_RIGHT
import com.swordfish.libretrodroid.GLRetroView.Companion.MOTION_SOURCE_DPAD
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
import javax.inject.Inject

abstract class BaseGameActivity : ImmersiveActivity() {

    protected lateinit var game: Game
    protected lateinit var system: GameSystem
    protected lateinit var containerLayout: ConstraintLayout
    protected lateinit var gameViewLayout: FrameLayout
    protected lateinit var overlayLayout: FrameLayout

    @Inject lateinit var settingsManager: SettingsManager
    @Inject lateinit var savesManager: SavesManager
    @Inject lateinit var coreVariablesManager: CoreVariablesManager
    @Inject lateinit var gamePadManager: GamePadManager

    private val keyEventsSubjects: PublishRelay<KeyEvent> = PublishRelay.create()
    private val motionEventsSubjects: PublishRelay<MotionEvent> = PublishRelay.create()

    protected var retroGameView: GLRetroView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        containerLayout = findViewById(R.id.game_container)
        gameViewLayout = findViewById(R.id.gameview_layout)
        overlayLayout = findViewById(R.id.overlay_layout)

        game = intent.getSerializableExtra(EXTRA_GAME) as Game
        system = GameSystem.findById(game.systemId)

        val directoriesManager = DirectoriesManager(applicationContext)

        try {
            initializeRetroGameView(directoriesManager, settingsManager.simulateScreen)
        } catch (e: Exception) {
            Timber.e(e, "Failed running game load")
            retroGameView = null
            displayCannotLoadGameMessage()
        }

        retrieveSRAMData()?.let {
            retroGameView?.unserializeSRAM(it)
        }

        retrieveAutoSaveData()?.let {
            restoreAutoSaveAsync(it)
        }

        setupPhysicalPad()
    }

    // If the activity is garbage collected we are losing its state. To avoid overwriting the previous autosave we just
    // reload the previous one. This is far from perfect but definitely improves the current behaviour.
    private fun retrieveAutoSaveData(): ByteArray? {
        if (intent.getBooleanExtra(EXTRA_LOAD_AUTOSAVE, false)) {
            return getAndResetTransientQuickSave() ?: savesManager.getAutoSave(game, system).blockingGet()
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
    private fun restoreAutoSaveAsync(saveGame: ByteArray) {
        if (!isAutoSaveEnabled()) {
            return
        }

        // PPSSPP and Mupen64 initialize some state while rendering the first frame, so we have to wait before restoring
        // the autosave.
        retroGameView?.getGLRetroEvents()
            ?.filter { it is GLRetroView.GLRetroEvents.FrameRendered }
            ?.firstElement()
            ?.flatMapCompletable { getRetryRestoreQuickSave(saveGame) }
            ?.subscribeOn(Schedulers.io())
            ?.autoDispose(scope())
            ?.subscribe()
    }

    private fun initializeRetroGameView(directoriesManager: DirectoriesManager, useShaders: Boolean) {
        retroGameView = GLRetroView(
                this,
                intent.getStringExtra(EXTRA_CORE_PATH)!!,
                intent.getStringExtra(EXTRA_GAME_PATH)!!,
                directoriesManager.getSystemDirectory().absolutePath,
                directoriesManager.getSavesDirectory().absolutePath,
                getShaderForSystem(useShaders, system)
        )
        retroGameView?.isFocusable = false
        retroGameView?.isFocusableInTouchMode = false
        retroGameView?.onCreate()

        val coreVariables = intent.getSerializableExtra(EXTRA_CORE_VARIABLES) as Array<CoreVariable>? ?: arrayOf()
        updateCoreVariables(coreVariables.toList())

        gameViewLayout.addView(retroGameView)

        val layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL

        retroGameView?.layoutParams = layoutParams
    }

    private fun displayCannotLoadGameMessage() {
        displayErrorDialog(R.string.game_dialog_cannot_load_game, R.string.ok) {
            finish()
        }
    }

    fun displayToast(id: Int) {
        Toast.makeText(this, id, Toast.LENGTH_SHORT).show()
    }

    fun displayToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    protected fun displayOptionsDialog() {
        val intent = Intent(this, getDialogClass()).apply {
            val options = getCoreOptions().filter { it.variable.key in system.exposedSettings }
            this.putExtra(GameMenuContract.EXTRA_CORE_OPTIONS, options.toTypedArray())
            this.putExtra(GameMenuContract.EXTRA_DISKS, retroGameView?.getAvailableDisks() ?: 0)
            this.putExtra(GameMenuContract.EXTRA_GAME, game)
        }
        startActivityForResult(intent, DIALOG_REQUEST)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    protected abstract fun getDialogClass(): Class<out Activity>

    protected abstract fun getShaderForSystem(useShader: Boolean, system: GameSystem): Int

    private fun isAutoSaveEnabled(): Boolean {
        return settingsManager.autoSave
    }

    override fun onResume() {
        super.onResume()

        coreVariablesManager.getCoreOptionsForSystem(system)
                .autoDispose(scope())
                .subscribeBy({}) {
                    onVariablesRead(it)
                }

        retroGameView?.onResume()
    }

    open fun onVariablesRead(coreVariables: List<CoreVariable>) {
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
        coreVariablesManager.getCoreOptionsForSystem(system)
            .autoDispose(scope())
            .subscribeBy({}) {
                updateCoreVariables(it)
            }
    }

    override fun onPause() {
        retroGameView?.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        retroGameView?.onDestroy()
        super.onDestroy()
    }

    // Now that we wait for the first rendered frame this is probably no longer needed, but we'll keep it just to be sure
    private fun getRetryRestoreQuickSave(saveGame: ByteArray) = Completable.fromCallable {
        val retroGameView = retroGameView ?: return@fromCallable null
        var times = 10
        while (!retroGameView.unserializeState(saveGame) && times > 0) {
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
                displayToast(resources.getString(R.string.game_toast_settings_button_using_gamepad, it.label))
            }

        val firstPlayerPressedKeys = keyEventsSubjects
            .filter { getDevicePort(it) == 0 }
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
        motionEventsSubjects
            .autoDispose(scope())
            .subscribeBy { sendMotionEvent(it) }
    }

    private fun setupGamePadKeys() {
        val bindKeys = Observables.combineLatest(getGamePadBindingsObservable(), keyEventsSubjects)
            .map { (bindings, event) ->
                val port = getDevicePort(event)
                val bindKeyCode = bindings[event.device]?.get(event.keyCode) ?: event.keyCode
                Triple(event.action, port, bindKeyCode)
            }
            .filter { (_, port, _) -> port >= 0 }
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

    private fun getGamePadBindingsObservable(): Observable<Map<InputDevice, Map<Int, Int>>> {
        return gamePadManager.getGamePadsObservable()
            .flatMapSingle { inputDevices ->
                Observable.fromIterable(inputDevices).flatMapSingle { inputDevice ->
                    gamePadManager.getBindings(inputDevice).map { inputDevice to it }
                }.toList()
            }
            .map { it.toMap() }
    }

    private fun sendMotionEvent(event: MotionEvent) {
        val port = getDevicePort(event)
        if (port < 0) return
        when (event.source) {
            InputDevice.SOURCE_JOYSTICK -> {
                if (system.sendLeftStickEventAsDPAD) {
                    sendMergedAsDPADEvents(event, port)
                } else {
                    sendSeparateMotionEvents(event, port)
                }
            }
        }
    }

    private fun sendMergedAsDPADEvents(event: MotionEvent, port: Int) {
        val xAxises = setOf(MotionEvent.AXIS_HAT_X, MotionEvent.AXIS_X)
        val yAxises = setOf(MotionEvent.AXIS_HAT_Y, MotionEvent.AXIS_Y)
        val xVal = xAxises.map { event.getAxisValue(it) }.maxBy { kotlin.math.abs(it) }!!
        val yVal = yAxises.map { event.getAxisValue(it) }.maxBy { kotlin.math.abs(it) }!!
        retroGameView?.sendMotionEvent(MOTION_SOURCE_DPAD, xVal, yVal, port)
    }

    private fun sendSeparateMotionEvents(event: MotionEvent, port: Int) {
        sendMotionEvent(event, MOTION_SOURCE_DPAD, MotionEvent.AXIS_HAT_X, MotionEvent.AXIS_HAT_Y, port)
        sendMotionEvent(event, MOTION_SOURCE_ANALOG_LEFT, MotionEvent.AXIS_X, MotionEvent.AXIS_Y, port)
        sendMotionEvent(event, MOTION_SOURCE_ANALOG_RIGHT, MotionEvent.AXIS_Z, MotionEvent.AXIS_RZ, port)
    }

    private fun sendMotionEvent(event: MotionEvent, source: Int, xAxis: Int, yAxis: Int, port: Int) {
        retroGameView?.sendMotionEvent(source, event.getAxisValue(xAxis), event.getAxisValue(yAxis), port)
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

    private fun getDevicePort(inputEvent: InputEvent) = (inputEvent.device?.controllerNumber ?: 0) - 1

    override fun onBackPressed() {
        autoSaveAndFinish()
    }

    private fun autoSaveAndFinish() {
        getAutoSaveAndFinishCompletable()
            .subscribeOn(Schedulers.io())
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
        val retroGameView = retroGameView ?: return Completable.complete()

        return Single.fromCallable { isAutoSaveEnabled() }
            .filter { it }
            .map { retroGameView.serializeState() }
            .doOnSuccess { Timber.i("Stored autosave file with size: ${it.size}") }
            .flatMapCompletable { savesManager.setAutoSave(game, system, it) }
    }

    private fun getSaveRAMCompletable(game: Game): Completable {
        val retroGameView = retroGameView ?: return Completable.complete()

        return Single.fromCallable { retroGameView.serializeSRAM() }
            .doOnSuccess { Timber.i("Stored sram file with size: ${it.size}") }
            .flatMapCompletable { savesManager.setSaveRAM(game, it) }
    }

    private fun saveSlot(index: Int): Completable {
        val retroGameView = retroGameView ?: return Completable.complete()

        return Single.just(game)
            .map { it to retroGameView.serializeState() }
            .doOnSuccess { (_, data) -> Timber.i("Storing quicksave with size: ${data.size}") }
            .subscribeOn(Schedulers.io())
            .flatMapCompletable { (game, data) -> savesManager.setSlotSave(game, data, system, index) }
    }

    private fun loadSlot(index: Int): Completable {
        val retroGameView = retroGameView ?: return Completable.complete()

        return Single.just(game)
            .flatMapMaybe { savesManager.getSlotSave(it, system, index) }
            .map { retroGameView.unserializeState(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess { if (!it) displayToast(R.string.game_toast_load_state_failed) }
            .ignoreElement()
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
        }
    }

    companion object {
        const val EXTRA_GAME = "game"
        const val EXTRA_CORE_PATH = "core_path"
        const val EXTRA_GAME_PATH = "game_path"
        const val EXTRA_CORE_VARIABLES = "core_variables"
        const val EXTRA_LOAD_SRAM = "load_sram"
        const val EXTRA_LOAD_AUTOSAVE = "load_autosave"

        private const val DIALOG_REQUEST = 100

        private var transientStashedState: ByteArray? = null
        private var transientSRAMState: ByteArray? = null
        /** A full savestate may not fit a bundle, so we need to ask for forgiveness and pass it statically. */
        fun setTransientQuickSave(state: ByteArray?) {
            transientStashedState = state
        }

        fun setTransientSaveRAMState(data: ByteArray?) {
            transientSRAMState = data
        }

        fun getAndResetTransientQuickSave(): ByteArray? {
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
