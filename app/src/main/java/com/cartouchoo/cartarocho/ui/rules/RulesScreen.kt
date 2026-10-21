package com.cartouchoo.cartarocho.ui.rules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cartouchoo.cartarocho.ui.common.Section

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Règles") },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Section(title = "Le jeu") {
                Text(
                    "Le tarot se joue de 3 à 5 joueurs avec un jeu de 78 cartes : 4 couleurs de 14 " +
                        "cartes, 21 atouts numérotés et l'Excuse. Le preneur annonce un contrat et doit, " +
                        "avec l'aide éventuelle d'un appelé, réunir seul suffisamment de points face aux " +
                        "autres joueurs (la défense).",
                )
            }

            Section(title = "Les bouts") {
                Text(
                    "Trois cartes appelées bouts (ou oudlers) valent chacune 4,5 points au comptage : " +
                        "le Petit (le 1 d'atout), le 21 d'atout et l'Excuse. Plus le preneur réunit de " +
                        "bouts dans ses plis, moins il a de points à réaliser pour remporter son contrat.",
                )
            }

            Section(title = "Points à réaliser selon les bouts") {
                Column {
                    TableRow("Bouts du preneur", "Points à réaliser", header = true)
                    HorizontalDivider()
                    TableRow("0 bout", "56 points")
                    TableRow("1 bout", "51 points")
                    TableRow("2 bouts", "41 points")
                    TableRow("3 bouts", "36 points")
                }
                Text(
                    "Le contrat est réussi si les points réalisés par le preneur sont supérieurs ou " +
                        "égaux à ce seuil.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            Section(title = "Les contrats") {
                Column {
                    TableRow("Contrat", "Coefficient", header = true)
                    HorizontalDivider()
                    TableRow("Petite", "× 1")
                    TableRow("Garde", "× 2")
                    TableRow("Garde Sans", "× 4")
                    TableRow("Garde Contre", "× 6")
                }
                Text(
                    "Dans Garde Sans, le preneur joue avec son chien mais sans l'écarter : le chien " +
                        "compte pour lui. Dans Garde Contre, le chien revient à la défense.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            Section(title = "Calcul de la valeur d'une donne") {
                Text(
                    "Base = (25 + écart entre points réalisés et points à réaliser) × coefficient du " +
                        "contrat.\n" +
                        "Si le contrat est réussi, cette base profite au preneur ; s'il est chuté, elle " +
                        "profite à la défense. À cette base s'ajoutent, selon les cas, le petit au bout, " +
                        "la poignée et le chelem.",
                )
            }

            Section(title = "Répartition des points") {
                Text(
                    "À 3 joueurs : le preneur gagne ou perd deux fois la valeur de la donne, chaque " +
                        "défenseur une fois.\n\n" +
                        "À 4 joueurs : le preneur gagne ou perd trois fois la valeur, chaque défenseur " +
                        "une fois.\n\n" +
                        "À 5 joueurs : le preneur gagne ou perd deux fois la valeur, l'appelé une fois, " +
                        "chaque défenseur une fois. Si le preneur s'appelle lui-même, il gagne ou perd " +
                        "quatre fois la valeur et chacun des quatre autres joueurs une fois.",
                )
            }

            Section(title = "Petit au bout") {
                Text(
                    "Si le Petit (le 1 d'atout) est joué au tout dernier pli de la donne, le camp qui " +
                        "remporte ce pli gagne un bonus de 10 points multiplié par le coefficient du " +
                        "contrat. Ce bonus peut profiter à l'attaque comme à la défense, selon qui a mené " +
                        "le Petit au bout.",
                )
            }

            Section(title = "Poignée") {
                Text(
                    "Un joueur qui détient un grand nombre d'atouts peut annoncer une poignée en les " +
                        "montrant avant de jouer : simple (20 points), double (30 points) ou triple " +
                        "(40 points). Ce bonus n'est jamais multiplié par le coefficient du contrat et " +
                        "profite toujours au camp qui remporte le contrat, quel que soit le camp qui l'a " +
                        "annoncée.",
                )
            }

            Section(title = "Chelem") {
                Text(
                    "Le chelem consiste, pour le preneur (et son éventuel appelé), à remporter la " +
                        "totalité des plis de la donne.",
                )
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    TableRow("Annoncé et réussi", "+ 400 points")
                    TableRow("Non annoncé et réussi", "+ 200 points")
                    TableRow("Annoncé et raté", "− 200 points")
                }
            }

            Section(title = "Misère (règle maison)") {
                Text(
                    "Cette règle n'existe pas dans le tarot officiel : c'est une variante ajoutée dans " +
                        "cette application. Un joueur peut annoncer une misère, indépendamment du contrat " +
                        "en cours. Chaque misère annoncée rapporte 10 points à son auteur, pris à parts " +
                        "égales à chacun des autres joueurs. Un même joueur peut cumuler plusieurs " +
                        "misères différentes sur une même donne.",
                )
            }
        }
    }
}

@Composable
private fun TableRow(label: String, value: String, header: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (header) FontWeight.Bold else FontWeight.Normal,
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (header) FontWeight.Bold else FontWeight.Normal,
        )
    }
}
