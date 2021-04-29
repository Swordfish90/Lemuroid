package com.swordfish.lemuroid.app.shared.settings

import android.content.Context
import android.content.SharedPreferences
import android.hardware.input.InputManager
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.gojuno.koptional.Optional
import com.gojuno.koptional.toOptional
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import dagger.Lazy

class GamePadManager(
    context: Context,
    sharedPreferencesFactory: Lazy<SharedPreferences>
) {

    private val inputManager = context.getSystemService(Context.INPUT_SERVICE) as InputManager

    private val sharedPreferences by lazy { sharedPreferencesFactory.get() }

    private val rxSharedPreferences = Single.fromCallable {
        RxSharedPreferences.create(sharedPreferencesFactory.get())
    }

    fun getGamePadsBindingsObservable(): Observable<(InputDevice?)->Map<Int, Int>> {
        return getEnabledGamePadsObservable()
            .observeOn(Schedulers.io())
            .flatMapSingle { inputDevices ->
                Observable.fromIterable(inputDevices).flatMapSingle { inputDevice ->
                    getBindings(inputDevice).map { inputDevice to it }
                }.toList()
            }
            .map { it.toMap() }
            .map { bindings -> { bindings[it] ?: mapOf() } }
    }

    fun getGamePadMenuShortCutObservable(): Observable<Optional<GameMenuShortcut>> {
        return getEnabledGamePadsObservable()
            .observeOn(Schedulers.io())
            .map { devices ->
                devices.firstOrNull()
                    ?.let {
                        sharedPreferences.getString(
                            computeGameMenuShortcutPreference(it),
                            GameMenuShortcut.getDefault(it)?.name
                        )
                    }
                    ?.let { GameMenuShortcut.findByName(it) }
                    .toOptional()
            }
    }

    fun getGamePadsPortMapperObservable(): Observable<(InputDevice?)->Int?> {
        return getEnabledGamePadsObservable().map { gamePads ->
            val portMappings = gamePads
                .mapIndexed { index, inputDevice -> inputDevice.id to index }
                .toMap()
            return@map { inputDevice -> portMappings[inputDevice?.id] }
        }
    }

    private fun getBindings(inputDevice: InputDevice): Single<Map<Int, Int>> {
        return Observable.fromIterable(INPUT_KEYS)
            .flatMapSingle { keyCode ->
                retrieveMappingFromPreferences(
                    inputDevice,
                    keyCode
                ).map { keyCode to it }
            }
            .toList()
            .map { it.toMap() }
    }

    fun resetAllBindings(): Completable {
        val actionCompletable = Completable.fromAction {
            val editor = sharedPreferences.edit()
            sharedPreferences.all.keys
                .filter { it.startsWith(GAME_PAD_BINDING_PREFERENCE_BASE_KEY) }
                .forEach { editor.remove(it) }
            editor.commit()
        }
        return actionCompletable.subscribeOn(Schedulers.io())
    }

    fun getGamePadsObservable(): Observable<List<InputDevice>> {
        val subject = BehaviorSubject.createDefault(getAllGamePads())

        val listener = object : InputManager.InputDeviceListener {
            override fun onInputDeviceAdded(deviceId: Int) {
                subject.onNext(getAllGamePads())
            }

            override fun onInputDeviceChanged(deviceId: Int) {
                subject.onNext(getAllGamePads())
            }

            override fun onInputDeviceRemoved(deviceId: Int) {
                subject.onNext(getAllGamePads())
            }
        }

        return subject
            .doOnSubscribe { inputManager.registerInputDeviceListener(listener, null) }
            .doFinally { inputManager.unregisterInputDeviceListener(listener) }
            .subscribeOn(AndroidSchedulers.mainThread())
    }

    fun getEnabledGamePadsObservable(): Observable<List<InputDevice>> {
        return getGamePadsObservable()
            .flatMap { devices ->
                if (devices.isEmpty()) {
                    return@flatMap Observable.just(listOf())
                }

                val enabledGamePads = devices.map { device ->
                    rxSharedPreferences
                        .flatMapObservable {
                            it.getBoolean(computeEnabledGamePadPreference(device)).asObservable()
                        }
                }

                Observable.combineLatest(enabledGamePads) { results ->
                    devices.filterIndexed { index, _ -> results[index] == true }
                }
            }
    }

    private fun retrieveMappingFromPreferences(
        inputDevice: InputDevice,
        keyCode: Int
    ): Single<Int> {
        val valueSingle = Single.fromCallable {
            val sharedPreferencesKey = computeKeyBindingPreference(inputDevice, keyCode)
            val sharedPreferencesDefault = getDefaultBinding(keyCode).toString()
            sharedPreferences.getString(sharedPreferencesKey, sharedPreferencesDefault)
        }

        return valueSingle.map { it.toInt() }.subscribeOn(Schedulers.io())
    }

    fun getDefaultBinding(keyCode: Int) = DEFAULT_BINDINGS.getValue(keyCode)

    private fun getAllGamePads(): List<InputDevice> {
        return runCatching {
            InputDevice.getDeviceIds()
                .map { InputDevice.getDevice(it) }
                .filter { isGamePad(it) }
                .sortedBy { it.controllerNumber }
        }.getOrNull() ?: listOf()
    }

    private fun isGamePad(device: InputDevice): Boolean {
        return sequenceOf(
            device.name !in BLACKLISTED_DEVICES,
            device.sources and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD,
            device.hasKeys(*MINIMAL_SUPPORTED_KEYS).all { it },
            device.isVirtual.not(),
            device.controllerNumber > 0
        ).all { it }
    }

    companion object {
        private const val GAME_PAD_BINDING_PREFERENCE_BASE_KEY = "pref_key_gamepad_binding"
        private const val GAME_PAD_ENABLED_PREFERENCE_BASE_KEY = "pref_key_gamepad_enabled"

        private fun getSharedPreferencesId(inputDevice: InputDevice) = inputDevice.descriptor

        // This is a last resort, but sadly there are some devices which present keys and the
        // SOURCE_GAMEPAD, so we basically black list them.
        private val BLACKLISTED_DEVICES = setOf(
            "virtual-search"
        )

        private val MINIMAL_SUPPORTED_KEYS = intArrayOf(
            KeyEvent.KEYCODE_BUTTON_A,
            KeyEvent.KEYCODE_BUTTON_B,
            KeyEvent.KEYCODE_BUTTON_X,
            KeyEvent.KEYCODE_BUTTON_Y,
        )

        fun computeEnabledGamePadPreference(inputDevice: InputDevice) =
            "${GAME_PAD_ENABLED_PREFERENCE_BASE_KEY}_${getSharedPreferencesId(inputDevice)}"

        fun computeGameMenuShortcutPreference(inputDevice: InputDevice) =
            "${GAME_PAD_BINDING_PREFERENCE_BASE_KEY}_${getSharedPreferencesId(inputDevice)}_gamemenu"

        fun computeKeyBindingPreference(inputDevice: InputDevice, keyCode: Int) =
            "${GAME_PAD_BINDING_PREFERENCE_BASE_KEY}_${getSharedPreferencesId(inputDevice)}_$keyCode"

        val TRIGGER_MOTIONS_TO_KEYS = mapOf(
            MotionEvent.AXIS_BRAKE to KeyEvent.KEYCODE_BUTTON_L2,
            MotionEvent.AXIS_THROTTLE to KeyEvent.KEYCODE_BUTTON_R2,
            MotionEvent.AXIS_LTRIGGER to KeyEvent.KEYCODE_BUTTON_L2,
            MotionEvent.AXIS_RTRIGGER to KeyEvent.KEYCODE_BUTTON_R2
        )

        val INPUT_KEYS = listOf(
            KeyEvent.KEYCODE_BUTTON_A,
            KeyEvent.KEYCODE_BUTTON_B,
            KeyEvent.KEYCODE_BUTTON_X,
            KeyEvent.KEYCODE_BUTTON_Y,
            KeyEvent.KEYCODE_BUTTON_START,
            KeyEvent.KEYCODE_BUTTON_SELECT,
            KeyEvent.KEYCODE_BUTTON_L1,
            KeyEvent.KEYCODE_BUTTON_R1,
            KeyEvent.KEYCODE_BUTTON_L2,
            KeyEvent.KEYCODE_BUTTON_R2,
            KeyEvent.KEYCODE_BUTTON_THUMBL,
            KeyEvent.KEYCODE_BUTTON_THUMBR,
            KeyEvent.KEYCODE_BUTTON_C,
            KeyEvent.KEYCODE_BUTTON_Z,
            KeyEvent.KEYCODE_BUTTON_1,
            KeyEvent.KEYCODE_BUTTON_2,
            KeyEvent.KEYCODE_BUTTON_3,
            KeyEvent.KEYCODE_BUTTON_4,
            KeyEvent.KEYCODE_BUTTON_5,
            KeyEvent.KEYCODE_BUTTON_6,
            KeyEvent.KEYCODE_BUTTON_7,
            KeyEvent.KEYCODE_BUTTON_8,
            KeyEvent.KEYCODE_BUTTON_9,
            KeyEvent.KEYCODE_BUTTON_10,
            KeyEvent.KEYCODE_BUTTON_11,
            KeyEvent.KEYCODE_BUTTON_12,
            KeyEvent.KEYCODE_BUTTON_13,
            KeyEvent.KEYCODE_BUTTON_14,
            KeyEvent.KEYCODE_BUTTON_15,
            KeyEvent.KEYCODE_BUTTON_16,
            KeyEvent.KEYCODE_BUTTON_MODE
        )

        val OUTPUT_KEYS = listOf(
            KeyEvent.KEYCODE_BUTTON_A,
            KeyEvent.KEYCODE_BUTTON_B,
            KeyEvent.KEYCODE_BUTTON_X,
            KeyEvent.KEYCODE_BUTTON_Y,
            KeyEvent.KEYCODE_BUTTON_START,
            KeyEvent.KEYCODE_BUTTON_SELECT,
            KeyEvent.KEYCODE_BUTTON_L1,
            KeyEvent.KEYCODE_BUTTON_L2,
            KeyEvent.KEYCODE_BUTTON_R1,
            KeyEvent.KEYCODE_BUTTON_R2,
            KeyEvent.KEYCODE_BUTTON_THUMBL,
            KeyEvent.KEYCODE_BUTTON_THUMBR,
            KeyEvent.KEYCODE_BUTTON_MODE,
            KeyEvent.KEYCODE_UNKNOWN
        )

        private val DEFAULT_BINDINGS = mapOf(
            KeyEvent.KEYCODE_BUTTON_A to KeyEvent.KEYCODE_BUTTON_B,
            KeyEvent.KEYCODE_BUTTON_B to KeyEvent.KEYCODE_BUTTON_A,
            KeyEvent.KEYCODE_BUTTON_X to KeyEvent.KEYCODE_BUTTON_Y,
            KeyEvent.KEYCODE_BUTTON_Y to KeyEvent.KEYCODE_BUTTON_X
        ).withDefault { if (it in OUTPUT_KEYS) it else KeyEvent.KEYCODE_UNKNOWN }
    }
}
