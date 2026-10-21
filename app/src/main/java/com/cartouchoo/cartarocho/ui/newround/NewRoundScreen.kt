package com.cartouchoo.cartarocho.ui.newround

import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cartouchoo.cartarocho.data.model.Player
import com.cartouchoo.cartarocho.data.repository.GameRepository
import com.cartouchoo.cartarocho.data.repository.RoundRepository
import com.cartouchoo.cartarocho.domain.Camp
import com.cartouchoo.cartarocho.domain.ChelemResult
import com.cartouchoo.cartarocho.domain.Contract
import com.cartouchoo.cartarocho.domain.MisereType
import com.cartouchoo.cartarocho.domain.PetitAuBoutCamp
import com.cartouchoo.cartarocho.domain.PoigneeType
import com.cartouchoo.cartarocho.ui.common.Section
import com.cartouchoo.cartarocho.ui.common.contractLabel
import com.cartouchoo.cartarocho.ui.common.formatScore

private val CONTRACT_OPTIONS = Contract.entries.map { it to contractLabel(it) }

private val BOUTS_OPTIONS = listOf(0, 1, 2, 3)

private val PETIT_AU_BOUT_OPTIONS = listOf(
    PetitAuBoutCamp.NONE to "Aucun",
    PetitAuBoutCamp.ATTACK to "Attaque",
    PetitAuBoutCamp.DEFENSE to "Défense",
)

private val POIGNEE_OPTIONS = listOf(
    PoigneeType.NONE to "Aucune",
    PoigneeType.SIMPLE to "Simple",
    PoigneeType.DOUBLE to "Double",
    PoigneeType.TRIPLE to "Triple",
)

private val CAMP_OPTIONS = listOf(
    Camp.ATTACK to "Attaque",
    Camp.DEFENSE to "Défense",
)

private val CHELEM_OPTIONS = listOf(
    ChelemResult.NONE to "Aucun",
    ChelemResult.ANNOUNCED_WON to "Annoncé et réussi",
    ChelemResult.UNANNOUNCED_WON to "Non annoncé et réussi",
    ChelemResult.ANNOUNCED_LOST to "Annoncé et raté",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewRoundScreen(
    gameId: Long,
    roundId: Long?,
    onDone: () -> Unit,
    gameRepository: GameRepository,
    roundRepository: RoundRepository,
    viewModel: NewRoundViewModel = viewModel(
        factory = NewRoundViewModel.factory(gameId, roundId, gameRepository, roundRepository),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val photoUri = remember { trickPhotoUri(context) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        if (!saved) return@rememberLauncherForActivityResult
        viewModel.onPhotoAnalyzing()
        scope.launch {
            try {
                viewModel.onPhotoRead(readWordsFromPhoto(context, photoUri))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                viewModel.onPhotoFailed("La photo n'a pas pu être analysée. Réessaie avec plus de lumière.")
            }
        }
    }
    val takeTrickPhoto: () -> Unit = {
        try {
            cameraLauncher.launch(photoUri)
        } catch (e: ActivityNotFoundException) {
            viewModel.onPhotoFailed("Aucune application d'appareil photo n'a été trouvée sur ce téléphone.")
        }
    }

    uiState.photoAnalysis?.let { analysis ->
        PhotoAnalysisDialog(
            analysis = analysis,
            onCountChange = viewModel::updateCardCount,
            onApply = viewModel::applyCardCount,
            onRetake = {
                viewModel.dismissPhotoAnalysis()
                takeTrickPhoto()
            },
            onDismiss = viewModel::dismissPhotoAnalysis,
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditing) "Modifier la donne" else "Nouveau tour") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
            )
        },
        bottomBar = {
            Surface(shadowElevation = 4.dp) {
                Button(
                    onClick = { viewModel.validate(onDone) },
                    enabled = uiState.canValidate,
                    modifier = Modifier
                        // Keep the button above the navigation bar, and above the keyboard when it's open.
                        .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Text("Valider")
                }
            }
        },
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Section(title = "Contrat") {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    CONTRACT_OPTIONS.forEachIndexed { index, (value, label) ->
                        SegmentedButton(
                            selected = uiState.contract == value,
                            onClick = { viewModel.setContract(value) },
                            shape = SegmentedButtonDefaults.itemShape(index, CONTRACT_OPTIONS.size),
                            icon = {},
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    letterSpacing = 0.sp,
                                ),
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }

            Section(title = "Preneur") {
                PlayerChipRow(
                    players = uiState.players,
                    selectedId = uiState.takerId,
                    onSelect = viewModel::setTaker,
                )
            }

            if (uiState.playerCount == 5) {
                Section(title = "Appelé") {
                    PlayerChipRow(
                        players = uiState.players,
                        selectedId = uiState.calledId,
                        onSelect = viewModel::setCalled,
                    )
                }
            }

            Section(title = "Nombre de bouts") {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    BOUTS_OPTIONS.forEachIndexed { index, value ->
                        SegmentedButton(
                            selected = uiState.bouts == value,
                            onClick = { viewModel.setBouts(value) },
                            shape = SegmentedButtonDefaults.itemShape(index, BOUTS_OPTIONS.size),
                        ) {
                            Text(value.toString())
                        }
                    }
                }
            }

            Section(title = "Points réalisés") {
                OutlinedTextField(
                    value = uiState.pointsText,
                    onValueChange = viewModel::setPointsText,
                    label = { Text("Points (0 à 91, demi-points autorisés)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = takeTrickPhoto,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Compter les points avec une photo des plis")
                }
            }

            ExpandableSection(
                title = "Petit au bout",
                initiallyExpanded = uiState.petitAuBout != PetitAuBoutCamp.NONE,
            ) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    PETIT_AU_BOUT_OPTIONS.forEachIndexed { index, (value, label) ->
                        SegmentedButton(
                            selected = uiState.petitAuBout == value,
                            onClick = { viewModel.setPetitAuBout(value) },
                            shape = SegmentedButtonDefaults.itemShape(index, PETIT_AU_BOUT_OPTIONS.size),
                        ) {
                            Text(label)
                        }
                    }
                }
            }

            ExpandableSection(
                title = "Poignée",
                initiallyExpanded = uiState.poignee != PoigneeType.NONE,
            ) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    POIGNEE_OPTIONS.forEachIndexed { index, (value, label) ->
                        SegmentedButton(
                            selected = uiState.poignee == value,
                            onClick = { viewModel.setPoignee(value) },
                            shape = SegmentedButtonDefaults.itemShape(index, POIGNEE_OPTIONS.size),
                        ) {
                            Text(label)
                        }
                    }
                }
                if (uiState.poignee != PoigneeType.NONE) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Annoncée par", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        CAMP_OPTIONS.forEachIndexed { index, (value, label) ->
                            SegmentedButton(
                                selected = uiState.poigneeCamp == value,
                                onClick = { viewModel.setPoigneeCamp(value) },
                                shape = SegmentedButtonDefaults.itemShape(index, CAMP_OPTIONS.size),
                            ) {
                                Text(label)
                            }
                        }
                    }
                }
            }

            ExpandableSection(
                title = "Chelem",
                initiallyExpanded = uiState.chelem != ChelemResult.NONE,
            ) {
                Column {
                    CHELEM_OPTIONS.forEach { (value, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setChelem(value) },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = uiState.chelem == value, onClick = { viewModel.setChelem(value) })
                            Text(label, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }

            ExpandableSection(
                title = "Misères",
                initiallyExpanded = uiState.miseres.isNotEmpty(),
            ) {
                Column {
                    uiState.players.forEach { player ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(player.name, modifier = Modifier.weight(1f))
                            FilterChip(
                                selected = uiState.miseres.any { it.playerId == player.id && it.type == MisereType.ATOUT },
                                onClick = { viewModel.toggleMisere(player.id, MisereType.ATOUT) },
                                label = { Text("Atout") },
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            FilterChip(
                                selected = uiState.miseres.any { it.playerId == player.id && it.type == MisereType.TETES },
                                onClick = { viewModel.toggleMisere(player.id, MisereType.TETES) },
                                label = { Text("Têtes") },
                            )
                        }
                    }
                }
            }

            Section(title = "Aperçu") {
                val scores = uiState.previewScores
                if (scores == null) {
                    Text(
                        "Complétez la saisie pour voir l'aperçu des points.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                } else {
                    Column {
                        uiState.players.forEach { player ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(player.name)
                                Text(formatScore(scores[player.id] ?: 0.0), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/** Section repliable, masquée par défaut sauf si [initiallyExpanded] (par ex. une valeur déjà choisie). */
@Composable
private fun ExpandableSection(
    title: String,
    initiallyExpanded: Boolean = false,
    content: @Composable () -> Unit,
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Icon(
                imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = if (expanded) "Réduire" else "Étendre",
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                content()
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlayerChipRow(
    players: List<Player>,
    selectedId: Long?,
    onSelect: (Long) -> Unit,
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        players.forEach { player ->
            FilterChip(
                selected = player.id == selectedId,
                onClick = { onSelect(player.id) },
                label = { Text(player.name) },
            )
        }
    }
}
