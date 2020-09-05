package com.swordfish.lemuroid.app.shared.settings

import android.content.Context
import android.content.SharedPreferences
import android.hardware.input.InputManager
import android.view.InputDevice
import android.view.KeyEvent
import androidx.preference.PreferenceManager
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

class GamePadManager(context: Context) {

    private val inputManager = context.getSystemService(Context.INPUT_SERVICE) as InputManager
    private val sharedPreferences: SharedPreferences = getDefaultSharedPreferences(context)

    private fun getDefaultSharedPreferences(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun getBindings(inputDevice: InputDevice): Single<Map<Int, Int>> {
        return Observable.fromIterable(INPUT_KEYS)
            .flatMapSingle { keyCode ->
                retrieveMappingFromPreferences(
                    inputDevice,
                    keyCode
                ).map { keyCode to it }
            }
            .toList()
            .map { it.toMap() }
            .subscribeOn(Schedulers.io())
    }

    fun resetAllBindings(): Completable {
        val actionCompletable = Completable.fromAction {
            val editor = sharedPreferences.edit()
            sharedPreferences.all.keys
                .filter { it.startsWith(GAME_PAD_PREFERENCE_BASE_KEY) }
                .forEach { editor.remove(it) }
            editor.commit()
        }
        return actionCompletable.subscribeOn(Schedulers.io())
    }

    fun getDistinctGamePads(): List<InputDevice> {
        return getAllGamePads().distinctBy { getSharedPreferencesId(it) }
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
                .filter { it.controllerNumber > 0 }
                .distinctBy { it.controllerNumber }
                .sortedBy { it.controllerNumber }
        }.getOrNull() ?: listOf()
    }

    companion object {
        private const val GAME_PAD_PREFERENCE_BASE_KEY = "pref_key_gamepad_binding"

        private fun getSharedPreferencesId(inputDevice: InputDevice) = inputDevice.descriptor

        fun computeKeyBindingPreference(inputDevice: InputDevice, keyCode: Int) =
            "${GAME_PAD_PREFERENCE_BASE_KEY}_${getSharedPreferencesId(inputDevice)}_$keyCode"

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
