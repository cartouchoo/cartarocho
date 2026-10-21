package com.cartouchoo.cartarocho.ui.game

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cartouchoo.cartarocho.data.repository.GameRepository
import com.cartouchoo.cartarocho.ui.common.formatDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesScreen(
    onBack: () -> Unit,
    onOpenGame: (gameId: Long) -> Unit,
    onNewGame: () -> Unit,
    gameRepository: GameRepository,
    viewModel: GamesViewModel = viewModel(factory = GamesViewModel.factory(gameRepository)),
) {
    val games by viewModel.games.collectAsStateWithLifecycle()
    val renameDialogState by viewModel.renameDialogState.collectAsStateWithLifecycle()
    var pendingDeleteId by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Parties") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewGame,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nouvelle partie") },
            )
        },
    ) { innerPadding ->
        if (games.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text("Aucune partie pour l'instant.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(innerPadding),
                // Leave room so the last game isn't hidden behind the button.
                contentPadding = PaddingValues(bottom = 88.dp),
            ) {
                items(games, key = { it.game.id }) { item ->
                    ListItem(
                        modifier = Modifier.clickable { onOpenGame(item.game.id) },
                        headlineContent = {
                            Text(item.game.name ?: formatDateTime(item.game.createdAt))
                        },
                        supportingContent = {
                            val players = item.players.joinToString(", ") { it.name }
                            Text(
                                if (item.game.name != null) {
                                    "${formatDateTime(item.game.createdAt)} • $players"
                                } else {
                                    players
                                },
                            )
                        },
                        trailingContent = {
                            Row {
                                IconButton(onClick = { viewModel.openRenameDialog(item.game) }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Renommer")
                                }
                                IconButton(onClick = { pendingDeleteId = item.game.id }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Supprimer")
                                }
                            }
                        },
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    val deleteId = pendingDeleteId
    if (deleteId != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null },
            title = { Text("Supprimer cette partie ?") },
            text = { Text("Cette action est irréversible : toutes les donnes de cette partie seront perdues.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteGame(deleteId)
                        pendingDeleteId = null
                    },
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteId = null }) {
                    Text("Annuler")
                }
            },
        )
    }

    val renameState = renameDialogState
    if (renameState != null) {
        AlertDialog(
            onDismissRequest = viewModel::dismissRenameDialog,
            title = { Text("Renommer la partie") },
            text = {
                OutlinedTextField(
                    value = renameState.name,
                    onValueChange = viewModel::updateRenameDialogName,
                    label = { Text("Nom (optionnel)") },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::confirmRenameDialog) {
                    Text("Enregistrer")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissRenameDialog) {
                    Text("Annuler")
                }
            },
        )
    }
}
