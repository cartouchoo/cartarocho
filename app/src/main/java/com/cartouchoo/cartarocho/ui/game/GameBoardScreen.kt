package com.cartouchoo.cartarocho.ui.game

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cartouchoo.cartarocho.data.model.Game
import com.cartouchoo.cartarocho.data.model.Player
import com.cartouchoo.cartarocho.data.model.RoundWithScores
import com.cartouchoo.cartarocho.data.repository.GameRepository
import com.cartouchoo.cartarocho.data.repository.RoundRepository
import com.cartouchoo.cartarocho.ui.common.formatDate
import com.cartouchoo.cartarocho.ui.common.formatRank
import com.cartouchoo.cartarocho.ui.common.formatScore
import kotlinx.coroutines.launch

/** Poids relatif de la colonne d'index par rapport à chaque colonne de joueur (qui a un poids de 1). */
private const val INDEX_COLUMN_WEIGHT = 0.9f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameBoardScreen(
    gameId: Long,
    onBack: () -> Unit,
    onNewRound: () -> Unit,
    onEditRound: (roundId: Long) -> Unit,
    gameRepository: GameRepository,
    roundRepository: RoundRepository,
    viewModel: GameBoardViewModel = viewModel(
        factory = GameBoardViewModel.factory(gameId, gameRepository, roundRepository),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val game by viewModel.game.collectAsStateWithLifecycle()
    val renameDialogOpen by viewModel.renameDialogOpen.collectAsStateWithLifecycle()
    val renameText by viewModel.renameText.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(game?.let(::formatGameTitle) ?: "Partie") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::openRenameDialog) {
                        Icon(Icons.Filled.Edit, contentDescription = "Renommer la partie")
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewRound,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nouveau tour") },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(bottom = 88.dp),
        ) {
            HeaderRow(players = uiState.players)
            HorizontalDivider()

            if (uiState.rounds.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Aucune donne. Appuyez sur « Nouveau tour » pour commencer.")
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(uiState.rounds, key = { it.id }) { round ->
                        RoundRow(
                            round = round,
                            players = uiState.players,
                            onEdit = { onEditRound(round.id) },
                            onDelete = {
                                viewModel.deleteRound(round)
                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Donne supprimée",
                                        actionLabel = "Annuler",
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        viewModel.undoDelete()
                                    }
                                }
                            },
                        )
                        HorizontalDivider()
                    }
                }
            }

            HorizontalDivider(thickness = 2.dp)
            TotalRow(players = uiState.players, totals = uiState.totals)
            RankingRow(players = uiState.players, ranking = uiState.ranking)
        }
    }

    if (renameDialogOpen) {
        AlertDialog(
            onDismissRequest = viewModel::dismissRenameDialog,
            title = { Text("Renommer la partie") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = viewModel::updateRenameText,
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

@Composable
private fun HeaderRow(players: List<Player>) {
    Row(modifier = Modifier.fillMaxWidth()) {
        TableCell(text = "N°", weight = INDEX_COLUMN_WEIGHT, emphasize = true)
        players.forEach { player ->
            TableCell(text = player.name, weight = 1f, emphasize = true)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RoundRow(
    round: RoundWithScores,
    players: List<Player>,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = {}, onLongClick = { showMenu = true }),
        ) {
            TableCell(text = (round.index + 1).toString(), weight = INDEX_COLUMN_WEIGHT)
            players.forEach { player ->
                TableCell(text = formatScore(round.scores[player.id] ?: 0.0), weight = 1f)
            }
        }
        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
            DropdownMenuItem(
                text = { Text("Modifier") },
                onClick = {
                    showMenu = false
                    onEdit()
                },
            )
            DropdownMenuItem(
                text = { Text("Supprimer") },
                onClick = {
                    showMenu = false
                    onDelete()
                },
            )
        }
    }
}

@Composable
private fun TotalRow(
    players: List<Player>,
    totals: Map<Long, Double>,
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        TableCell(text = "Total", weight = INDEX_COLUMN_WEIGHT, emphasize = true)
        players.forEach { player ->
            TableCell(text = formatScore(totals[player.id] ?: 0.0), weight = 1f, emphasize = true)
        }
    }
}

@Composable
private fun RankingRow(
    players: List<Player>,
    ranking: Map<Long, Int>,
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        TableCell(text = "", weight = INDEX_COLUMN_WEIGHT)
        players.forEach { player ->
            val rank = ranking[player.id] ?: 0
            TableCell(
                text = formatRank(rank),
                weight = 1f,
                emphasize = rank == 1,
                color = if (rank == 1) MaterialTheme.colorScheme.primary else Color.Unspecified,
            )
        }
    }
}

@Composable
private fun RowScope.TableCell(
    text: String,
    weight: Float,
    emphasize: Boolean = false,
    color: Color = Color.Unspecified,
) {
    Box(
        modifier = Modifier
            .weight(weight)
            .padding(vertical = 12.dp, horizontal = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Normal,
            color = color,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

private fun formatGameTitle(game: Game): String = game.name ?: "Partie du ${formatDate(game.createdAt)}"
