package com.example.shottracker.feature.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shottracker.domain.model.Round
import com.example.shottracker.domain.model.RoundStatus
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onRoundClick: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var roundToDelete by remember { mutableStateOf<Round?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Round History") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.rounds.isEmpty() && !uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No rounds yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                item {
                    HandicapIndexCard(
                        handicapIndex = uiState.handicapIndex,
                        eligibleRoundCount = uiState.handicapEligibleRoundCount,
                        formulaDescription = uiState.handicapFormulaDescription
                    )
                }

                // Most recent up to 20 rounds — eligible for the handicap-index calculation.
                items(uiState.rounds.take(20)) { round ->
                    RoundHistoryCard(
                        round = round,
                        differential = uiState.differentialsById[round.id],
                        isHandicapCounted = round.id in uiState.handicapCountedRoundIds,
                        onClick = { onRoundClick(round.id) },
                        onDelete = { roundToDelete = round }
                    )
                }
                // Thin divider separating handicap-eligible rounds from older ones.
                if (uiState.rounds.size > 20) {
                    item {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    items(uiState.rounds.drop(20)) { round ->
                        RoundHistoryCard(
                            round = round,
                            differential = uiState.differentialsById[round.id],
                            isHandicapCounted = false,
                            onClick = { onRoundClick(round.id) },
                            onDelete = { roundToDelete = round }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }

        // Delete confirmation dialog
        roundToDelete?.let { round ->
            AlertDialog(
                onDismissRequest = { roundToDelete = null },
                title = { Text("Delete Round?") },
                text = { Text("Are you sure you want to delete this round at ${round.courseName}? This cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteRound(round.id)
                            roundToDelete = null
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { roundToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun HandicapIndexCard(
    handicapIndex: Double?,
    eligibleRoundCount: Int,
    formulaDescription: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Handicap Index",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (handicapIndex == null) {
                    Text(
                        text = if (eligibleRoundCount == 0) "No rated rounds yet"
                        else "Need at least 3 rated rounds (have $eligibleRoundCount)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                } else if (formulaDescription != null) {
                    Text(
                        text = formulaDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
            if (handicapIndex != null) {
                Text(
                    text = String.format("%.1f", handicapIndex),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun RoundHistoryCard(
    round: Round,
    differential: Double?,
    isHandicapCounted: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = when (round.status) {
                RoundStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primaryContainer
                RoundStatus.COMPLETED -> MaterialTheme.colorScheme.surface
                RoundStatus.ABANDONED -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = round.courseName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = round.startDateTime.format(dateFormatter),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (round.status == RoundStatus.IN_PROGRESS) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "In Progress",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if ((round.totalAdjustment ?: 0) > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Adjusted Score: ${(round.totalScore ?: 0) - round.totalAdjustment!!}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                differential?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Differential: ${String.format("%.1f", it)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isHandicapCounted) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                round.pcc?.takeIf { it != 0 }?.let { pcc ->
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "PCC: ${if (pcc > 0) "+$pcc" else "$pcc"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                if (round.totalScore != null) {
                    Text(
                        text = "${round.totalScore}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    round.scoreToPar?.let { scoreToPar ->
                        Text(
                            text = if (scoreToPar >= 0) "+$scoreToPar" else "$scoreToPar",
                            style = MaterialTheme.typography.bodyMedium,
                            color = when {
                                scoreToPar < 0 -> MaterialTheme.colorScheme.primary
                                scoreToPar > 0 -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                } else {
                    Text(
                        text = "${round.holesPlayed} holes",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
