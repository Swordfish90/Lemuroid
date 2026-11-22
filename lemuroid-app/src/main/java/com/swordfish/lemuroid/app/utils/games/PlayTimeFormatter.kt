package com.swordfish.lemuroid.app.utils.games

object PlayTimeFormatter {
    fun format(playTimeMs: Long): String {
        if (playTimeMs <= 0L) return "0m"
        val totalMinutes = playTimeMs / 60_000L
        if (totalMinutes < 1L) return "< 1m"
        val hours = totalMinutes / 60L
        val minutes = totalMinutes % 60L
        return when {
            hours > 0L && minutes > 0L -> "${hours}h ${minutes}m"
            hours > 0L -> "${hours}h"
            else -> "${minutes}m"
        }
    }
}
