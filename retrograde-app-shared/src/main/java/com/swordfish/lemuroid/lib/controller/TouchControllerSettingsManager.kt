package com.swordfish.lemuroid.lib.controller

import android.content.Context
import android.content.SharedPreferences
import com.swordfish.lemuroid.common.kotlin.SharedPreferencesDelegates
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper
import com.swordfish.touchinput.controller.R
import kotlin.math.roundToInt

class TouchControllerSettingsManager(
    private val context: Context,
    private val controllerID: TouchControllerID,
    orientation: Orientation
) {
    private val sharedPreferences: SharedPreferences =
        SharedPreferencesHelper.getSharedPreferences(context)

    private var scale: Float by SharedPreferencesDelegates.PercentageDelegate(
        sharedPreferences,
        getPreferenceString(R.string.pref_key_virtual_pad_scale, orientation),
        floatToIndex(DEFAULT_SCALE),
        100
    )

    private var rotation: Float by SharedPreferencesDelegates.PercentageDelegate(
        sharedPreferences,
        getPreferenceString(R.string.pref_key_virtual_pad_rotation, orientation),
        floatToIndex(DEFAULT_ROTATION),
        100
    )

    private var marginX: Float by SharedPreferencesDelegates.PercentageDelegate(
        sharedPreferences,
        getPreferenceString(R.string.pref_key_virtual_pad_margin_x, orientation),
        floatToIndex(DEFAULT_MARGIN_X),
        100
    )

    private var marginY: Float by SharedPreferencesDelegates.PercentageDelegate(
        sharedPreferences,
        getPreferenceString(R.string.pref_key_virtual_pad_margin_y, orientation),
        floatToIndex(DEFAULT_MARGIN_Y),
        100
    )

    enum class Orientation {
        PORTRAIT,
        LANDSCAPE
    }

    data class Settings(
        val scale: Float = DEFAULT_SCALE,
        val rotation: Float = DEFAULT_ROTATION,
        val marginX: Float = DEFAULT_MARGIN_X,
        val marginY: Float = DEFAULT_MARGIN_Y
    )

    fun retrieveSettings(): Settings {
        return Settings(scale, rotation, marginX, marginY)
    }

    fun storeSettings(settings: Settings) {
        this.scale = settings.scale
        this.rotation = settings.rotation
        this.marginX = settings.marginX
        this.marginY = settings.marginY
    }

    private fun floatToIndex(value: Float): Int = (value * 100).roundToInt()

    companion object {
        const val DEFAULT_SCALE = 0.5f
        const val DEFAULT_ROTATION = 0.0f
        const val DEFAULT_MARGIN_X = 0.0f
        const val DEFAULT_MARGIN_Y = 0.0f

        const val MAX_ROTATION = 45f
        const val MIN_SCALE = 0.75f
        const val MAX_SCALE = 1.5f

        const val MAX_MARGINS = 96f
    }

    private fun getPreferenceString(preferenceStringId: Int, orientation: Orientation): String {
        return "${context.getString(preferenceStringId)}_${controllerID}_${orientation.ordinal}"
    }
}
