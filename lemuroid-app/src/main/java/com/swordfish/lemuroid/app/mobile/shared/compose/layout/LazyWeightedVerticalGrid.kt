package com.swordfish.lemuroid.app.mobile.shared.compose.layout

import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridSlots
import androidx.compose.foundation.lazy.grid.LazyGridSlotsProvider
import androidx.compose.foundation.lazy.grid.LazyGridSpanLayoutProvider.LineConfiguration
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.app.mobile.shared.compose.layout.foundation.LazyGrid
import kotlin.math.roundToInt

@Composable
fun LazyWeightedVerticalGrid(
    modifier: Modifier = Modifier,
    state: LazyGridState = rememberLazyGridState(),
    targetItemWidth: Dp = 144.dp,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    spacing: Dp = 0.dp,
    content: LazyWeightedGridScope.() -> Unit,
) {
    require(targetItemWidth > 0.dp)
    require(spacing >= 0.dp)
    val scope = LazyWeightedGridScope().apply(content)
    val slots = rememberGridSlots(targetItemWidth, spacing)

    LazyGrid(
        modifier = modifier,
        state = state,
        slots = slots,
        contentPadding = contentPadding,
        reverseLayout = false,
        isVertical = true,
        flingBehavior = ScrollableDefaults.flingBehavior(),
        userScrollEnabled = true,
        overscrollEffect = rememberOverscrollEffect(),
        verticalArrangement = Arrangement.spacedBy(spacing),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        content = { scope.content(this) },
        lineSlots = { line, defaultSlots, gap ->
            weightedRowSlots(defaultSlots, line, gap, scope::itemRowWeight)
        },
    )
}

@Composable
private fun rememberGridSlots(
    targetItemWidth: Dp,
    spacing: Dp,
): LazyGridSlotsProvider =
    remember(targetItemWidth, spacing) {
        var cache: GridSlotCache? = null
        LazyGridSlotsProvider { density, constraints ->
            val width = constraints.maxWidth
            val cached = cache
            if (cached != null && cached.width == width && cached.density == density.density) {
                return@LazyGridSlotsProvider cached.slots
            }

            val gap = with(density) { spacing.roundToPx() }
            val sizes =
                with(density) {
                    with(GridCells.Adaptive(targetItemWidth)) {
                        calculateCrossAxisCellSizes(width, gap).toIntArray()
                    }
                }

            val slots = LazyGridSlots(sizes, slotPositions(sizes, gap))
            cache = GridSlotCache(width, density.density, slots)
            slots
        }
    }

private class GridSlotCache(
    val width: Int,
    val density: Float,
    val slots: LazyGridSlots,
)

internal fun weightedRowSlots(
    defaultSlots: LazyGridSlots,
    line: LineConfiguration,
    spacing: Int,
    itemRowWeight: (Int) -> Float,
): LazyGridSlots {
    // Spans cover multiple slots; applying one item weight per slot would size them incorrectly.
    if (line.spans.any { it.currentLineSpan != 1 }) return defaultSlots
    val weights =
        List(defaultSlots.sizes.size) { column ->
            if (column < line.spans.size) itemRowWeight(line.firstItemIndex + column) else 1f
        }
    val widths = weightedRowWidths(defaultSlots.sizes.sum(), weights)
    return LazyGridSlots(widths, slotPositions(widths, spacing))
}

private fun slotPositions(
    widths: IntArray,
    spacing: Int,
): IntArray {
    val positions = IntArray(widths.size)
    for (index in 1 until widths.size) {
        positions[index] = positions[index - 1] + widths[index - 1] + spacing
    }
    return positions
}

internal fun weightedRowWidths(
    availableWidth: Int,
    weights: List<Float>,
): IntArray {
    val normalizedWeights = weights.map(::normalizedRowWeight)
    val total = normalizedWeights.sum()
    var accumulated = 0.0
    var used = 0
    return IntArray(normalizedWeights.size) { index ->
        accumulated += normalizedWeights[index]
        val end = (availableWidth * accumulated / total).roundToInt()
        (end - used).also { used = end }
    }
}

private fun normalizedRowWeight(weight: Float): Double {
    return if (weight.isFinite() && weight > 0f) weight.toDouble() else 1.0
}
