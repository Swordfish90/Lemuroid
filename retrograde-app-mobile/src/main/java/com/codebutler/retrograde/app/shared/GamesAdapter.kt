package com.codebutler.retrograde.app.shared

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.codebutler.retrograde.R
import com.codebutler.retrograde.lib.library.db.entity.Game
import com.squareup.picasso.Picasso

class GameViewHolder(parent: View) : RecyclerView.ViewHolder(parent) {
    private var titleView: TextView? = null
    private var coverView: ImageView? = null

    init {
        titleView = itemView.findViewById(R.id.text)
        coverView = itemView.findViewById(R.id.image)
    }

    fun bind(game: Game) {
        titleView?.text = game.title
        Picasso.get().isLoggingEnabled = true
        game.coverFrontUrl?.let {
            Picasso.get()
                    .load(it)
                    .fit()
                    .error(R.color.design_default_color_error)
                    .into(coverView)
        }
    }
}

class GamesAdapter : PagedListAdapter<Game, GameViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        return GameViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_griditem_game, parent, false))
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Game>() {
            override fun areItemsTheSame(oldConcert: Game, newConcert: Game) = oldConcert.id == newConcert.id

            override fun areContentsTheSame(oldConcert: Game, newConcert: Game) = oldConcert == newConcert
        }
    }
}
