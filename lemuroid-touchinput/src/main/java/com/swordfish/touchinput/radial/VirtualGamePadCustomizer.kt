package com.swordfish.touchinput.radial

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.PopupWindow
import com.google.android.material.slider.Slider
import com.swordfish.touchinput.controller.R

class VirtualGamePadCustomizer(
    private val virtualGamePadSettingsManager: VirtualGamePadSettingsManager
) {

    fun displayGamePadCustomizationPopup(parentView: ViewGroup, virtualGamePad: LemuroidVirtualGamePad): PopupWindow {
        val inflater = parentView.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val customView = inflater.inflate(R.layout.layout_customize_touch, parentView, false) as FrameLayout

        val popupWindow = PopupWindow(
            customView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val sizeSlider = customView.findViewById<Slider>(R.id.touch_slider_size)
        val rotationSlider = customView.findViewById<Slider>(R.id.touch_slider_rotation)
        val xOffsetSlider = customView.findViewById<Slider>(R.id.touch_slider_xpos)
        val yOffsetSlider = customView.findViewById<Slider>(R.id.touch_slider_ypos)

        rotationSlider.addOnChangeListener { _, value, _ ->
            virtualGamePadSettingsManager.rotation = value
            virtualGamePad.padRotation = value
        }

        sizeSlider.addOnChangeListener { _, value, _ ->
            virtualGamePadSettingsManager.scale = value
            virtualGamePad.padScale = value
        }

        xOffsetSlider.addOnChangeListener { _, value, _ ->
            virtualGamePadSettingsManager.offsetX = value
            virtualGamePad.padOffsetX = value
        }

        yOffsetSlider.addOnChangeListener { _, value, _ ->
            virtualGamePadSettingsManager.offsetY = value
            virtualGamePad.padOffsetY = value
        }

        fun initializeAllSettings() {
            sizeSlider.value = virtualGamePad.padScale
            rotationSlider.value = virtualGamePad.padRotation
            xOffsetSlider.value = virtualGamePad.padOffsetX
            yOffsetSlider.value = virtualGamePad.padOffsetY
        }

        initializeAllSettings()

        val closeButton = customView.findViewById<Button>(R.id.touch_button_close)
        closeButton.setOnClickListener {
            virtualGamePad.isHighlighted = false
            popupWindow.dismiss()
        }

        val resetButton = customView.findViewById<Button>(R.id.touch_button_reset)
        resetButton.setOnClickListener {
            virtualGamePad.resetSettings()
            initializeAllSettings()
        }

        virtualGamePad.isHighlighted = true

        popupWindow.setOnDismissListener {
            virtualGamePad.isHighlighted = false
        }

        popupWindow.showAtLocation(parentView, Gravity.CENTER, 0, 0)
        return popupWindow
    }
}
