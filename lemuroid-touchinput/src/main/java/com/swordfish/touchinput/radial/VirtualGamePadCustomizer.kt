package com.swordfish.touchinput.radial

import android.app.Activity
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
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.ui.setVisibleOrGone
import com.swordfish.touchinput.controller.R

class VirtualGamePadCustomizer(
    private val virtualGamePadSettingsManager: VirtualGamePadSettingsManager,
    system: GameSystem
) {

    private val displayRotation = system.virtualGamePadOptions.hasRotation

    fun displayPortraitDialog(
        activity: Activity,
        parentView: ViewGroup,
        virtualGamePad: LemuroidVirtualGamePad
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
                virtualGamePad.padScale = value
            }

        customView.findViewById<TextView>(R.id.touch_textview_rotation)
            ?.setVisibleOrGone(displayRotation)

        customView.findViewById<Slider>(R.id.touch_slider_rotation)?.apply {
            setVisibleOrGone(displayRotation)
            addOnChangeListener { _, value, _ ->
                virtualGamePadSettingsManager.portraitRotation = value
                virtualGamePad.padRotation = value
            }
        }

        customView.findViewById<Button>(R.id.touch_button_close)?.setOnClickListener {
            popupWindow.dismiss()
        }

        customView.findViewById<Button>(R.id.touch_button_reset)?.setOnClickListener {
            virtualGamePadSettingsManager.resetPortrait()
            loadPortraitSettingsIntoGamePad(virtualGamePad)
            loadPortraitSettingsPopupWindow(customView)
        }

        loadPortraitSettingsPopupWindow(customView)

        popupWindow.showAtLocation(parentView, Gravity.CENTER, 0, 0)
        return popupWindow
    }

    fun displayLandscapeDialog(
        activity: Activity,
        parent: ViewGroup,
        virtualGamePad: LemuroidVirtualGamePad
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
                virtualGamePad.padRotation = value
            }
        }

        customView.findViewById<Slider>(R.id.touch_slider_size)
            ?.addOnChangeListener { _, value, _ ->
                virtualGamePadSettingsManager.landscapeScale = value
                virtualGamePad.padScale = value
            }

        customView.findViewById<Slider>(R.id.touch_slider_ypos)
            ?.addOnChangeListener { _, value, _ ->
                virtualGamePadSettingsManager.landscapeOffsetY = value
                virtualGamePad.padOffsetY = value
            }

        customView.findViewById<Button>(R.id.touch_button_close)?.setOnClickListener {
            popupWindow.dismiss()
        }

        customView.findViewById<Button>(R.id.touch_button_reset)?.setOnClickListener {
            virtualGamePadSettingsManager.resetLandscape()
            loadLandscapeSettingsIntoGamePad(virtualGamePad)
            loadLandscapeSettingsIntoPopupWindow(customView)
        }

        loadLandscapeSettingsIntoPopupWindow(customView)

        popupWindow.showAtLocation(parent, Gravity.CENTER, 0, 0)
        return popupWindow
    }

    fun loadPortraitSettingsIntoGamePad(virtualGamePad: LemuroidVirtualGamePad) {
        virtualGamePad.padScale = virtualGamePadSettingsManager.portraitScale
        virtualGamePad.padRotation = virtualGamePadSettingsManager.portraitRotation
        virtualGamePad.padOffsetY = 0f
    }

    fun loadLandscapeSettingsIntoGamePad(virtualGamePad: LemuroidVirtualGamePad) {
        virtualGamePad.padScale = virtualGamePadSettingsManager.landscapeScale
        virtualGamePad.padRotation = virtualGamePadSettingsManager.landscapeRotation
        virtualGamePad.padOffsetY = virtualGamePadSettingsManager.landscapeOffsetY
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
