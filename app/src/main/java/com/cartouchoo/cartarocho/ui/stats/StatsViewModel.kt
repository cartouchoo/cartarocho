package com.cartouchoo.cartarocho.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cartouchoo.cartarocho.data.repository.GameRepository
import com.cartouchoo.cartarocho.data.repository.PlayerRepository
import com.cartouchoo.cartarocho.data.repository.RoundRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class StatsViewModel(
    playerRepository: PlayerRepository,
    gameRepository: GameRepository,
    roundRepository: RoundRepository,
) : ViewModel() {

    val stats: StateFlow<List<PlayerStats>> = combine(
        playerRepository.observeAll(),
        gameRepository.observeAllGamePlayerLinks(),
        roundRepository.observeAllRounds(),
    ) { players, links, rounds ->
        computePlayerStats(players, links, rounds)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    companion object {
        fun factory(
            playerRepository: PlayerRepository,
            gameRepository: GameRepository,
            roundRepository: RoundRepository,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { StatsViewModel(playerRepository, gameRepository, roundRepository) }
        }
    }
}
