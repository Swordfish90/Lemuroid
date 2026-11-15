package com.swordfish.lemuroid.app.mobile.feature.editgame

import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.map
import com.swordfish.lemuroid.common.paging.buildFlowPaging
import com.swordfish.lemuroid.lib.library.LemuroidLibrary
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.metadata.libretrodb.LibretroDBMetadataProvider
import com.swordfish.lemuroid.metadata.libretrodb.db.entity.LibretroRom
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.Serializable
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class EditGameViewModel(
    private val savedState: SavedStateHandle,
    private val game: Game,
    private val library: LemuroidLibrary,
    private val provider: LibretroDBMetadataProvider,
) : ViewModel() {
    data class UiState(
        val draft: Game,
        val pickedImage: String? = null,
        val choosingGame: Boolean = false,
        val confirmingDiscard: Boolean = false,
        val busy: Boolean = false,
        val shouldDismiss: Boolean = false,
    ) : Serializable

    private val mutableState = savedState.getMutableStateFlow("state", UiState(draft = game)).apply {
        value = value.copy(busy = false, shouldDismiss = false)
    }
    val state = mutableState.asStateFlow()
    val hasUnsavedChanges: Boolean
        get() {
            val current = state.value
            return current.draft.title != game.title ||
                current.draft.developer.orEmpty() != game.developer.orEmpty() ||
                current.draft.coverFrontUrl != game.coverFrontUrl ||
                current.pickedImage != null
        }

    val query = savedState.getStateFlow("query", "")

    val candidates = query
        .debounce(250.milliseconds)
        .flatMapLatest { query ->
            buildFlowPaging(30, viewModelScope) { provider.searchGames(game.systemId, query) }
        }
        .map { page -> page.map(::toGame) }

    private fun toGame(rom: LibretroRom): Game {
        val metadata = provider.convertToGameMetadata(rom)
        return game.copy(
            id = rom.id,
            title = metadata.name.orEmpty(),
            developer = metadata.developer,
            coverFrontUrl = metadata.thumbnail,
        )
    }

    private fun updateDraft(draft: Game, pickedImage: String? = null) {
        mutableState.update { it.copy(draft = draft, pickedImage = pickedImage) }
    }

    fun changeTitle(title: String) {
        updateDraft(state.value.draft.copy(title = title), state.value.pickedImage)
    }

    fun changeDeveloper(developer: String) {
        updateDraft(state.value.draft.copy(developer = developer), state.value.pickedImage)
    }

    fun pickImage(uri: String) {
        updateDraft(state.value.draft, uri)
    }

    fun changeQuery(query: String) {
        savedState["query"] = query
    }

    fun showDatabase(show: Boolean) {
        mutableState.update { it.copy(choosingGame = show) }
    }

    fun requestDismiss() {
        if (state.value.busy || state.value.shouldDismiss) return
        if (hasUnsavedChanges) {
            mutableState.update { it.copy(confirmingDiscard = true) }
        } else {
            mutableState.update { it.copy(shouldDismiss = true) }
        }
    }

    fun keepEditing() {
        mutableState.update { it.copy(confirmingDiscard = false) }
    }

    fun discardChanges() {
        if (state.value.busy || state.value.shouldDismiss) return
        keepEditing()
        mutableState.update { it.copy(shouldDismiss = true) }
    }

    fun chooseGame(game: Game) {
        updateDraft(
            state.value.draft.copy(
                title = game.title,
                developer = game.developer,
                coverFrontUrl = game.coverFrontUrl,
            ),
        )
        showDatabase(false)
    }

    fun restoreDefaults() {
        if (state.value.busy) return
        mutableState.update { it.copy(busy = true) }
        viewModelScope.launch {
            try {
                chooseGame(library.resolveDefaultGame(game))
            } catch (exception: Exception) {
                Timber.e(exception, "Could not restore game defaults")
            } finally {
                mutableState.update { it.copy(busy = false) }
            }
        }
    }

    fun save() {
        val current = state.value
        if (current.busy || current.shouldDismiss || current.draft.title.isBlank()) return
        mutableState.update { it.copy(busy = true) }
        viewModelScope.launch {
            try {
                library.updateGamePresentation(game, current.draft, current.pickedImage?.toUri())
                mutableState.update { it.copy(shouldDismiss = true) }
            } catch (exception: Exception) {
                Timber.e(exception, "Could not save game")
            } finally {
                mutableState.update { it.copy(busy = false) }
            }
        }
    }

    class Factory(
        private val savedState: SavedStateHandle,
        private val game: Game,
        private val library: LemuroidLibrary,
        private val provider: LibretroDBMetadataProvider,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return EditGameViewModel(savedState, game, library, provider) as T
        }
    }
}
