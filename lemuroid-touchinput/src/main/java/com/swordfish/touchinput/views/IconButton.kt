package com.swordfish.touchinput.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton
import com.jakewharton.rxrelay2.PublishRelay
import com.swordfish.touchinput.events.ViewEvent
import com.swordfish.touchinput.interfaces.ClickEventsSource
import io.reactivex.Observable

class IconButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageButton(context, attrs, defStyleAttr), ClickEventsSource {

    private val events: PublishRelay<ViewEvent.Click> = PublishRelay.create()

    init {
        setOnClickListener { handleClickEvent() }
    }

    private fun handleClickEvent() {
        events.accept(ViewEvent.Click)
    }

    override fun getEvents(): Observable<ViewEvent.Click> = events
}
