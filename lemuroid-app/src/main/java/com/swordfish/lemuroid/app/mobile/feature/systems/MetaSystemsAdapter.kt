package com.swordfish.lemuroid.app.mobile.feature.systems

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.systems.MetaSystemInfo
import com.swordfish.lemuroid.lib.library.MetaSystemID

class MetaSystemViewHolder(parent: View) : RecyclerView.ViewHolder(parent) {
    private var coverView: ImageView? = null
    private var textView: TextView? = null
    private var subtextView: TextView? = null

    init {
        coverView = itemView.findViewById(R.id.image)
        textView = itemView.findViewById(R.id.text)
        subtextView = itemView.findViewById(R.id.subtext)
    }

    fun bind(metaSystemInfo: MetaSystemInfo, onSystemClick: (MetaSystemID) -> Unit) {
        textView?.text = itemView.context.resources.getString(metaSystemInfo.metaSystem.titleResId)
        subtextView?.text = itemView.context.getString(
            R.string.system_grid_details,
            metaSystemInfo.count.toString()
        )
        coverView?.setImageResource(metaSystemInfo.metaSystem.imageResId)
        itemView.setOnClickListener { onSystemClick(metaSystemInfo.metaSystem) }
    }
}

class MetaSystemsAdapter(
    private val onSystemClick: (MetaSystemID) -> Unit
) : ListAdapter<MetaSystemInfo, MetaSystemViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MetaSystemViewHolder {
        return MetaSystemViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.layout_system, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MetaSystemViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it, onSystemClick) }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<MetaSystemInfo>() {

            override fun areItemsTheSame(oldInfo: MetaSystemInfo, newInfo: MetaSystemInfo) =
                oldInfo.metaSystem == newInfo.metaSystem

            override fun areContentsTheSame(oldInfo: MetaSystemInfo, newInfo: MetaSystemInfo) =
                oldInfo == newInfo
        }
    }
}
