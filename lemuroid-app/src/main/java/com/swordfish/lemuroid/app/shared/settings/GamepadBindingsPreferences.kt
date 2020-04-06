package com.swordfish.lemuroid.app.shared.settings

import android.content.Context
import android.view.KeyEvent
import androidx.preference.ListPreference
import androidx.preference.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.swordfish.lemuroid.R
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class GamepadBindingsPreferences(private val rxSharedPreferences: RxSharedPreferences) {

    private val keys by lazy {
        listOf(
            Key(KeyEvent.KEYCODE_BUTTON_A, "A"),
            Key(KeyEvent.KEYCODE_BUTTON_B, "B"),
            Key(KeyEvent.KEYCODE_BUTTON_X, "X"),
            Key(KeyEvent.KEYCODE_BUTTON_Y, "Y"),
            Key(KeyEvent.KEYCODE_BUTTON_START, "Start"),
            Key(KeyEvent.KEYCODE_BUTTON_SELECT, "Select"),
            Key(KeyEvent.KEYCODE_BUTTON_L1, "L1"),
            Key(KeyEvent.KEYCODE_BUTTON_L2, "L2"),
            Key(KeyEvent.KEYCODE_BUTTON_R1, "R1"),
            Key(KeyEvent.KEYCODE_BUTTON_R2, "R2"),
            Key(KeyEvent.KEYCODE_BUTTON_THUMBL, "L3"),
            Key(KeyEvent.KEYCODE_BUTTON_THUMBR, "R3")
        )
    }

    private val codeKeyMap: Map<Int, String> by lazy {
        keys.map { it.code to it.text }
            .toMap()
    }

    fun getPreferences(context: Context): List<Preference> {
        return keys.map { getPreferenceForKey(context, it) }
    }

    private fun getPreferenceForKey(context: Context, key: Key): Preference {
        val preference = ListPreference(context)
        preference.key = computeKeyBindingPreference(key)
        preference.title = context.resources.getString(R.string.settings_gamepad_button_name, key.text)
        preference.entries = keys.map { it.text } .toTypedArray()
        preference.entryValues = keys.map { it.code.toString() } .toTypedArray()
        preference.setValueIndex(keys.indexOf(key))
        preference.setDefaultValue(key.code.toString())
        preference.summaryProvider = Preference.SummaryProvider<ListPreference> {
            codeKeyMap[it.value.toInt()]
        }
        preference.isIconSpaceReserved = false
        return preference
    }

    fun getMappings(): Single<Map<Int, Int>> {
        return Observable.fromIterable(keys)
            .flatMapSingle { key -> retrieveBindingFromPreferences(key).map { key.code to it } }
            .toList()
            .map { it.toMap() }
    }

    private fun retrieveBindingFromPreferences(key: Key): Single<Int> {
        return rxSharedPreferences.getInteger(computeKeyBindingPreference(key))
            .asObservable()
            .subscribeOn(Schedulers.io())
            .first(key.code)
    }

    data class Key(val code: Int, val text: String)

    companion object {
        private const val PREFERENCE_BASE_KEY = "pref_key_gamepad_binding"

        private fun computeKeyBindingPreference(key: Key) = "${PREFERENCE_BASE_KEY}_${key.text.toLowerCase()}"
    }

    @dagger.Module
    class Module
}
