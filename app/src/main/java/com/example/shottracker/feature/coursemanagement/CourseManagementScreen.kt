package com.example.shottracker.feature.coursemanagement

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shottracker.domain.model.Course
import com.example.shottracker.domain.model.HoleInfo
import com.example.shottracker.domain.model.Tee

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseManagementScreen(
    onSearchCourses: () -> Unit,
    onCreateCourse: () -> Unit,
    onBack: () -> Unit,
    viewModel: CourseManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Courses") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            Button(
                onClick = onSearchCourses,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Search & Import Courses")
            }
            OutlinedButton(
                onClick = onCreateCourse,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text("Create Course Manually")
            }

            if (uiState.courses.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No courses saved yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.courses, key = { it.id }) { course ->
                        CourseItem(
                            course = course,
                            onClick = { viewModel.selectCourse(course) },
                            onDelete = { viewModel.confirmDelete(course) }
                        )
                    }
                }
            }
        }
    }

    uiState.courseToDelete?.let { course ->
        AlertDialog(
            onDismissRequest = viewModel::dismissDelete,
            title = { Text("Delete Course?") },
            text = { Text("Are you sure you want to delete ${course.name}? This cannot be undone.") },
            confirmButton = {
                Button(onClick = viewModel::deleteCourse) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDelete) {
                    Text("Cancel")
                }
            }
        )
    }

    uiState.editingCourse?.let { course ->
        EditCourseDialog(
            course = course,
            holes = uiState.editingHoles,
            tees = uiState.editingTees,
            isSaving = uiState.isSaving,
            onParChanged = viewModel::updateHolePar,
            onAddTee = viewModel::openNewTeeForm,
            onEditTee = viewModel::openEditTeeForm,
            onDeleteTee = viewModel::deleteTee,
            onSetHandicaps = viewModel::openHandicapEditor,
            onSave = viewModel::saveHoleEdits,
            onCancel = viewModel::closeCourseEditor
        )
    }

    if (uiState.editingHandicapsTeeId != null) {
        EditHoleHandicapsDialog(
            holes = uiState.editingHoles,
            edits = uiState.handicapEdits,
            error = uiState.handicapError,
            onChange = viewModel::updateHandicapEntry,
            onClearAll = viewModel::clearAllHandicapEdits,
            onCancel = viewModel::cancelHandicapEditor,
            onSave = viewModel::saveHandicapEdits,
        )
    }

    uiState.teeForm?.let { form ->
        EditTeeDialog(
            form = form,
            onNameChange = viewModel::updateTeeFormName,
            onColorChange = viewModel::updateTeeFormColor,
            onRatingChange = viewModel::updateTeeFormRating,
            onSlopeChange = viewModel::updateTeeFormSlope,
            onDistanceChange = viewModel::updateTeeFormDistance,
            onSave = viewModel::saveTeeForm,
            onCancel = viewModel::cancelTeeForm
        )
    }
}

@Composable
private fun CourseItem(
    course: Course,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = course.name,
                    style = MaterialTheme.typography.titleMedium
                )
                if (course.holes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${course.holes.size} holes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete ${course.name}",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun EditCourseDialog(
    course: Course,
    holes: List<HoleInfo>,
    tees: List<Tee>,
    isSaving: Boolean,
    onParChanged: (Int, Int) -> Unit,
    onAddTee: () -> Unit,
    onEditTee: (Tee) -> Unit,
    onDeleteTee: (Long) -> Unit,
    onSetHandicaps: (Long) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val totalPar = holes.sumOf { it.par }

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Edit · ${course.name}") },
        text = {
            Column {
                // Tees section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tees (${tees.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onAddTee) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Tee")
                    }
                }
                if (tees.isEmpty()) {
                    Text(
                        text = "No tees added yet. Tap \"Add Tee\" to enter rating and slope.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                } else {
                    Column {
                        tees.forEach { tee ->
                            TeeRow(
                                tee = tee,
                                onEdit = { onEditTee(tee) },
                                onDelete = { onDeleteTee(tee.id) },
                                onSetHandicaps = { onSetHandicaps(tee.id) }
                            )
                            HorizontalDivider()
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pars section
                Text(
                    text = "Total Par: $totalPar",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                LazyColumn(
                    modifier = Modifier.heightIn(max = 320.dp)
                ) {
                    items(holes, key = { it.holeNumber }) { hole ->
                        HoleParRow(
                            hole = hole,
                            onParChanged = { newPar -> onParChanged(hole.holeNumber, newPar) }
                        )
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = !isSaving
            ) {
                Text(if (isSaving) "Saving..." else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel, enabled = !isSaving) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun TeeRow(
    tee: Tee,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSetHandicaps: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tee.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            val rating = tee.rating?.let { String.format("%.1f", it) } ?: "—"
            val slope = tee.slope?.toString() ?: "—"
            val distance = tee.totalDistance?.let { " · $it yds" } ?: ""
            Text(
                text = "Rating $rating · Slope $slope$distance",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        TextButton(onClick = onSetHandicaps) {
            Text("Handicaps")
        }
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete ${tee.name}",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun EditHoleHandicapsDialog(
    holes: List<HoleInfo>,
    edits: Map<Int, String>,
    error: String?,
    onChange: (Int, String) -> Unit,
    onClearAll: () -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit,
) {
    val holeCount = holes.size
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Set Hole Handicaps") },
        text = {
            Column {
                Text(
                    text = "Assign a stroke index 1..$holeCount to each hole. Each value must be unique.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp)
                ) {
                    items(holes, key = { it.holeNumber }) { hole ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "Hole ${hole.holeNumber} (Par ${hole.par})",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            OutlinedTextField(
                                value = edits[hole.holeNumber].orEmpty(),
                                onValueChange = { onChange(hole.holeNumber, it) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.width(72.dp),
                                textStyle = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                if (error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onClearAll) { Text("Clear All") }
            }
        },
        confirmButton = { Button(onClick = onSave) { Text("Save") } },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancel") } }
    )
}

@Composable
private fun EditTeeDialog(
    form: TeeFormState,
    onNameChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onRatingChange: (String) -> Unit,
    onSlopeChange: (String) -> Unit,
    onDistanceChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(if (form.teeId == 0L) "Add Tee" else "Edit Tee") },
        text = {
            Column {
                OutlinedTextField(
                    value = form.name,
                    onValueChange = onNameChange,
                    label = { Text("Name (e.g., Blue, White)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = form.color,
                    onValueChange = onColorChange,
                    label = { Text("Color (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = form.rating,
                        onValueChange = onRatingChange,
                        label = { Text("Rating") },
                        placeholder = { Text("71.5") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = form.slope,
                        onValueChange = onSlopeChange,
                        label = { Text("Slope") },
                        placeholder = { Text("132") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = form.distance,
                    onValueChange = onDistanceChange,
                    label = { Text("Total Distance (yds, optional)") },
                    placeholder = { Text("6500") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                form.error?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onSave) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text("Cancel") }
        }
    )
}

@Composable
private fun HoleParRow(
    hole: HoleInfo,
    onParChanged: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Hole ${hole.holeNumber}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = { onParChanged(hole.par - 1) },
            enabled = hole.par > 3
        ) {
            Icon(Icons.Default.Remove, contentDescription = "Decrease par")
        }
        Text(
            text = "Par ${hole.par}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(64.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        IconButton(
            onClick = { onParChanged(hole.par + 1) },
            enabled = hole.par < 6
        ) {
            Icon(Icons.Default.Add, contentDescription = "Increase par")
        }
    }
}
