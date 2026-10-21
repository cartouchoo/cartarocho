package com.cartouchoo.cartarocho.ui.newgame

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cartouchoo.cartarocho.data.model.Player
import com.cartouchoo.cartarocho.data.repository.GameRepository
import com.cartouchoo.cartarocho.data.repository.PlayerRepository

private val PLAYER_COUNT_OPTIONS = listOf(3, 4, 5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewGameScreen(
    onBack: () -> Unit,
    onGameCreated: (gameId: Long) -> Unit,
    playerRepository: PlayerRepository,
    gameRepository: GameRepository,
    viewModel: NewGameViewModel = viewModel(
        factory = NewGameViewModel.factory(playerRepository, gameRepository),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nouvelle partie") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
        ) {
            Text("Nombre de joueurs", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                PLAYER_COUNT_OPTIONS.forEachIndexed { index, count ->
                    SegmentedButton(
                        selected = uiState.playerCount == count,
                        onClick = { viewModel.setPlayerCount(count) },
                        shape = SegmentedButtonDefaults.itemShape(index, PLAYER_COUNT_OPTIONS.size),
                    ) {
                        Text(count.toString())
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Joueurs (${uiState.selectedPlayerIds.size}/${uiState.playerCount})",
                style = MaterialTheme.typography.titleMedium,
            )

            if (uiState.players.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Aucun joueur. Ajoutez des joueurs avant de créer une partie.")
                }
            } else {
                // weight(1f) makes the list scroll instead of pushing "Valider" off-screen.
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    items(uiState.players, key = { it.id }) { player ->
                        PlayerSelectionRow(
                            player = player,
                            selected = player.id in uiState.selectedPlayerIds,
                            selectionFull = uiState.selectedPlayerIds.size >= uiState.playerCount,
                            onToggle = { viewModel.togglePlayer(player.id) },
                        )
                    }
                }
            }

            Button(
                onClick = { viewModel.onValidate(onGameCreated) },
                enabled = uiState.canValidate,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            ) {
                Text("Valider")
            }
        }
    }
}

@Composable
private fun PlayerSelectionRow(
    player: Player,
    selected: Boolean,
    selectionFull: Boolean,
    onToggle: () -> Unit,
) {
    val enabled = selected || !selectionFull
    ListItem(
        headlineContent = { Text(player.name) },
        leadingContent = {
            Checkbox(checked = selected, onCheckedChange = { onToggle() }, enabled = enabled)
        },
        modifier = Modifier.clickable(enabled = enabled, onClick = onToggle),
    )
}
