package com.codebutler.retrograde.app

import com.jakewharton.rxrelay2.ReplayRelay
import io.reactivex.Observable
import timber.log.Timber

class RxTimberTree : Timber.DebugTree() {

    private val relay = ReplayRelay.createWithSize<LogEntry>(500)

    val observable: Observable<LogEntry> = relay.hide()

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        relay.accept(LogEntry(System.currentTimeMillis(), priority, tag, message, t))
    }

    data class LogEntry(
        val timestamp: Long,
        val priority: Int,
        val tag: String?,
        val message: String?,
        val error: Throwable?
    )
}
