package com.swordfish.lemuroid.app.feature.systems

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.lib.library.GameSystem

class SystemViewHolder(parent: View) : RecyclerView.ViewHolder(parent) {
    private var coverView: ImageView? = null
    private var textView: TextView? = null
    private var subtextView: TextView? = null

    init {
        coverView = itemView.findViewById(R.id.image)
        textView = itemView.findViewById(R.id.text)
        subtextView = itemView.findViewById(R.id.subtext)
    }

    fun bind(systemInfo: SystemInfo, onSystemClick: (GameSystem) -> Unit) {
        textView?.text = itemView.context.resources.getString(systemInfo.system.titleResId)
        subtextView?.text = itemView.context.getString(R.string.system_grid_details, systemInfo.count.toString())
        coverView?.setImageResource(systemInfo.system.imageResId)
        itemView.setOnClickListener { onSystemClick(systemInfo.system) }
    }
}

class SystemsAdapter(
    private val onSystemClick: (GameSystem) -> Unit
) : ListAdapter<SystemInfo, SystemViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SystemViewHolder {
        return SystemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_system, parent, false))
    }

    override fun onBindViewHolder(holder: SystemViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it, onSystemClick) }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<SystemInfo>() {

            override fun areItemsTheSame(oldInfo: SystemInfo, newInfo: SystemInfo) =
                    oldInfo.system.id == newInfo.system.id

            override fun areContentsTheSame(oldInfo: SystemInfo, newInfo: SystemInfo) =
                    oldInfo == newInfo
        }
    }
}
