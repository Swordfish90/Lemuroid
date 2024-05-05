package com.swordfish.lemuroid.common.paging

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.cachedIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

fun <T : Any> buildFlowPaging(
    pageSize: Int,
    coroutineScope: CoroutineScope,
    source: () -> PagingSource<Int, T>,
): Flow<PagingData<T>> {
    return Pager(PagingConfig(pageSize), pagingSourceFactory = source)
        .flow
        .cachedIn(coroutineScope)
}
