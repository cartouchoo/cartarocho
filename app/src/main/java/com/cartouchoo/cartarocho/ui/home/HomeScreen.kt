package com.cartouchoo.cartarocho.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.automirrored.filled.Rule
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cartouchoo.cartarocho.ui.theme.CartarochoTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToGames: () -> Unit,
    onNavigateToPlayers: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToRules: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Cartarocho") })
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            HomeMenuItem(
                icon = Icons.Filled.Style,
                title = "Parties",
                onClick = onNavigateToGames,
            )
            HomeMenuItem(
                icon = Icons.Filled.Groups,
                title = "Joueurs",
                onClick = onNavigateToPlayers,
            )
            HomeMenuItem(
                icon = Icons.Filled.BarChart,
                title = "Statistiques",
                onClick = onNavigateToStats,
            )
            HomeMenuItem(
                icon = Icons.AutoMirrored.Filled.Rule,
                title = "Règles",
                onClick = onNavigateToRules,
            )
        }
    }
}

@Composable
private fun HomeMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        ListItem(
            headlineContent = { Text(title) },
            leadingContent = { Icon(icon, contentDescription = null) },
            trailingContent = { Icon(Icons.Filled.ChevronRight, contentDescription = null) },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    CartarochoTheme {
        HomeScreen(
            onNavigateToGames = {},
            onNavigateToPlayers = {},
            onNavigateToStats = {},
            onNavigateToRules = {},
        )
    }
}
