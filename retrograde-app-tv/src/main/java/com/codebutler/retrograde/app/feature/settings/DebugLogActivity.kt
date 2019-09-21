package com.codebutler.retrograde.app.feature.settings

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codebutler.retrograde.R
import com.codebutler.retrograde.common.kotlin.bindView
import com.codebutler.retrograde.common.kotlin.inflate
import com.codebutler.retrograde.lib.android.RetrogradeActivity
import com.codebutler.retrograde.lib.logging.RxTimberTree
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import dagger.android.AndroidInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class DebugLogActivity : RetrogradeActivity() {

    @Inject lateinit var rxTimberTree: RxTimberTree

    private val adapter = LogAdapter()

    private val recycler by bindView<RecyclerView>(R.id.recycler)

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_log)

        val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        layoutManager.stackFromEnd = true

        recycler.layoutManager = layoutManager
        recycler.adapter = adapter

        rxTimberTree.observable
                .observeOn(AndroidSchedulers.mainThread())
                .autoDisposable(scope())
                .subscribe { logEntry ->
                    adapter.add(logEntry)
                    recycler.scrollToPosition(adapter.itemCount - 1)
                }
    }

    class LogAdapter : RecyclerView.Adapter<LogEntryViewHolder>() {

        private val entries = mutableListOf<RxTimberTree.LogEntry>()

        fun add(logEntry: RxTimberTree.LogEntry) {
            entries.add(logEntry)
            notifyDataSetChanged()
        }

        override fun getItemCount() = entries.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogEntryViewHolder {
            return LogEntryViewHolder(parent)
        }

        override fun onBindViewHolder(holder: LogEntryViewHolder, position: Int) {
            holder.bind(entries[position])
        }
    }

    class LogEntryViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(parent.inflate(R.layout.listitem_log_entry)) {

        private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.US)

        private val timestampTextView by bindView<TextView>(R.id.timestamp)
        private val tagTextView by bindView<TextView>(R.id.tag)
        private val priorityTextView by bindView<TextView>(R.id.priority)
        private val messageTextView by bindView<TextView>(R.id.message)
        private val exceptionTextView by bindView<TextView>(R.id.exception)

        fun bind(logEntry: RxTimberTree.LogEntry) {
            timestampTextView.text = dateFormat.format(logEntry.timestamp)
            tagTextView.text = logEntry.tag
            priorityTextView.text = getPriorityText(logEntry)
            messageTextView.text = logEntry.message

            if (logEntry.error != null) {
                exceptionTextView.text = logEntry.error.toString()
                exceptionTextView.visibility = View.VISIBLE
            } else {
                exceptionTextView.text = null
                exceptionTextView.visibility = View.GONE
            }

            val color = getPriorityColor(logEntry)
            tagTextView.setTextColor(color)
            priorityTextView.setTextColor(color)
            messageTextView.setTextColor(color)
            exceptionTextView.setTextColor(color)
        }

        companion object {
            private val orange = Color.parseColor("#FFC107")
            private val red = Color.parseColor("#F44336")

            private fun getPriorityColor(logEntry: RxTimberTree.LogEntry) =
                    when (logEntry.priority) {
                        Log.VERBOSE -> Color.GRAY
                        Log.DEBUG -> Color.LTGRAY
                        Log.INFO -> Color.WHITE
                        Log.WARN -> orange
                        Log.ERROR -> red
                        Log.ASSERT -> red
                        else -> Color.WHITE
                    }

            private fun getPriorityText(logEntry: RxTimberTree.LogEntry) =
                    when (logEntry.priority) {
                        Log.VERBOSE -> "V"
                        Log.DEBUG -> "D"
                        Log.INFO -> "I"
                        Log.WARN -> "W"
                        Log.ERROR -> "E"
                        Log.ASSERT -> "A"
                        else -> "?"
                    }
        }
    }
}
