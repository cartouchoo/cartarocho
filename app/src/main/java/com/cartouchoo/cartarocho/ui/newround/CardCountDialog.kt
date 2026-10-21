package com.cartouchoo.cartarocho.ui.newround

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.cartouchoo.cartarocho.domain.CardCount

/** Affiche l'étape en cours de la saisie par photo : lecture, vérification ou erreur. */
@Composable
fun PhotoAnalysisDialog(
    analysis: PhotoAnalysis,
    onCountChange: (CardCount) -> Unit,
    onApply: () -> Unit,
    onRetake: () -> Unit,
    onDismiss: () -> Unit,
) {
    when (analysis) {
        PhotoAnalysis.Analyzing -> AlertDialog(
            onDismissRequest = {},
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
            confirmButton = {},
            title = { Text("Lecture des cartes…") },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.padding(end = 16.dp))
                    Text("Analyse de la photo sur le téléphone.")
                }
            },
        )

        is PhotoAnalysis.Failed -> AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Photo non lue") },
            text = { Text(analysis.message) },
            confirmButton = { TextButton(onClick = onRetake) { Text("Reprendre une photo") } },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Fermer") } },
        )

        is PhotoAnalysis.Review -> CardCountDialog(
            review = analysis,
            onCountChange = onCountChange,
            onApply = onApply,
            onRetake = onRetake,
            onDismiss = onDismiss,
        )
    }
}

@Composable
private fun CardCountDialog(
    review: PhotoAnalysis.Review,
    onCountChange: (CardCount) -> Unit,
    onApply: () -> Unit,
    onRetake: () -> Unit,
    onDismiss: () -> Unit,
) {
    val count = review.count
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Vérifier les cartes") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = if (review.trumpsSeen.isEmpty()) {
                        "Aucun atout lu sur la photo."
                    } else {
                        "Atouts lus : " + review.trumpsSeen.joinToString(", ")
                    },
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    "Ajoute l'Excuse et les cartes basses sans numéro : la photo ne peut pas les lire.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                SectionLabel("Bouts")
                CheckRow("Petit (1 d'atout)", count.petit) { onCountChange(count.copy(petit = it)) }
                CheckRow("21 d'atout", count.vingtEtUn) { onCountChange(count.copy(vingtEtUn = it)) }
                CheckRow("Excuse", count.excuse) { onCountChange(count.copy(excuse = it)) }

                SectionLabel("Têtes")
                HeadStepper("Rois", count.rois) { onCountChange(count.copy(rois = it)) }
                HeadStepper("Dames", count.dames) { onCountChange(count.copy(dames = it)) }
                HeadStepper("Cavaliers", count.cavaliers) { onCountChange(count.copy(cavaliers = it)) }
                HeadStepper("Valets", count.valets) { onCountChange(count.copy(valets = it)) }

                SectionLabel("Nombre total de cartes")
                OutlinedTextField(
                    value = if (count.totalCards == 0) "" else count.totalCards.toString(),
                    onValueChange = { text ->
                        val digits = text.filter(Char::isDigit).take(2)
                        val value = digits.toIntOrNull() ?: 0
                        onCountChange(count.copy(totalCards = value.coerceAtMost(CardCount.MAX_CARDS)))
                    },
                    label = { Text("Toutes les cartes du tas") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (count.totalCards < count.honorCards) {
                    Text(
                        "Compté comme ${count.honorCards} cartes (bouts et têtes).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("${formatPoints(count.points)} points", fontWeight = FontWeight.Bold)
                    Text("${count.bouts} bout${if (count.bouts > 1) "s" else ""}", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(onClick = onRetake, modifier = Modifier.fillMaxWidth()) {
                    Text("Reprendre une photo")
                }
            }
        },
        confirmButton = { TextButton(onClick = onApply) { Text("Appliquer") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } },
    )
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
    )
}

@Composable
private fun CheckRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(label)
    }
}

@Composable
private fun HeadStepper(label: String, value: Int, onValueChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f))
        IconButton(onClick = { onValueChange(value - 1) }, enabled = value > 0) {
            Icon(Icons.Filled.Remove, contentDescription = "$label : retirer")
        }
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.width(24.dp),
        )
        IconButton(onClick = { onValueChange(value + 1) }, enabled = value < CardCount.MAX_PER_HEAD) {
            Icon(Icons.Filled.Add, contentDescription = "$label : ajouter")
        }
    }
}

private fun formatPoints(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString() else value.toString().replace('.', ',')
