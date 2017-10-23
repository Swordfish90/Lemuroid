package com.codebutler.odyssey.app.feature.home

import com.codebutler.odyssey.lib.core.CoreManager
import com.codebutler.odyssey.lib.library.db.OdysseyDatabase
import com.codebutler.odyssey.lib.library.provider.GameLibraryProviderRegistry
import dagger.Module
import dagger.Provides

@Module
class HomeModule {

    @Provides
    fun gameLauncher(gameLibraryProviderRegistry: GameLibraryProviderRegistry, coreManager: CoreManager, odysseyDb: OdysseyDatabase)
            = GameLauncher(gameLibraryProviderRegistry, coreManager, odysseyDb)
}
