package com.cartouchoo.cartarocho.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cartouchoo.cartarocho.data.repository.GameRepository
import com.cartouchoo.cartarocho.data.repository.PlayerRepository
import com.cartouchoo.cartarocho.data.repository.RoundRepository
import com.cartouchoo.cartarocho.ui.common.contractLabel
import com.cartouchoo.cartarocho.ui.common.formatScore
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onBack: () -> Unit,
    playerRepository: PlayerRepository,
    gameRepository: GameRepository,
    roundRepository: RoundRepository,
    viewModel: StatsViewModel = viewModel(
        factory = StatsViewModel.factory(playerRepository, gameRepository, roundRepository),
    ),
) {
    val stats by viewModel.stats.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistiques") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
            )
        },
    ) { innerPadding ->
        if (stats.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text("Aucune statistique pour l'instant. Jouez une partie pour commencer.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(stats, key = { it.player.id }) { stat ->
                    PlayerStatsCard(stat)
                }
            }
        }
    }
}

@Composable
private fun PlayerStatsCard(stat: PlayerStats) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(stat.player.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    formatScore(stat.totalScore),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            val winRate = if (stat.gamesPlayed > 0) {
                " (${(stat.wins * 100.0 / stat.gamesPlayed).roundToInt()} %)"
            } else {
                ""
            }
            Text(
                "Parties jouées : ${stat.gamesPlayed}  •  Victoires : ${stat.wins}$winRate",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                "Moyenne par partie : ${formatScore(stat.averageScorePerGame)}",
                style = MaterialTheme.typography.bodySmall,
            )

            if (stat.timesTaker > 0) {
                val successRate = (stat.takerSuccesses * 100.0 / stat.timesTaker).roundToInt()
                Text(
                    "Preneur : ${stat.timesTaker} fois, réussi ${stat.takerSuccesses} ($successRate %)",
                    style = MaterialTheme.typography.bodySmall,
                )
                val contractBreakdown = stat.contractCounts
                    .filterValues { it > 0 }
                    .entries
                    .joinToString(" · ") { (contract, count) -> "${contractLabel(contract)} $count" }
                if (contractBreakdown.isNotEmpty()) {
                    Text(contractBreakdown, style = MaterialTheme.typography.bodySmall)
                }
            } else {
                Text("Jamais preneur", style = MaterialTheme.typography.bodySmall)
            }

            if (stat.chelemCount > 0 || stat.poigneeCount > 0 || stat.misereCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Chelems : ${stat.chelemCount}  •  Poignées : ${stat.poigneeCount}  •  Misères : ${stat.misereCount}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
