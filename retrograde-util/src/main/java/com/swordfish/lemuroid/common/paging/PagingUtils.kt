package com.swordfish.lemuroid.common.paging

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow

fun <T : Any> buildFlowPaging(
    pageSize: Int,
    source: () -> PagingSource<Int, T>
): Flow<PagingData<T>> {
    return Pager(PagingConfig(pageSize), pagingSourceFactory = source).flow
}
