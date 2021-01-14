package com.swordfish.lemuroid.common.paging

import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.cachedIn
import androidx.paging.liveData
import kotlinx.coroutines.CoroutineScope

fun <T : Any> buildLiveDataPaging(
    pageSize: Int,
    viewModelScope: CoroutineScope,
    source: () -> PagingSource<Int, T>
): LiveData<PagingData<T>> {
    return Pager(PagingConfig(pageSize), pagingSourceFactory = source).liveData.cachedIn(viewModelScope)
}
