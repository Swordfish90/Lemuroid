package com.codebutler.odyssey.app.feature.home

import android.arch.lifecycle.Observer
import android.arch.paging.LivePagedListProvider
import android.arch.paging.PagedList
import android.content.Context
import android.os.Bundle
import android.support.v17.leanback.app.VerticalGridSupportFragment
import android.support.v17.leanback.widget.OnItemViewClickedListener
import android.support.v17.leanback.widget.VerticalGridPresenter
import com.codebutler.odyssey.R
import com.codebutler.odyssey.app.feature.game.GameActivity
import com.codebutler.odyssey.lib.library.GameSystem
import com.codebutler.odyssey.lib.library.db.OdysseyDatabase
import com.codebutler.odyssey.lib.library.db.entity.Game
import com.codebutler.odyssey.lib.ui.PagedListObjectAdapter
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class GamesGridFragment : VerticalGridSupportFragment() {

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gridPresenter = VerticalGridPresenter()
        gridPresenter.numberOfColumns = NUM_COLUMNS
        setGridPresenter(gridPresenter)
    }

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val mode: Mode = Mode.values().find { it.value == arguments.getString(ARG_MODE) }!!
        val param: String? = arguments.getString(ARG_PARAM)

        title = when (mode) {
            GamesGridFragment.Mode.ALL -> getString(R.string.all_games)
            GamesGridFragment.Mode.SYSTEM -> getString(GameSystem.findById(param!!)!!.titleResId)
        }

        getQuery(mode, param)
                .create(0, PagedList.Config.Builder()
                    .setPageSize(50)
                    .setPrefetchDistance(50)
                    .build()
                )
                .observe(this, Observer { pagedList ->
                    val adapter = PagedListObjectAdapter(GamePresenter(), Game.DIFF_CALLBACK)
                    adapter.pagedList = pagedList
                    this.adapter = adapter
                })

        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, _ ->
            when (item) {
                is Game -> startActivity(GameActivity.newIntent(context, item))
            }
        }
    }

    private fun getQuery(mode: Mode, param: String?): LivePagedListProvider<Int, Game> = when (mode) {
        Mode.ALL -> odysseyDb.gameDao().selectAll()
        Mode.SYSTEM -> odysseyDb.gameDao().selectBySystem(param!!)
    }
}
