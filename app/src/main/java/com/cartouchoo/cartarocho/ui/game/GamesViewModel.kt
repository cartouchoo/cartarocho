package com.cartouchoo.cartarocho.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cartouchoo.cartarocho.data.model.Game
import com.cartouchoo.cartarocho.data.model.Player
import com.cartouchoo.cartarocho.data.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GameListItem(
    val game: Game,
    val players: List<Player>,
)

/** État de la boîte de dialogue de renommage d'une partie. */
data class RenameGameDialogState(
    val gameId: Long,
    val name: String,
)

class GamesViewModel(
    private val gameRepository: GameRepository,
) : ViewModel() {

    val games: StateFlow<List<GameListItem>> = gameRepository.observeAll()
        .map { games -> games.map { game -> GameListItem(game, gameRepository.getPlayers(game.id)) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _renameDialogState = MutableStateFlow<RenameGameDialogState?>(null)
    val renameDialogState: StateFlow<RenameGameDialogState?> = _renameDialogState.asStateFlow()

    fun deleteGame(gameId: Long) {
        viewModelScope.launch {
            gameRepository.deleteGame(gameId)
        }
    }

    fun openRenameDialog(game: Game) {
        _renameDialogState.value = RenameGameDialogState(gameId = game.id, name = game.name.orEmpty())
    }

    fun updateRenameDialogName(name: String) {
        _renameDialogState.value = _renameDialogState.value?.copy(name = name)
    }

    fun dismissRenameDialog() {
        _renameDialogState.value = null
    }

    fun confirmRenameDialog() {
        val state = _renameDialogState.value ?: return
        viewModelScope.launch {
            gameRepository.rename(state.gameId, state.name)
            _renameDialogState.value = null
        }
    }

    companion object {
        fun factory(gameRepository: GameRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { GamesViewModel(gameRepository) }
        }
    }
}
