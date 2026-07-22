package com.example.shottracker.feature.round

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewRoundSetupScreen(
    onRoundStarted: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: NewRoundSetupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // Re-check DND access when returning to this screen (e.g. back from settings).
    LifecycleResumeEffect(Unit) {
        viewModel.refreshDndPermission()
        onPauseOrDispose {}
    }

    if (uiState.needsDndPermission) {
        val context = LocalContext.current
        AlertDialog(
            onDismissRequest = viewModel::dismissDndPermissionPrompt,
            title = { Text("Allow Do Not Disturb access") },
            text = {
                Text(
                    "To silence notifications during your round, ShotTracker needs Do Not " +
                        "Disturb access. Open settings to grant it."
                )
            },
            confirmButton = {
                Button(onClick = {
                    context.startActivity(viewModel.dndSettingsIntent())
                    viewModel.dismissDndPermissionPrompt()
                }) { Text("Open settings") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDndPermissionPrompt) { Text("Not now") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Round") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Course selection or creation
            Text(
                text = "Select Course",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            run {
                var courseExpanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = courseExpanded,
                    onExpandedChange = { courseExpanded = it }
                ) {
                    OutlinedTextField(
                        value = uiState.selectedCourse?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Course") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = courseExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )

                    ExposedDropdownMenu(
                        expanded = courseExpanded,
                        onDismissRequest = { courseExpanded = false }
                    ) {
                        uiState.courses.forEach { course ->
                            DropdownMenuItem(
                                text = { Text(course.name) },
                                onClick = {
                                    viewModel.onCourseSelected(course)
                                    courseExpanded = false
                                }
                            )
                        }
                    }
                }

                // Tee selection if course is selected
                uiState.selectedCourse?.let {
                    if (uiState.tees.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Select Tees",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        var teeExpanded by remember { mutableStateOf(false) }

                        ExposedDropdownMenuBox(
                            expanded = teeExpanded,
                            onExpandedChange = { teeExpanded = it }
                        ) {
                            val selectedLabel = uiState.selectedTee?.let { tee ->
                                val r = tee.rating?.let { String.format("%.1f", it) } ?: "—"
                                val s = tee.slope?.toString() ?: "—"
                                val distSuffix = tee.totalDistance?.let { " · $it yds" } ?: ""
                                "${tee.name} · $r / $s$distSuffix"
                            } ?: "Select tees"
                            OutlinedTextField(
                                value = selectedLabel,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = teeExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            )

                            ExposedDropdownMenu(
                                expanded = teeExpanded,
                                onDismissRequest = { teeExpanded = false }
                            ) {
                                uiState.tees.forEach { tee ->
                                    DropdownMenuItem(
                                        text = {
                                            val r = tee.rating?.let { String.format("%.1f", it) } ?: "—"
                                            val s = tee.slope?.toString() ?: "—"
                                            val distSuffix = tee.totalDistance?.let { " · $it yds" } ?: ""
                                            Text("${tee.name} · $r / $s$distSuffix")
                                        },
                                        onClick = {
                                            viewModel.onTeeSelected(tee)
                                            teeExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Auto Do-Not-Disturb opt-in (remembered between rounds).
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Enable Do Not Disturb?",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = uiState.silenceDuringRound,
                    onCheckedChange = viewModel::onSilenceDuringRoundChanged
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.startRound(onRoundStarted) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && (uiState.isCreatingCourse && uiState.courseName.isNotBlank() || uiState.selectedCourse != null)
            ) {
                Text(if (uiState.isLoading) "Starting..." else "Start Round")
            }
        }
    }
}
