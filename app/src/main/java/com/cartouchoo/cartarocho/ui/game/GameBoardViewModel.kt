package com.cartouchoo.cartarocho.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cartouchoo.cartarocho.data.model.Game
import com.cartouchoo.cartarocho.data.model.Player
import com.cartouchoo.cartarocho.data.model.RoundWithScores
import com.cartouchoo.cartarocho.data.repository.GameRepository
import com.cartouchoo.cartarocho.data.repository.RoundRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GameBoardUiState(
    val players: List<Player> = emptyList(),
    val rounds: List<RoundWithScores> = emptyList(),
    val totals: Map<Long, Double> = emptyMap(),
    val ranking: Map<Long, Int> = emptyMap(),
)

class GameBoardViewModel(
    private val gameId: Long,
    private val gameRepository: GameRepository,
    private val roundRepository: RoundRepository,
) : ViewModel() {

    private val _game = MutableStateFlow<Game?>(null)
    val game: StateFlow<Game?> = _game.asStateFlow()

    private val _renameDialogOpen = MutableStateFlow(false)
    val renameDialogOpen: StateFlow<Boolean> = _renameDialogOpen.asStateFlow()

    private val _renameText = MutableStateFlow("")
    val renameText: StateFlow<String> = _renameText.asStateFlow()

    val uiState: StateFlow<GameBoardUiState> = combine(
        gameRepository.observePlayers(gameId),
        roundRepository.observeRounds(gameId),
    ) { players, rounds ->
        val totals = players.associate { player ->
            player.id to rounds.sumOf { it.scores[player.id] ?: 0.0 }
        }
        val ranking = totals.entries
            .sortedByDescending { it.value }
            .mapIndexed { index, entry -> entry.key to index + 1 }
            .toMap()
        GameBoardUiState(players = players, rounds = rounds, totals = totals, ranking = ranking)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GameBoardUiState())

    private var pendingDelete: RoundWithScores? = null

    init {
        viewModelScope.launch {
            _game.value = gameRepository.getById(gameId)
        }
    }

    fun deleteRound(round: RoundWithScores) {
        pendingDelete = round
        viewModelScope.launch {
            roundRepository.deleteRound(round.id)
        }
    }

    fun undoDelete() {
        val round = pendingDelete ?: return
        pendingDelete = null
        viewModelScope.launch {
            roundRepository.restoreRound(round)
        }
    }

    fun openRenameDialog() {
        _renameText.value = _game.value?.name.orEmpty()
        _renameDialogOpen.value = true
    }

    fun updateRenameText(name: String) {
        _renameText.value = name
    }

    fun dismissRenameDialog() {
        _renameDialogOpen.value = false
    }

    fun confirmRenameDialog() {
        val name = _renameText.value
        _renameDialogOpen.value = false
        viewModelScope.launch {
            gameRepository.rename(gameId, name)
            _game.value = gameRepository.getById(gameId)
        }
    }

    companion object {
        fun factory(
            gameId: Long,
            gameRepository: GameRepository,
            roundRepository: RoundRepository,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { GameBoardViewModel(gameId, gameRepository, roundRepository) }
        }
    }
}
