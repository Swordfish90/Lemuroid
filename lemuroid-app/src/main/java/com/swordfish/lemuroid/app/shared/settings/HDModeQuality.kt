package com.swordfish.lemuroid.app.shared.settings

enum class HDModeQuality {
    LOW,
    MEDIUM,
    HIGH,
    ;

    companion object {
        fun parse(value: Int): HDModeQuality {
            val result = kotlin.runCatching { HDModeQuality.values()[value] }
            return result.getOrNull() ?: MEDIUM
        }
    }
}
