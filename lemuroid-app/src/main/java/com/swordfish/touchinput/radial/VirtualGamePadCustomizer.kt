package com.swordfish.touchinput.radial

import android.content.Context
import android.content.pm.ActivityInfo
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.PopupWindow
import android.widget.TextView
import com.google.android.material.slider.Slider
import com.swordfish.lemuroid.app.mobile.feature.game.GameActivity
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.ui.setVisibleOrGone
import com.swordfish.touchinput.controller.R

class VirtualGamePadCustomizer(
    private val virtualGamePadSettingsManager: VirtualGamePadSettingsManager,
    system: GameSystem
) {

    private val displayRotation = system.virtualGamePadOptions.hasRotation

    fun displayPortraitDialog(
        activity: GameActivity,
        parentView: ViewGroup
    ): PopupWindow {
        val originalRequestedOrientation = activity.requestedOrientation
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT

        val inflater = parentView.context.getSystemService(
            Context.LAYOUT_INFLATER_SERVICE
        ) as LayoutInflater

        val customView = inflater.inflate(
            R.layout.layout_customize_touch_portrait,
            parentView,
            false
        ) as FrameLayout

        val popupWindow = PopupWindow(
            customView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        popupWindow.setOnDismissListener {
            activity.requestedOrientation = originalRequestedOrientation
        }

        customView.findViewById<Slider>(R.id.touch_slider_size)
            ?.addOnChangeListener { _, value, _ ->
                virtualGamePadSettingsManager.portraitScale = value
                activity.padScale = value
            }

        customView.findViewById<TextView>(R.id.touch_textview_rotation)
            ?.setVisibleOrGone(displayRotation)

        customView.findViewById<Slider>(R.id.touch_slider_rotation)?.apply {
            setVisibleOrGone(displayRotation)
            addOnChangeListener { _, value, _ ->
                virtualGamePadSettingsManager.portraitRotation = value
                activity.padRotation = value
            }
        }

        customView.findViewById<Button>(R.id.touch_button_close)?.setOnClickListener {
            popupWindow.dismiss()
        }

        customView.findViewById<Button>(R.id.touch_button_reset)?.setOnClickListener {
            virtualGamePadSettingsManager.resetPortrait()
            loadPortraitSettingsIntoGamePad(activity)
            loadPortraitSettingsPopupWindow(customView)
        }

        loadPortraitSettingsPopupWindow(customView)

        popupWindow.showAtLocation(parentView, Gravity.CENTER, 0, 0)
        return popupWindow
    }

    fun displayLandscapeDialog(
        activity: GameActivity,
        parent: ViewGroup
    ): PopupWindow {
        val originalRequestedOrientation = activity.requestedOrientation
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        val inflater = parent.context.getSystemService(
            Context.LAYOUT_INFLATER_SERVICE
        ) as LayoutInflater

        val customView =
            inflater.inflate(
                R.layout.layout_customize_touch_landscape,
                parent,
                false
            ) as FrameLayout

        val popupWindow = PopupWindow(
            customView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        popupWindow.setOnDismissListener {
            activity.requestedOrientation = originalRequestedOrientation
        }

        customView.findViewById<TextView>(R.id.touch_textview_rotation)
            ?.setVisibleOrGone(displayRotation)

        customView.findViewById<Slider>(R.id.touch_slider_rotation)?.apply {
            setVisibleOrGone(displayRotation)
            addOnChangeListener { _, value, _ ->
                virtualGamePadSettingsManager.landscapeRotation = value
                activity.padRotation = value
            }
        }

        customView.findViewById<Slider>(R.id.touch_slider_size)
            ?.addOnChangeListener { _, value, _ ->
                virtualGamePadSettingsManager.landscapeScale = value
                activity.padScale = value
            }

        customView.findViewById<Slider>(R.id.touch_slider_ypos)
            ?.addOnChangeListener { _, value, _ ->
                virtualGamePadSettingsManager.landscapeOffsetY = value
                activity.padOffsetY = value
            }

        customView.findViewById<Button>(R.id.touch_button_close)?.setOnClickListener {
            popupWindow.dismiss()
        }

        customView.findViewById<Button>(R.id.touch_button_reset)?.setOnClickListener {
            virtualGamePadSettingsManager.resetLandscape()
            loadLandscapeSettingsIntoGamePad(activity)
            loadLandscapeSettingsIntoPopupWindow(customView)
        }

        loadLandscapeSettingsIntoPopupWindow(customView)

        popupWindow.showAtLocation(parent, Gravity.CENTER, 0, 0)
        return popupWindow
    }

    fun loadPortraitSettingsIntoGamePad(activity: GameActivity) {
        activity.padScale = virtualGamePadSettingsManager.portraitScale
        activity.padRotation = virtualGamePadSettingsManager.portraitRotation
        activity.padOffsetY = 0f
    }

    fun loadLandscapeSettingsIntoGamePad(activity: GameActivity) {
        activity.padScale = virtualGamePadSettingsManager.landscapeScale
        activity.padRotation = virtualGamePadSettingsManager.landscapeRotation
        activity.padOffsetY = virtualGamePadSettingsManager.landscapeOffsetY
    }

    private fun loadPortraitSettingsPopupWindow(view: View) {
        view.findViewById<Slider>(
            R.id.touch_slider_size
        )?.value = virtualGamePadSettingsManager.portraitScale

        view.findViewById<Slider>(
            R.id.touch_slider_rotation
        )?.value = virtualGamePadSettingsManager.portraitRotation
    }

    private fun loadLandscapeSettingsIntoPopupWindow(view: View) {
        view.findViewById<Slider>(
            R.id.touch_slider_size
        )?.value = virtualGamePadSettingsManager.landscapeScale

        view.findViewById<Slider>(
            R.id.touch_slider_rotation
        )?.value = virtualGamePadSettingsManager.landscapeRotation

        view.findViewById<Slider>(
            R.id.touch_slider_ypos
        )?.value = virtualGamePadSettingsManager.landscapeOffsetY
    }
}
