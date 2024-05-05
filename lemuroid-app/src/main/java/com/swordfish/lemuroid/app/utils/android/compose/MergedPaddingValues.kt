package com.swordfish.lemuroid.app.utils.android.compose

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class MergedPaddingValues(val paddings: List<PaddingValues>) {
    @Composable
    fun asPaddingValues(): PaddingValues {
        val direction = LocalLayoutDirection.current

        return PaddingValues(
            start = paddings.sumBy { it.calculateStartPadding(direction) },
            end = paddings.sumBy { it.calculateEndPadding(direction) },
            top = paddings.sumBy { it.calculateTopPadding() },
            bottom = paddings.sumBy { it.calculateBottomPadding() },
        )
    }

    operator fun plus(other: MergedPaddingValues): MergedPaddingValues {
        return MergedPaddingValues(this.paddings + other.paddings)
    }

    operator fun plus(other: PaddingValues): MergedPaddingValues {
        return MergedPaddingValues(this.paddings + listOf(other))
    }

    private fun List<PaddingValues>.sumBy(paddings: (PaddingValues) -> Dp): Dp {
        return this.map { paddings(it) }
            .fold(0.dp) { current, value -> current.plus(value) }
    }
}

operator fun PaddingValues.plus(other: PaddingValues): MergedPaddingValues {
    return MergedPaddingValues(listOf(this) + listOf(other))
}
