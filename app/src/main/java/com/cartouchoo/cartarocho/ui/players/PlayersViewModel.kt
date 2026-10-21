package com.cartouchoo.cartarocho.ui.players

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cartouchoo.cartarocho.data.model.Player
import com.cartouchoo.cartarocho.data.repository.PlayerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** État de la boîte de dialogue d'ajout/modification d'un joueur. [playerId] nul = création. */
data class PlayerDialogState(
    val playerId: Long? = null,
    val name: String = "",
)

class PlayersViewModel(
    private val repository: PlayerRepository,
) : ViewModel() {

    val players: StateFlow<List<Player>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _dialogState = MutableStateFlow<PlayerDialogState?>(null)
    val dialogState: StateFlow<PlayerDialogState?> = _dialogState.asStateFlow()

    fun openAddDialog() {
        _dialogState.value = PlayerDialogState()
    }

    fun openEditDialog(player: Player) {
        _dialogState.value = PlayerDialogState(playerId = player.id, name = player.name)
    }

    fun updateDialogName(name: String) {
        _dialogState.update { it?.copy(name = name.take(MAX_PLAYER_NAME_LENGTH)) }
    }

    fun dismissDialog() {
        _dialogState.value = null
    }

    fun confirmDialog() {
        val state = _dialogState.value ?: return
        val name = state.name.trim()
        if (name.isEmpty()) return
        viewModelScope.launch {
            if (state.playerId == null) {
                repository.add(name)
            } else {
                repository.rename(state.playerId, name)
            }
            _dialogState.value = null
        }
    }

    companion object {
        /** Les noms courts gardent le tableau de la partie lisible sans scroll horizontal. */
        const val MAX_PLAYER_NAME_LENGTH = 10

        fun factory(repository: PlayerRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { PlayersViewModel(repository) }
        }
    }
}
