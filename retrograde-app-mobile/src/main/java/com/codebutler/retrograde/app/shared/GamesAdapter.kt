package com.codebutler.retrograde.app.shared

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.ToggleButton
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.codebutler.retrograde.R
import com.codebutler.retrograde.lib.library.GameSystem
import com.codebutler.retrograde.lib.library.db.entity.Game
import com.squareup.picasso.Picasso

class GameViewHolder(parent: View) : RecyclerView.ViewHolder(parent) {
    private var titleView: TextView? = null
    private var subtitleView: TextView? = null
    private var coverView: ImageView? = null
    private var favoriteToggle: ToggleButton? = null

    init {
        titleView = itemView.findViewById(R.id.text)
        subtitleView = itemView.findViewById(R.id.subtext)
        coverView = itemView.findViewById(R.id.image)
        favoriteToggle = itemView.findViewById(R.id.favorite_toggle)
    }

    fun bind(game: Game, gameInteractor: GameInteractor) {
        val systemName = getSystemNameForGame(game)

        titleView?.text = game.title
        subtitleView?.text = "$systemName - ${game.developer}"
        favoriteToggle?.isChecked = game.isFavorite

        Picasso.get()
                .load(game.coverFrontUrl)
                .placeholder(R.drawable.ic_image_paceholder)
                .error(R.drawable.ic_image_paceholder)
                .into(coverView)

        itemView.setOnClickListener { gameInteractor.onGameClick(game) }
        favoriteToggle?.setOnCheckedChangeListener { _, isChecked -> gameInteractor.onFavoriteToggle(game, isChecked) }
    }

    private fun getSystemNameForGame(game: Game): String {
        return GameSystem.findById(game.systemId)?.shortTitleResId?.let {
            itemView.context.getString(it)
        } ?: ""
    }

    fun unbind() {
        coverView?.apply {
            Picasso.get().cancelRequest(this)
            this.setImageDrawable(null)
        }
        itemView.setOnClickListener(null)
        favoriteToggle?.setOnCheckedChangeListener(null)
    }
}

class GamesAdapter(
    private val baseLayout: Int,
    private val gameInteractor: GameInteractor
) : PagedListAdapter<Game, GameViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        return GameViewHolder(LayoutInflater.from(parent.context).inflate(baseLayout, parent, false))
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it, gameInteractor) }
    }

    override fun onViewRecycled(holder: GameViewHolder) {
        holder.unbind()
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Game>() {
            override fun areItemsTheSame(oldConcert: Game, newConcert: Game) = oldConcert.id == newConcert.id

            override fun areContentsTheSame(oldConcert: Game, newConcert: Game) = oldConcert == newConcert
        }
    }
}
