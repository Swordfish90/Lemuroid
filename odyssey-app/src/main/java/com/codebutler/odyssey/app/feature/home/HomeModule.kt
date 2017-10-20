package com.codebutler.odyssey.app.feature.home

import com.codebutler.odyssey.lib.core.CoreManager
import com.codebutler.odyssey.lib.library.GameLibrary
import com.codebutler.odyssey.lib.library.db.OdysseyDatabase
import dagger.Module
import dagger.Provides

@Module
class HomeModule {

    @Provides
    fun gameLauncher(gameLibrary: GameLibrary, coreManager: CoreManager, odysseyDb: OdysseyDatabase)
            = GameLauncher(gameLibrary, coreManager, odysseyDb)
}