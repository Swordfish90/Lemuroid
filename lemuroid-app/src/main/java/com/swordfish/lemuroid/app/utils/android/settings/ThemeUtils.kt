package com.swordfish.lemuroid.app.utils.android.settings

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

@Composable
fun isAppInDarkTheme(themeMode: String): Boolean {
    return when (themeMode) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }
}
