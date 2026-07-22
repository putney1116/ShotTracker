package com.example.shottracker.feature.createcourse

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shottracker.feature.createcourse.steps.GreensStep
import com.example.shottracker.feature.createcourse.steps.HoleCountStep
import com.example.shottracker.feature.createcourse.steps.NameStep
import com.example.shottracker.feature.createcourse.steps.ParsStep
import com.example.shottracker.feature.createcourse.steps.ReviewStep
import com.example.shottracker.feature.createcourse.steps.TeesStep

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCourseScreen(
    onBack: () -> Unit,
    onCourseCreated: () -> Unit,
    viewModel: CreateCourseViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                CreateCourseEvent.Saved -> onCourseCreated()
            }
        }
    }

    LaunchedEffect(state.saveError) {
        state.saveError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSaveError()
        }
    }

    BackHandler { viewModel.onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titleForStep(state)) },
                navigationIcon = {
                    IconButton(onClick = viewModel::onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (state.step) {
                WizardStep.Name -> NameStep(
                    state = state,
                    onNameChange = viewModel::setName,
                    onCityChange = viewModel::setCity,
                    onStateChange = viewModel::setState,
                    onNext = viewModel::onNext,
                )
                WizardStep.HoleCount -> HoleCountStep(
                    state = state,
                    onSelect = viewModel::setHoleCount,
                    onNext = viewModel::onNext,
                )
                WizardStep.Pars -> ParsStep(
                    state = state,
                    onParChanged = viewModel::updateHolePar,
                    onNext = viewModel::onNext,
                )
                WizardStep.Greens -> GreensStep(
                    state = state,
                    onPlace = viewModel::placeGreen,
                    onSelectTarget = viewModel::selectTarget,
                    onNext = viewModel::onNext,
                    onBack = viewModel::onBack,
                )
                WizardStep.Tees -> TeesStep(
                    state = state,
                    onOpenNewTeeForm = viewModel::openNewTeeForm,
                    onCancelTeeForm = viewModel::cancelTeeForm,
                    onTeeFormName = viewModel::updateTeeFormName,
                    onTeeFormColor = viewModel::updateTeeFormColor,
                    onTeeFormRating = viewModel::updateTeeFormRating,
                    onTeeFormSlope = viewModel::updateTeeFormSlope,
                    onTeeFormDistance = viewModel::updateTeeFormDistance,
                    onSaveTeeForm = viewModel::saveTeeForm,
                    onDeleteTeeDraft = viewModel::deleteTeeDraft,
                    onNext = viewModel::onNext,
                )
                WizardStep.Review -> ReviewStep(
                    state = state,
                    onSave = viewModel::onSave,
                )
            }
        }
    }

    if (state.showDiscardDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDiscardDialog,
            title = { Text("Discard course?") },
            text = { Text("You'll lose your progress.") },
            confirmButton = {
                Button(onClick = onBack) { Text("Discard") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDiscardDialog) { Text("Cancel") }
            },
        )
    }
}

private fun titleForStep(state: CreateCourseUiState): String = when (state.step) {
    WizardStep.Name -> "New Course — Name"
    WizardStep.HoleCount -> "New Course — Holes"
    WizardStep.Pars -> "New Course — Pars"
    WizardStep.Greens -> "Hole ${state.currentGreenHole} of ${state.holeCount} — Place ${state.selectedGreenTarget}"
    WizardStep.Tees -> "New Course — Tees"
    WizardStep.Review -> "New Course — Review"
}
