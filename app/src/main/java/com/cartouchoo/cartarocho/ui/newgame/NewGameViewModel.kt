package com.cartouchoo.cartarocho.ui.newgame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cartouchoo.cartarocho.data.model.Player
import com.cartouchoo.cartarocho.data.repository.GameRepository
import com.cartouchoo.cartarocho.data.repository.PlayerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NewGameUiState(
    val players: List<Player> = emptyList(),
    val playerCount: Int = 4,
    val selectedPlayerIds: List<Long> = emptyList(),
    val isCreating: Boolean = false,
) {
    val canValidate: Boolean get() = selectedPlayerIds.size == playerCount && !isCreating
}

class NewGameViewModel(
    private val playerRepository: PlayerRepository,
    private val gameRepository: GameRepository,
) : ViewModel() {

    private val playerCount = MutableStateFlow(4)
    private val selectedPlayerIds = MutableStateFlow<List<Long>>(emptyList())
    private val isCreating = MutableStateFlow(false)

    val uiState: StateFlow<NewGameUiState> = combine(
        playerRepository.observeAll(),
        playerCount,
        selectedPlayerIds,
        isCreating,
    ) { players, count, selected, creating ->
        NewGameUiState(
            players = players,
            playerCount = count,
            selectedPlayerIds = selected,
            isCreating = creating,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NewGameUiState())

    fun setPlayerCount(count: Int) {
        playerCount.value = count
        selectedPlayerIds.update { it.take(count) }
    }

    fun togglePlayer(playerId: Long) {
        selectedPlayerIds.update { current ->
            when {
                playerId in current -> current - playerId
                current.size < playerCount.value -> current + playerId
                else -> current
            }
        }
    }

    fun onValidate(onGameCreated: (gameId: Long) -> Unit) {
        val state = uiState.value
        if (!state.canValidate) return
        isCreating.value = true
        viewModelScope.launch {
            val gameId = gameRepository.createGame(state.selectedPlayerIds)
            isCreating.value = false
            onGameCreated(gameId)
        }
    }

    companion object {
        fun factory(
            playerRepository: PlayerRepository,
            gameRepository: GameRepository,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { NewGameViewModel(playerRepository, gameRepository) }
        }
    }
}
