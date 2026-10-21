package com.cartouchoo.cartarocho.ui.players

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cartouchoo.cartarocho.data.model.Player
import com.cartouchoo.cartarocho.data.repository.PlayerRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayersScreen(
    onBack: () -> Unit,
    playerRepository: PlayerRepository,
    viewModel: PlayersViewModel = viewModel(factory = PlayersViewModel.factory(playerRepository)),
) {
    val players by viewModel.players.collectAsStateWithLifecycle()
    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Joueurs") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = viewModel::openAddDialog,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nouveau joueur") },
            )
        },
    ) { innerPadding ->
        if (players.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text("Aucun joueur pour l'instant")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(innerPadding)) {
                items(players, key = { it.id }) { player ->
                    PlayerRow(player = player, onEdit = { viewModel.openEditDialog(player) })
                    HorizontalDivider()
                }
            }
        }
    }

    val state = dialogState
    if (state != null) {
        PlayerDialog(
            state = state,
            onNameChange = viewModel::updateDialogName,
            onConfirm = viewModel::confirmDialog,
            onDismiss = viewModel::dismissDialog,
        )
    }
}

@Composable
private fun PlayerRow(player: Player, onEdit: () -> Unit) {
    ListItem(
        headlineContent = { Text(player.name) },
        trailingContent = {
            TextButton(onClick = onEdit) {
                Text("Modifier")
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerDialog(
    state: PlayerDialogState,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val isEditing = state.playerId != null
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Modifier le joueur" else "Nouveau joueur") },
        text = {
            OutlinedTextField(
                value = state.name,
                onValueChange = onNameChange,
                label = { Text("Nom") },
                supportingText = {
                    Text("${state.name.length}/${PlayersViewModel.MAX_PLAYER_NAME_LENGTH}")
                },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = state.name.isNotBlank()) {
                Text(if (isEditing) "Enregistrer" else "Ajouter")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        },
    )
}
