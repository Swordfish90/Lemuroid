package com.codebutler.retrograde.app.feature.home

import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import android.content.Context
import android.os.Bundle
import androidx.leanback.app.VerticalGridSupportFragment
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.Row
import androidx.leanback.widget.RowPresenter
import androidx.leanback.widget.VerticalGridPresenter
import com.codebutler.retrograde.R
import com.codebutler.retrograde.app.feature.main.MainActivity
import com.codebutler.retrograde.app.shared.GameInteractionHandler
import com.codebutler.retrograde.app.shared.GamePresenter
import com.codebutler.retrograde.app.shared.ui.PagedListObjectAdapter
import com.codebutler.retrograde.lib.injection.PerFragment
import com.codebutler.retrograde.lib.library.GameSystem
import com.codebutler.retrograde.lib.library.db.RetrogradeDatabase
import com.codebutler.retrograde.lib.library.db.entity.Game
import dagger.Provides
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class GamesGridFragment : VerticalGridSupportFragment(), OnItemViewClickedListener {

    enum class Mode(val value: String) {
        ALL("all"),
        SYSTEM("system")
    }

    companion object {
        private const val ARG_MODE = "mode"
        private const val ARG_PARAM = "param"

        fun create(mode: Mode, param: String? = null): GamesGridFragment {
            val fragment = GamesGridFragment()
            fragment.arguments = Bundle().apply {
                putString(ARG_MODE, mode.value)
                putString(ARG_PARAM, param)
            }
            return fragment
        }

        private val NUM_COLUMNS = 5
    }

    @Inject lateinit var retrogradeDb: RetrogradeDatabase
    @Inject lateinit var gameInteractionHandler: GameInteractionHandler

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gridPresenter = VerticalGridPresenter()
        gridPresenter.numberOfColumns = NUM_COLUMNS
        setGridPresenter(gridPresenter)

        gameInteractionHandler.onRefreshListener = {
            loadContents()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        onItemViewClickedListener = this
        loadContents()
    }

    override fun onItemClicked(
        itemViewHolder: Presenter.ViewHolder,
        item: Any,
        rowViewHolder: RowPresenter.ViewHolder?,
        row: Row?
    ) {
        when (item) {
            is Game -> gameInteractionHandler.onItemClick(item)
        }
    }

    private fun loadContents() {
        val mode: Mode = Mode.values().find { it.value == arguments!!.getString(ARG_MODE) }!!
        val param: String? = arguments!!.getString(ARG_PARAM)

        title = when (mode) {
            Mode.ALL -> getString(R.string.all_games)
            Mode.SYSTEM -> getString(GameSystem.findById(param!!)!!.titleResId)
        }

        LivePagedListBuilder(getQuery(mode, param), 50)
                .build()
                .observe(this, Observer { pagedList ->
                    val adapter = PagedListObjectAdapter(GamePresenter(
                            requireActivity(),
                            gameInteractionHandler
                    ), Game.DIFF_CALLBACK)
                    adapter.pagedList = pagedList
                    this.adapter = adapter
                })
    }

    private fun getQuery(mode: Mode, param: String?): DataSource.Factory<Int, Game> = when (mode) {
        Mode.ALL -> retrogradeDb.gameDao().selectAll()
        Mode.SYSTEM -> retrogradeDb.gameDao().selectBySystem(param!!)
    }

    @dagger.Module
    class Module {

        @Provides
        @PerFragment
        fun gameInteractionHandler(activity: MainActivity, retrogradeDb: RetrogradeDatabase) =
                GameInteractionHandler(activity, retrogradeDb)
    }
}
