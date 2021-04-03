package com.swordfish.lemuroid.lib.controller

import android.content.Context
import android.content.SharedPreferences
import com.swordfish.touchinput.controller.R
import kotlin.math.roundToInt
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Single

class TouchControllerSettingsManager(
    private val context: Context,
    private val controllerID: TouchControllerID,
    private val sharedPreferences: Lazy<SharedPreferences>,
    private val orientation: Orientation
) {
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

    fun retrieveSettings(): Single<Settings> {
        return Single.fromCallable { sharedPreferences.get() }
            .map {
                Settings(
                    scale = indexToFloat(
                        it.getInt(
                            getPreferenceString(R.string.pref_key_virtual_pad_scale, orientation),
                            floatToIndex(DEFAULT_SCALE)
                        )
                    ),
                    rotation = indexToFloat(
                        it.getInt(
                            getPreferenceString(R.string.pref_key_virtual_pad_rotation, orientation),
                            floatToIndex(DEFAULT_ROTATION)
                        )
                    ),
                    marginX = indexToFloat(
                        it.getInt(
                            getPreferenceString(R.string.pref_key_virtual_pad_margin_x, orientation),
                            floatToIndex(DEFAULT_MARGIN_X)
                        )
                    ),
                    marginY = indexToFloat(
                        it.getInt(
                            getPreferenceString(R.string.pref_key_virtual_pad_margin_y, orientation),
                            floatToIndex(DEFAULT_MARGIN_Y)
                        )
                    )
                )
            }
    }

    fun storeSettings(settings: Settings): Completable {
        return Single.fromCallable { sharedPreferences.get() }
            .doOnSuccess {
                it.edit().apply {
                    putInt(
                        getPreferenceString(R.string.pref_key_virtual_pad_scale, orientation),
                        floatToIndex(settings.scale)
                    ).apply()
                    putInt(
                        getPreferenceString(R.string.pref_key_virtual_pad_rotation, orientation),
                        floatToIndex(settings.rotation)
                    ).apply()
                    putInt(
                        getPreferenceString(R.string.pref_key_virtual_pad_margin_x, orientation),
                        floatToIndex(settings.marginX)
                    ).apply()
                    putInt(
                        getPreferenceString(R.string.pref_key_virtual_pad_margin_y, orientation),
                        floatToIndex(settings.marginY)
                    ).apply()
                }
            }
            .ignoreElement()
    }

    private fun indexToFloat(index: Int): Float = index / 100f

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
