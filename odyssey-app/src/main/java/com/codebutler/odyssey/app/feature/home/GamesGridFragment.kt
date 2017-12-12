package com.codebutler.odyssey.app.feature.home

import android.arch.lifecycle.Observer
import android.arch.paging.DataSource
import android.arch.paging.LivePagedListBuilder
import android.content.Context
import android.os.Bundle
import android.support.v17.leanback.app.VerticalGridSupportFragment
import android.support.v17.leanback.widget.OnItemViewClickedListener
import android.support.v17.leanback.widget.Presenter
import android.support.v17.leanback.widget.Row
import android.support.v17.leanback.widget.RowPresenter
import android.support.v17.leanback.widget.VerticalGridPresenter
import com.codebutler.odyssey.R
import com.codebutler.odyssey.app.feature.main.MainActivity
import com.codebutler.odyssey.app.shared.GameInteractionHandler
import com.codebutler.odyssey.lib.injection.PerFragment
import com.codebutler.odyssey.lib.library.GameSystem
import com.codebutler.odyssey.lib.library.db.OdysseyDatabase
import com.codebutler.odyssey.lib.library.db.entity.Game
import com.codebutler.odyssey.lib.ui.PagedListObjectAdapter
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

    @Inject lateinit var odysseyDb: OdysseyDatabase
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
            rowViewHolder: RowPresenter.ViewHolder,
            row: Row) {
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
                    val adapter = PagedListObjectAdapter(GamePresenter(gameInteractionHandler), Game.DIFF_CALLBACK)
                    adapter.pagedList = pagedList
                    this.adapter = adapter
                })
    }

    private fun getQuery(mode: Mode, param: String?): DataSource.Factory<Int, Game> = when (mode) {
        Mode.ALL -> odysseyDb.gameDao().selectAll()
        Mode.SYSTEM -> odysseyDb.gameDao().selectBySystem(param!!)
    }

    @dagger.Module
    class Module {

        @Provides
        @PerFragment
        fun gameInteractionHandler(activity: MainActivity, odysseyDb: OdysseyDatabase) =
                GameInteractionHandler(activity, odysseyDb)
    }
}
