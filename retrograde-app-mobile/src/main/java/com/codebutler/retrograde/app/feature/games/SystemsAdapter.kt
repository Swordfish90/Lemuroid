package com.codebutler.retrograde.app.feature.games

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codebutler.retrograde.R
import com.codebutler.retrograde.lib.library.GameSystem

class SystemViewHolder(parent: View) : RecyclerView.ViewHolder(parent) {
    private var coverView: ImageView? = null
    private var textView: TextView? = null

    init {
        coverView = itemView.findViewById(R.id.image)
        textView = itemView.findViewById(R.id.text)
    }

    fun bind(system: GameSystem, onSystemClick: (GameSystem) -> Unit) {
        textView?.setText(system.titleResId)
        coverView?.setImageResource(system.imageResId)
        itemView.setOnClickListener { onSystemClick(system) }
    }
}

class SystemsAdapter(
    private val onSystemClick: (GameSystem) -> Unit
) : ListAdapter<GameSystem, SystemViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SystemViewHolder {
        return SystemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_system, parent, false))
    }

    override fun onBindViewHolder(holder: SystemViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it, onSystemClick) }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<GameSystem>() {
            override fun areItemsTheSame(oldSystem: GameSystem, newSystem: GameSystem) = oldSystem.id == newSystem.id

            override fun areContentsTheSame(oldSystem: GameSystem, newSystem: GameSystem) = oldSystem == newSystem
        }
    }
}
