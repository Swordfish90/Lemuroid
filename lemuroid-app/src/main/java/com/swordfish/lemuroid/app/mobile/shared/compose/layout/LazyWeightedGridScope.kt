package com.swordfish.lemuroid.app.mobile.shared.compose.layout

import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.layout.MutableIntervalList
import androidx.compose.runtime.Composable

class LazyWeightedGridScope internal constructor() {
    private class Interval(
        val weight: (Int) -> Float,
        val content: LazyGridScope.() -> Unit,
    )

    private val intervals = MutableIntervalList<Interval>()

    fun item(
        key: Any? = null,
        span: (LazyGridItemSpanScope.() -> GridItemSpan)? = null,
        contentType: Any? = null,
        itemRowWeight: Float = 1f,
        content: @Composable LazyGridItemScope.() -> Unit,
    ) {
        intervals.addInterval(1, Interval({ itemRowWeight }) { this.item(key, span, contentType, content) })
    }

    fun items(
        count: Int,
        key: ((Int) -> Any)? = null,
        span: (LazyGridItemSpanScope.(Int) -> GridItemSpan)? = null,
        contentType: (Int) -> Any? = { null },
        itemRowWeight: (Int) -> Float = { 1f },
        itemContent: @Composable LazyGridItemScope.(Int) -> Unit,
    ) {
        intervals.addInterval(count, Interval(itemRowWeight) { this.items(count, key, span, contentType, itemContent) })
    }

    internal fun itemRowWeight(index: Int): Float {
        val interval = intervals[index]
        return interval.value.weight(index - interval.startIndex)
    }

    internal fun content(scope: LazyGridScope) {
        if (intervals.size > 0) {
            intervals.forEach { it.value.content(scope) }
        }
    }
}
