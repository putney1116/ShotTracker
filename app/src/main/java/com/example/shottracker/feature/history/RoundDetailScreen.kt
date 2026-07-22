package com.example.shottracker.feature.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shottracker.domain.model.HoleScore
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoundDetailScreen(
    roundId: Long,
    onBack: () -> Unit,
    viewModel: RoundDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val showAdj = uiState.holeScores.any { (it.adjustment ?: 0) > 0 }
    val showHcp = uiState.holeHandicaps.isNotEmpty()
    val dateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")

    var showDatePicker by remember { mutableStateOf(false) }
    var teeMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.round?.courseName ?: "Round Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.isEditing) {
                        IconButton(onClick = viewModel::exitEditMode) {
                            Icon(Icons.Default.Done, contentDescription = "Done editing")
                        }
                    } else {
                        IconButton(onClick = viewModel::enterEditMode) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit round")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Date sub-header (clickable in edit mode)
            uiState.round?.let { round ->
                val dateModifier = if (uiState.isEditing) {
                    Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                        .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 4.dp)
                } else {
                    Modifier.padding(start = 16.dp, top = 8.dp)
                }
                Text(
                    text = round.startDateTime.format(dateFormatter) +
                        if (uiState.isEditing) "  (tap to change)" else "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (uiState.isEditing) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = dateModifier
                )
            }

            // Tee + differential rows: line 1 = round settings (tee/rating/slope),
            // line 2 = results computed from those settings (differential/pcc).
            uiState.tee?.let { tee ->
                val diff = viewModel.handicapDifferential
                val rating = tee.rating?.let { String.format("%.1f", it) } ?: "—"
                val slope = tee.slope?.toString() ?: "—"
                val diffText = diff?.let { String.format("%.1f", it) } ?: "—"
                val pcc = uiState.round?.pcc ?: 0
                val pccText = if (pcc > 0) "+$pcc" else "$pcc"
                Text(
                    text = "Tee: ${tee.name}  ·  Rating $rating  ·  Slope $slope",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
                val differentialColor = if (uiState.isHandicapCounted) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
                Row(
                    modifier = Modifier.padding(start = 16.dp, top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    Text(
                        text = "Differential $diffText",
                        style = MaterialTheme.typography.bodySmall,
                        color = differentialColor
                    )
                    Text(
                        text = "  ·  PCC $pccText",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Tee selector (only in edit mode)
            if (uiState.isEditing && uiState.availableTees.isNotEmpty()) {
                Box(
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp)
                ) {
                    TextButton(onClick = { teeMenuExpanded = true }) {
                        Text(
                            text = "Change tee: ${uiState.tee?.name ?: "Select"}"
                        )
                    }
                    DropdownMenu(
                        expanded = teeMenuExpanded,
                        onDismissRequest = { teeMenuExpanded = false }
                    ) {
                        uiState.availableTees.forEach { tee ->
                            DropdownMenuItem(
                                text = {
                                    val rating = tee.rating?.let { String.format("%.1f", it) } ?: "—"
                                    val slope = tee.slope?.toString() ?: "—"
                                    val distSuffix = tee.totalDistance?.let { " · $it yds" } ?: ""
                                    Text("${tee.name} · $rating / $slope$distSuffix")
                                },
                                onClick = {
                                    viewModel.setRoundTee(tee.id)
                                    teeMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // PCC stepper (only in edit mode)
            if (uiState.isEditing) {
                val currentPcc = uiState.round?.pcc ?: 0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PCC (Playing Conditions)",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { viewModel.setRoundPcc(currentPcc - 1) },
                        enabled = currentPcc > -3
                    ) {
                        Text("−", style = MaterialTheme.typography.titleLarge)
                    }
                    Text(
                        text = if (currentPcc > 0) "+$currentPcc" else "$currentPcc",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(40.dp),
                        textAlign = TextAlign.Center
                    )
                    IconButton(
                        onClick = { viewModel.setRoundPcc(currentPcc + 1) },
                        enabled = currentPcc < 3
                    ) {
                        Text("+", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }

            // Summary card — matches scorecard order: Par, Shots, Pen, Putts, Score, +/-
            SummaryCard(
                totalPar = viewModel.totalPar,
                totalShots = viewModel.totalShots,
                totalPenalties = viewModel.totalPenalties,
                totalPutts = viewModel.totalPutts,
                totalScore = viewModel.totalScore,
                totalAdjustment = viewModel.totalAdjustment,
                totalGir = viewModel.totalGir,
                scoreToPar = viewModel.scoreToPar
            )

            // Scorecard column header
            ScorecardHeader(showAdj = showAdj, showHcp = showHcp)
            HorizontalDivider()

            // Hole rows + Front 9 / Back 9 summaries
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(uiState.holeScores) { holeScore ->
                    HoleScoreRow(
                        holeScore = holeScore,
                        showAdj = showAdj,
                        showHcp = showHcp,
                        holeHandicap = uiState.holeHandicaps[holeScore.holeNumber],
                        onClick = if (uiState.isEditing) {
                            { viewModel.startEditingHole(holeScore) }
                        } else null
                    )
                    HorizontalDivider()

                    if (holeScore.holeNumber == 9) {
                        SummaryRow(
                            label = "Front 9",
                            holes = uiState.holeScores.filter { it.holeNumber in 1..9 },
                            showAdj = showAdj,
                            showHcp = showHcp
                        )
                        HorizontalDivider()
                    } else if (holeScore.holeNumber == 18) {
                        SummaryRow(
                            label = "Back 9",
                            holes = uiState.holeScores.filter { it.holeNumber in 10..18 },
                            showAdj = showAdj,
                            showHcp = showHcp
                        )
                        HorizontalDivider()
                    }
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }

        // Date picker dialog
        if (showDatePicker) {
            val initialMillis = uiState.round?.startTime?.toEpochMilli()
                ?: Instant.now().toEpochMilli()
            val state = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    Button(onClick = {
                        val millis = state.selectedDateMillis
                        if (millis != null) {
                            val picked = Instant.ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC).toLocalDate()
                            viewModel.setRoundDate(picked)
                        }
                        showDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = state)
            }
        }

        // Hole edit dialog (shown when editingHole is set)
        uiState.editingHole?.let { holeScore ->
            EditHoleDialog(
                holeScore = holeScore,
                holeHandicap = uiState.holeHandicaps[holeScore.holeNumber],
                editShots = uiState.editShots,
                editPutts = uiState.editPutts,
                editPenalties = uiState.editPenalties,
                editAdjustment = uiState.editAdjustment,
                onShotsChanged = viewModel::onEditShotsChanged,
                onPuttsChanged = viewModel::onEditPuttsChanged,
                onPenaltiesChanged = viewModel::onEditPenaltiesChanged,
                onAdjustmentIncrement = viewModel::incrementEditAdjustment,
                onAdjustmentDecrement = viewModel::decrementEditAdjustment,
                onSave = viewModel::saveHoleEdit,
                onCancel = viewModel::cancelHoleEdit
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
private fun EditHoleDialog(
    holeScore: HoleScore,
    holeHandicap: Int?,
    editShots: Int?,
    editPutts: Int?,
    editPenalties: Int?,
    editAdjustment: Int?,
    onShotsChanged: (Int?) -> Unit,
    onPuttsChanged: (Int?) -> Unit,
    onPenaltiesChanged: (Int?) -> Unit,
    onAdjustmentIncrement: () -> Unit,
    onAdjustmentDecrement: () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val computedScore = (editShots ?: 0) + (editPutts ?: 0) + (editPenalties ?: 0)
    val adjustment = editAdjustment ?: 0
    val adjustedScore = computedScore - adjustment

    Dialog(onDismissRequest = onCancel) {
        Surface(
            shape = AlertDialogDefaults.shape,
            color = AlertDialogDefaults.containerColor,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // Title
                Text(
                    text = "Edit Hole ${holeScore.holeNumber}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = AlertDialogDefaults.titleContentColor
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Par / Score / Adj (when set) / +/-
                val scoreToPar = computedScore - holeScore.par
                val scoreToParText = when {
                    computedScore == 0 -> "-"
                    scoreToPar >= 0 -> "+$scoreToPar"
                    else -> "$scoreToPar"
                }
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Par: ${holeScore.par}", style = MaterialTheme.typography.bodyMedium, maxLines = 1, softWrap = false)
                    if (holeHandicap != null) {
                        Text("Hcp: $holeHandicap", style = MaterialTheme.typography.bodyMedium, maxLines = 1, softWrap = false)
                    }
                    Text("Score: $computedScore", style = MaterialTheme.typography.bodyMedium, maxLines = 1, softWrap = false)
                    if (adjustment > 0) {
                        Text("Adj: $adjustedScore", style = MaterialTheme.typography.bodyMedium, maxLines = 1, softWrap = false)
                    }
                    Text("+/-: $scoreToParText", style = MaterialTheme.typography.bodyMedium, maxLines = 1, softWrap = false)
                }
                Spacer(modifier = Modifier.height(16.dp))

                StatChip(
                    label = "Shots",
                    value = editShots ?: 0,
                    onIncrement = { onShotsChanged((editShots ?: 0) + 1) },
                    onDecrement = { onShotsChanged((editShots ?: 0) - 1) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                StatChip(
                    label = "Penalties",
                    value = editPenalties ?: 0,
                    onIncrement = { onPenaltiesChanged((editPenalties ?: 0) + 1) },
                    onDecrement = { onPenaltiesChanged((editPenalties ?: 0) - 1) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                StatChip(
                    label = "Putts",
                    value = editPutts ?: 0,
                    onIncrement = { onPuttsChanged((editPutts ?: 0) + 1) },
                    onDecrement = { onPuttsChanged((editPutts ?: 0) - 1) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom row: Adj chip on the LEFT, Cancel + Save on the RIGHT, same baseline.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatChip(
                        label = "Adj",
                        value = adjustment,
                        onIncrement = onAdjustmentIncrement,
                        onDecrement = onAdjustmentDecrement
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = onCancel) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onSave) { Text("Save") }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun StatChip(
    label: String,
    value: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isFilled = value > 0
    // Outer Box has no clip so the floating label can draw outside the inner box's bounds.
    Box {
        // Inner box: the actual outlined chip with border + click handling.
        Box(
            modifier = modifier
                .height(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(
                    BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    RoundedCornerShape(8.dp)
                )
                .combinedClickable(onClick = onIncrement, onLongClick = onDecrement)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            // Invisible label always reserves the empty-state width so the chip's width
            // doesn't shrink when the value goes 0 → non-zero. Wider values (e.g., "10")
            // can still expand the chip, but it never goes below the label's width.
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.alpha(0f)
            )
            // Empty: show label centered (matches an empty OutlinedTextField).
            // Filled: show the value (the label moves to the floating overlay below).
            Text(
                text = if (isFilled) "$value" else label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isFilled) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        // Floating label that "notches" the top border when filled.
        // Background matches the dialog surface so it visually breaks the border line.
        if (isFilled) {
            Text(
                text = label,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 12.dp, y = (-7).dp)
                    .background(AlertDialogDefaults.containerColor)
                    .padding(horizontal = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SummaryCard(
    totalPar: Int,
    totalShots: Int,
    totalPenalties: Int,
    totalPutts: Int,
    totalScore: Int,
    totalAdjustment: Int,
    totalGir: Int,
    scoreToPar: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SummaryItem("Par", totalPar.toString())
            SummaryItem("Shots", totalShots.toString())
            SummaryItem("Pen", totalPenalties.toString())
            SummaryItem("Putts", totalPutts.toString())
            SummaryItem("Score", totalScore.toString())
            if (totalAdjustment > 0) {
                SummaryItem("Adj", (totalScore - totalAdjustment).toString())
            }
            SummaryItem("GIR", totalGir.toString())
            SummaryItem(
                "+/-",
                if (scoreToPar >= 0) "+$scoreToPar" else "$scoreToPar"
            )
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun ScorecardHeader(showAdj: Boolean, showHcp: Boolean) {
    val labels = buildList {
        add("Hole")
        if (showHcp) add("Hcp")
        add("Par"); add("Shots"); add("Pen"); add("Putts"); add("Score")
        if (showAdj) add("Adj")
        add("GIR"); add("+/-")
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        labels.forEach { label ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Visible
            )
        }
    }
}

@Composable
private fun HoleScoreRow(
    holeScore: HoleScore,
    showAdj: Boolean,
    showHcp: Boolean,
    holeHandicap: Int?,
    onClick: (() -> Unit)? = null
) {
    val scoreToPar = holeScore.scoreToPar
    val scoreColor = when {
        scoreToPar == null -> MaterialTheme.colorScheme.onSurface
        scoreToPar < 0 -> Color(0xFF4CAF50)
        scoreToPar == 0 -> MaterialTheme.colorScheme.onSurface
        else -> Color(0xFFF44336)
    }

    val rowModifier = Modifier
        .fillMaxWidth()
        .let { if (onClick != null) it.clickable(onClick = onClick) else it }
        .padding(horizontal = 16.dp, vertical = 12.dp)

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val shots = holeScore.score?.let { it - (holeScore.putts ?: 0) - (holeScore.penalties ?: 0) }
        Text(
            text = "${holeScore.holeNumber}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        if (showHcp) {
            Text(
                text = holeHandicap?.toString() ?: "-",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
        Text(
            text = "${holeScore.par}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Text(
            text = shots?.toString() ?: "-",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        val penaltyColor = if ((holeScore.penalties ?: 0) >= 1) Color(0xFFF44336) else MaterialTheme.colorScheme.onSurface
        Text(
            text = holeScore.penaltiesDisplay,
            style = MaterialTheme.typography.bodyLarge,
            color = penaltyColor,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        val puttsValue = if (holeScore.score != null) (holeScore.putts ?: 0) else holeScore.putts
        val puttsColor = when (puttsValue) {
            0, 1 -> Color(0xFF4CAF50)
            in 3..Int.MAX_VALUE -> Color(0xFFF44336)
            else -> MaterialTheme.colorScheme.onSurface
        }
        Text(
            text = puttsValue?.toString() ?: "-",
            style = MaterialTheme.typography.bodyLarge,
            color = puttsColor,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Text(
            text = holeScore.scoreDisplay,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = scoreColor,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        if (showAdj) {
            Text(
                text = holeScore.adjustedScore?.toString() ?: "-",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = scoreColor,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
        Text(
            text = if (holeScore.isGir == true) "✓" else "-",
            style = MaterialTheme.typography.bodyLarge,
            color = if (holeScore.isGir == true) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Text(
            text = scoreToPar?.let { if (it >= 0) "+$it" else "$it" } ?: "-",
            style = MaterialTheme.typography.bodyMedium,
            color = scoreColor,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SummaryRow(
    label: String,
    holes: List<HoleScore>,
    showAdj: Boolean,
    showHcp: Boolean
) {
    val totalPar = holes.sumOf { it.par }
    val totalShots = holes.sumOf { (it.score ?: 0) - (it.putts ?: 0) - (it.penalties ?: 0) }
    val totalPenalties = holes.sumOf { it.penalties ?: 0 }
    val totalPutts = holes.sumOf { it.putts ?: 0 }
    val totalScore = holes.sumOf { it.score ?: 0 }
    val totalAdjustedScore = holes.sumOf { it.adjustedScore ?: 0 }
    val totalGir = holes.count { it.isGir == true }
    val scored = holes.filter { it.score != null }
    val scoreToPar = scored.sumOf { (it.score ?: 0) - it.par }
    val scoreToParText = if (scored.isEmpty()) "-" else if (scoreToPar >= 0) "+$scoreToPar" else "$scoreToPar"
    val scoreColor = when {
        scored.isEmpty() -> MaterialTheme.colorScheme.onSurface
        scoreToPar < 0 -> Color(0xFF4CAF50)
        scoreToPar == 0 -> MaterialTheme.colorScheme.onSurface
        else -> Color(0xFFF44336)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Label cell stays one column wide (centered above "Hole"), but overflow=Visible +
        // softWrap=false lets "Front 9" / "Back 9" bleed into the adjacent Hcp column (which
        // is just an empty Spacer here) instead of wrapping.
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Visible
        )
        if (showHcp) {
            Spacer(modifier = Modifier.weight(1f))
        }
        Text(
            text = "$totalPar",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Text(
            text = "$totalShots",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Text(
            text = "$totalPenalties",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Text(
            text = "$totalPutts",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Text(
            text = "$totalScore",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = scoreColor,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        if (showAdj) {
            Text(
                text = "$totalAdjustedScore",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = scoreColor,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
        Text(
            text = "$totalGir",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Text(
            text = scoreToParText,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = scoreColor,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
    }
}
