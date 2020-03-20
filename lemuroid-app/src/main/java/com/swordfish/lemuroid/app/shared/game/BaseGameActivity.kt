package com.swordfish.lemuroid.app.shared.game

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.coreoptions.CoreOption
import com.swordfish.lemuroid.app.shared.GameMenuContract
import com.swordfish.lemuroid.app.mobile.feature.settings.SettingsManager
import com.swordfish.lemuroid.app.shared.ImmersiveActivity
import com.swordfish.lemuroid.common.dump
import com.swordfish.lemuroid.lib.core.CoreVariable
import com.swordfish.lemuroid.lib.core.CoreVariablesManager
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.saves.SavesManager
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import com.swordfish.lemuroid.lib.util.subscribeBy
import com.swordfish.libretrodroid.GLRetroView
import com.swordfish.libretrodroid.Variable
import com.swordfish.touchinput.events.OptionType
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

abstract class BaseGameActivity : ImmersiveActivity() {

    protected lateinit var system: GameSystem
    protected lateinit var containerLayout: ConstraintLayout
    protected lateinit var gameViewLayout: FrameLayout
    protected lateinit var overlayLayout: FrameLayout

    @Inject lateinit var settingsManager: SettingsManager
    @Inject lateinit var savesManager: SavesManager
    @Inject lateinit var retrogradeDb: RetrogradeDatabase
    @Inject lateinit var coreVariablesManager: CoreVariablesManager

    protected var retroGameView: GLRetroView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        containerLayout = findViewById(R.id.game_container)
        gameViewLayout = findViewById(R.id.gameview_layout)
        overlayLayout = findViewById(R.id.overlay_layout)

        system = GameSystem.findById(intent.getStringExtra(EXTRA_SYSTEM_ID))

        val directoriesManager = DirectoriesManager(applicationContext)

        try {
            initializeRetroGameView(directoriesManager, settingsManager.simulateScreen)
        } catch (e: Exception) {
            Timber.e(e, "Failed running game load")
            retroGameView = null
            displayCannotLoadGameMessage()
        }

        getAndResetTransientSaveRAMState()?.let {
            retroGameView?.unserializeSRAM(it)
        }

        getAndResetTransientQuickSave()?.let {
            restoreQuickSaveAsync(it)
        }

        setupPhysicalPad()

        retroGameView?.requestFocus()

        if (retroGameView != null && settingsManager.autoSave && !system.supportsAutosave) {
            displayToast(R.string.game_toast_autosave_not_supported)
        }
    }

    /* On some cores unserialize fails with no reason. So we need to try multiple times. */
    private fun restoreQuickSaveAsync(saveGame: ByteArray) {
        if (!isAutoSaveEnabled()) {
            return
        }

        Timber.i("Loading saved state of ${saveGame.size} bytes")

        getRetryRestoreQuickSave(saveGame)
                .subscribeOn(Schedulers.io())
                .autoDispose(scope())
                .subscribe()
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
        AlertDialog.Builder(this)
            .setMessage(R.string.game_dialog_cannot_load_game)
            .setPositiveButton(R.string.ok) { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    protected abstract fun displayToast(id: Int)

    protected fun displayOptionsDialog() {
        retrieveCurrentGame()
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribeBy { game ->
                val intent = Intent(this, getDialogClass()).apply {

                    val options = getCoreOptions().filter { it.variable.key in system.exposedSettings }
                    this.putExtra(GameMenuContract.EXTRA_CORE_OPTIONS, options.toTypedArray())
                    this.putExtra(GameMenuContract.EXTRA_DISKS, retroGameView?.getAvailableDisks() ?: 0)
                    this.putExtra(GameMenuContract.EXTRA_GAME_ID, game.id)
                    this.putExtra(GameMenuContract.EXTRA_SYSTEM_ID, game.systemId)
                }
                startActivityForResult(intent, DIALOG_REQUEST)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                // TODO FILIPPO... Understand what to do with animations...
            }
    }

    protected abstract fun getDialogClass(): Class<out Activity>

    protected abstract fun getShaderForSystem(useShader: Boolean, system: GameSystem): Int

    protected fun isAutoSaveEnabled(): Boolean {
        return system.supportsAutosave && settingsManager.autoSave
    }

    override fun onResume() {
        super.onResume()

        coreVariablesManager.getCoreOptionsForSystem(system)
                .autoDispose(scope())
                .subscribeBy({}) {
                    updateCoreVariables(it)
                }

        retroGameView?.onResume()
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

    override fun onPause() {
        retroGameView?.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        retroGameView?.onDestroy()
        super.onDestroy()
    }

    protected fun getRetryRestoreQuickSave(saveGame: ByteArray) = Completable.fromCallable {
        val retroGameView = retroGameView ?: return@fromCallable null
        var times = 10
        while (!retroGameView.unserializeState(saveGame) && times > 0) {
            Thread.sleep(200)
            times--
        }
    }

    protected fun handlePadOption(option: OptionType) {
        when (option) {
            OptionType.SETTINGS -> displayOptionsDialog()
        }
    }

    private fun setupPhysicalPad() {
        retroGameView?.getGameKeyEvents()
            ?.filter { it.keyCode in GAMEPAD_MENU_SHORTCUT }
            ?.scan(mutableSetOf<Int>()) { keys, event ->
                if (event.action == KeyEvent.ACTION_DOWN) {
                    keys.add(event.keyCode)
                } else if (event.action == KeyEvent.ACTION_UP) {
                    keys.remove(event.keyCode)
                }
                keys
            }
            ?.doOnNext {
                if (it.containsAll(GAMEPAD_MENU_SHORTCUT)) {
                    displayOptionsDialog()
                }
            }
            ?.subscribeOn(Schedulers.single())
            ?.autoDispose(scope())
            ?.subscribe()

        retroGameView?.getConnectedGamepads()
            ?.map { it > 0 }
            ?.distinctUntilChanged()
            ?.autoDispose(scope())
            ?.subscribe { gamepadsConnected ->
                if (gamepadsConnected) {
                    displayToast(R.string.game_toast_settings_button_using_gamepad)
                }
            }
    }

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
        return retrieveCurrentGame().flatMapCompletable { game ->
            val saveRAMCompletable = getSaveRAMCompletable(game)
            val autoSaveCompletable = getAutoSaveCompletable(game)

            saveRAMCompletable.andThen(autoSaveCompletable)
                    .doOnComplete { finish() }
        }
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

        return retrieveCurrentGame()
            .map { it to retroGameView.serializeState() }
            .doOnSuccess { (_, data) -> Timber.i("Storing quicksave with size: ${data.size}") }
            .flatMapCompletable { (game, data) -> savesManager.setSlotSave(game, data, system, index) }
    }

    private fun loadSlot(index: Int): Completable {
        val retroGameView = retroGameView ?: return Completable.complete()

        return retrieveCurrentGame()
            .flatMap { savesManager.getSlotSave(it, system, index) }
            .map { retroGameView.unserializeState(it) }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess { if (!it) displayToast(R.string.game_toast_load_state_failed) }
            .ignoreElement()
    }

    private fun retrieveCurrentGame(): Maybe<Game> {
        val gameId = intent.getIntExtra(EXTRA_GAME_ID, -1)
        return retrogradeDb.gameDao()
            .selectById(gameId)
            .subscribeOn(Schedulers.io())
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

    inner class OrientationHandler {

        fun handleOrientationChange(orientation: Int) {
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {

                // Finally we should also avoid system bars. Touch element might appear under system bars, or the game
                // view might be cut due to rounded corners.
                setContainerWindowsInsets(top = true, bottom = true)
                changeGameViewConstraints(ConstraintSet.BOTTOM, ConstraintSet.TOP)
            } else {
                changeGameViewConstraints(ConstraintSet.BOTTOM, ConstraintSet.BOTTOM)
                setContainerWindowsInsets(top = false, bottom = true)
            }
        }

        private fun changeGameViewConstraints(gameViewConstraint: Int, padConstraint: Int) {
            val constraintSet = ConstraintSet()
            constraintSet.clone(containerLayout)
            constraintSet.connect(R.id.gameview_layout, gameViewConstraint, R.id.overlay_layout, padConstraint, 0)
            constraintSet.applyTo(containerLayout)
        }

        private fun setContainerWindowsInsets(top: Boolean, bottom: Boolean) {
            containerLayout.setOnApplyWindowInsetsListener { v, insets ->
                val topInset = if (top) { insets.systemWindowInsetTop } else { 0 }
                val bottomInset = if (bottom) { insets.systemWindowInsetBottom } else { 0 }
                v.setPadding(0, topInset, 0, bottomInset)
                insets.consumeSystemWindowInsets()
            }
            containerLayout.requestApplyInsets()
        }
    }

    companion object {
        // TODO: We should handle gamepads without these buttons
        val GAMEPAD_MENU_SHORTCUT = setOf(KeyEvent.KEYCODE_BUTTON_THUMBL, KeyEvent.KEYCODE_BUTTON_THUMBR)
        const val EXTRA_GAME_ID = "game_id"
        const val EXTRA_SYSTEM_ID = "system_id"
        const val EXTRA_CORE_PATH = "core_path"
        const val EXTRA_GAME_PATH = "game_path"
        const val EXTRA_CORE_VARIABLES = "core_variables"

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
