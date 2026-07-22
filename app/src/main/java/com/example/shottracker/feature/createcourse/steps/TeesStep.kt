package com.example.shottracker.feature.createcourse.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.shottracker.feature.coursemanagement.TeeFormState
import com.example.shottracker.feature.createcourse.CreateCourseUiState

@Composable
fun TeesStep(
    state: CreateCourseUiState,
    onOpenNewTeeForm: () -> Unit,
    onCancelTeeForm: () -> Unit,
    onTeeFormName: (String) -> Unit,
    onTeeFormColor: (String) -> Unit,
    onTeeFormRating: (String) -> Unit,
    onTeeFormSlope: (String) -> Unit,
    onTeeFormDistance: (String) -> Unit,
    onSaveTeeForm: () -> Unit,
    onDeleteTeeDraft: (String) -> Unit,
    onNext: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Tees (${state.tees.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            TextButton(onClick = onOpenNewTeeForm) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Tee")
            }
        }
        HorizontalDivider()
        if (state.tees.isEmpty()) {
            Text(
                text = "Add at least one tee with rating and slope.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp),
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f),
            ) {
                items(state.tees, key = { it.name }) { tee ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(tee.name, fontWeight = FontWeight.Bold)
                                val rating = tee.rating?.let { String.format("%.1f", it) } ?: "—"
                                val slope = tee.slope?.toString() ?: "—"
                                Text("Rating $rating · Slope $slope",
                                     style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = { onDeleteTeeDraft(tee.name) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete ${tee.name}",
                                     tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }

        state.teesError?.let { err ->
            Text(
                text = err,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        Button(
            onClick = onNext,
            enabled = state.tees.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        ) { Text("Next") }
    }

    state.teeForm?.let { form ->
        TeeFormDialog(
            form = form,
            onNameChange = onTeeFormName,
            onColorChange = onTeeFormColor,
            onRatingChange = onTeeFormRating,
            onSlopeChange = onTeeFormSlope,
            onDistanceChange = onTeeFormDistance,
            onSave = onSaveTeeForm,
            onCancel = onCancelTeeForm,
        )
    }
}

@Composable
private fun TeeFormDialog(
    form: TeeFormState,
    onNameChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onRatingChange: (String) -> Unit,
    onSlopeChange: (String) -> Unit,
    onDistanceChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Add Tee") },
        text = {
            Column {
                OutlinedTextField(
                    value = form.name,
                    onValueChange = onNameChange,
                    label = { Text("Name (e.g., Blue, White)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = form.color,
                    onValueChange = onColorChange,
                    label = { Text("Color (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = form.rating,
                        onValueChange = onRatingChange,
                        label = { Text("Rating") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = form.slope,
                        onValueChange = onSlopeChange,
                        label = { Text("Slope") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = form.distance,
                    onValueChange = onDistanceChange,
                    label = { Text("Total Distance (yds, optional)") },
                    placeholder = { Text("6500") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                form.error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error,
                         style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = { Button(onClick = onSave) { Text("Save") } },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancel") } },
    )
}
