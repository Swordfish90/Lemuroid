package com.swordfish.lemuroid.common.rx

import com.swordfish.lemuroid.common.kotlin.NTuple4
import com.swordfish.lemuroid.common.kotlin.NTuple5
import io.reactivex.Observable

object RXUtils {
    fun <T1, T2, T3, T4> combineLatest(
        source1: Observable<T1>,
        source2: Observable<T2>,
        source3: Observable<T3>,
        source4: Observable<T4>
    ): Observable<NTuple4<T1, T2, T3, T4>> {
        return Observable.combineLatest(
            source1,
            source2,
            source3,
            source4
        ) { t1, t2, t3, t4 -> NTuple4(t1, t2, t3, t4) }
    }

    fun <T1, T2, T3, T4, T5> combineLatest(
        source1: Observable<T1>,
        source2: Observable<T2>,
        source3: Observable<T3>,
        source4: Observable<T4>,
        source5: Observable<T5>
    ): Observable<NTuple5<T1, T2, T3, T4, T5>> {
        return Observable.combineLatest(
            source1,
            source2,
            source3,
            source4,
            source5
        ) { t1, t2, t3, t4, t5 -> NTuple5(t1, t2, t3, t4, t5) }
    }
}
