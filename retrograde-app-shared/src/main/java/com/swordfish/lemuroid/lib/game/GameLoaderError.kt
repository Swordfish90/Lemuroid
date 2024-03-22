package com.swordfish.lemuroid.lib.game

class GameLoaderException(val error: GameLoaderError) : RuntimeException("Game Loader error: $error")

sealed class GameLoaderError {
    object GLIncompatible : GameLoaderError()

    object Generic : GameLoaderError()

    object LoadCore : GameLoaderError()

    object LoadGame : GameLoaderError()

    object Saves : GameLoaderError()

    object UnsupportedArchitecture : GameLoaderError()

    data class MissingBiosFiles(val missingFiles: List<String>) : GameLoaderError()
}
