package com.swordfish.lemuroid.app.shared.rumble

enum class RumbleDestination(val preferenceValue: String) {
    AUTO("auto"),
    CONTROLLER("controller"),
    DEVICE("device");

    companion object {
        fun fromPreferenceValue(value: String) =
            entries.firstOrNull { it.preferenceValue == value } ?: CONTROLLER
    }
}
