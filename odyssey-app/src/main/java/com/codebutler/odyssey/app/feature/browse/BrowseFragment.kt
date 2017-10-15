/*
 * BrowseFragment.kt
 *
 * Copyright (C) 2017 Odyssey Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.codebutler.odyssey.app.feature.browse

import android.os.Bundle
import android.support.v17.leanback.app.BrowseFragment
import android.support.v17.leanback.app.BrowseSupportFragment
import android.support.v17.leanback.widget.ArrayObjectAdapter
import android.support.v17.leanback.widget.HeaderItem
import android.support.v17.leanback.widget.ListRow
import android.support.v17.leanback.widget.ListRowPresenter
import android.support.v17.leanback.widget.OnItemViewClickedListener
import com.codebutler.odyssey.R
import com.codebutler.odyssey.app.feature.game.GameActivity
import com.codebutler.odyssey.app.feature.main.MainActivity
import com.codebutler.odyssey.common.http.OdysseyHttp
import com.codebutler.odyssey.lib.core.CoreManager
import com.codebutler.odyssey.lib.library.GameLibrary
import com.codebutler.odyssey.lib.library.GameSystem
import com.codebutler.odyssey.lib.library.db.entity.Game
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.kotlin.autoDisposeWith
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class BrowseFragment : BrowseSupportFragment() {

    lateinit var component: BrowseComponent

    @Inject lateinit var coreManager: CoreManager
    @Inject lateinit var gameLibrary: GameLibrary

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        component = DaggerBrowseComponent.builder()
                .mainComponent((activity as MainActivity).component)
                .build()
        component.inject(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        headersState = BrowseFragment.HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true
        title = getString(R.string.app_name)

        setOnSearchClickedListener {
            // TODO
        }

        prepareEntranceTransition()

        gameLibrary.indexGames()

        gameLibrary.games
                .observeOn(AndroidSchedulers.mainThread())
                .autoDisposeWith(AndroidLifecycleScopeProvider.from(this))
                .subscribe { games ->
                    val gamesBySystem = games
                            .groupBy { it.systemId }
                            .mapKeys { (systemId, _) -> getString(GameSystem.findById(systemId)!!.titleResId) }
                            .toSortedMap()
                    val categoryRowAdapter = ArrayObjectAdapter(ListRowPresenter())
                    for ((systemTitle, gamesForSystem) in gamesBySystem) {
                        val gamesAdapter = ArrayObjectAdapter(GamePresenter())
                        gamesAdapter.addAll(0, gamesForSystem)
                        categoryRowAdapter.add(ListRow(HeaderItem(systemTitle), gamesAdapter))
                    }
                    adapter = categoryRowAdapter
                    startEntranceTransition()
        }

        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, _ ->
            when (item) {
                is Game -> {
                    progressBarManager.show()
                    val gameSystem = GameSystem.findById(item.systemId)!!
                    coreManager.downloadCore(gameSystem.coreFileName)
                            .observeOn(AndroidSchedulers.mainThread())
                            .autoDisposeWith(AndroidLifecycleScopeProvider.from(this))
                            .subscribe { response ->
                                progressBarManager.hide()
                                when (response) {
                                    is OdysseyHttp.Response.Success -> {
                                        val coreFile = response.body
                                        startActivity(GameActivity.newIntent(
                                                context = activity,
                                                coreFilePath = coreFile.absolutePath,
                                                gameFilePath = item.fileUri.path))
                                    }
                                    is OdysseyHttp.Response.Failure -> {
                                        val errorFragment = BrowseErrorFragment.create(response.error.toString())
                                        fragmentManager.beginTransaction()
                                                .replace(R.id.content, errorFragment)
                                                .addToBackStack(null)
                                                .commit()
                                    }
                                }
                            }
                }
            }
        }
    }
}
