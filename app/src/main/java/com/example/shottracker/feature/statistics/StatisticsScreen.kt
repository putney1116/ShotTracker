package com.example.shottracker.feature.statistics

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onBack: () -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Filters
            item {
                FiltersCard(
                    courses = uiState.availableCourses,
                    selectedCourse = uiState.selectedCourse,
                    startDate = uiState.startDate,
                    endDate = uiState.endDate,
                    onCourseSelected = viewModel::setSelectedCourse,
                    onStartDateChanged = viewModel::setStartDate,
                    onEndDateChanged = viewModel::setEndDate,
                    onClearFilters = viewModel::clearFilters
                )
            }

            if (uiState.totalRounds == 0 && !uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No completed rounds match these filters",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                // Overview
                item {
                    StatisticsCard(title = "Overview") {
                        StatRow("Total Rounds", "${uiState.totalRounds}")
                        uiState.averageScore?.let {
                            StatRow("Average Score", String.format("%.1f", it))
                        }
                        uiState.bestScore?.let {
                            StatRow("Best Score", "$it")
                        }
                    }
                }

                // Per Round Averages
                item {
                    StatisticsCard(title = "Per Round Averages") {
                        uiState.averageShotsPerRound?.let {
                            StatRow("Shots", String.format("%.1f", it))
                        }
                        uiState.averagePuttsPerRound?.let {
                            StatRow("Putts", String.format("%.1f", it))
                        }
                        uiState.averageGirPerRound?.let {
                            StatRow("GIR", String.format("%.1f", it))
                        }
                        uiState.averagePenaltiesPerRound?.let {
                            StatRow("Penalties", String.format("%.1f", it))
                        }
                    }
                }

                // Per Hole Averages
                item {
                    StatisticsCard(title = "Per Hole Averages") {
                        uiState.averagePuttsPerHole?.let {
                            StatRow("Putts", String.format("%.2f", it))
                        }
                        uiState.averageGirPerHole?.let {
                            StatRow("GIR", String.format("%.0f%%", it * 100))
                        }
                        uiState.averagePenaltiesPerHole?.let {
                            StatRow("Penalties", String.format("%.2f", it))
                        }
                    }
                }

                // Scoring by par (rows expand to show per-par per-hole averages)
                item {
                    var expandedPars by remember { mutableStateOf(setOf<Int>()) }
                    StatisticsCard(title = "Scoring by Par") {
                        ParStatRow(
                            label = "Par 3 Average",
                            stats = uiState.par3Stats,
                            expanded = 3 in expandedPars,
                            onClick = {
                                expandedPars = if (3 in expandedPars) expandedPars - 3 else expandedPars + 3
                            }
                        )
                        ParStatRow(
                            label = "Par 4 Average",
                            stats = uiState.par4Stats,
                            expanded = 4 in expandedPars,
                            onClick = {
                                expandedPars = if (4 in expandedPars) expandedPars - 4 else expandedPars + 4
                            }
                        )
                        ParStatRow(
                            label = "Par 5 Average",
                            stats = uiState.par5Stats,
                            expanded = 5 in expandedPars,
                            onClick = {
                                expandedPars = if (5 in expandedPars) expandedPars - 5 else expandedPars + 5
                            }
                        )
                    }
                }
            }

            // Club distances (always shown, all-time)
            if (uiState.clubStatistics.isNotEmpty()) {
                item {
                    Text(
                        text = "Club Distances (all-time)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(uiState.clubStatistics) { clubStat ->
                    ClubStatCard(clubStat)
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FiltersCard(
    courses: List<String>,
    selectedCourse: String?,
    startDate: LocalDate?,
    endDate: LocalDate?,
    onCourseSelected: (String?) -> Unit,
    onStartDateChanged: (LocalDate?) -> Unit,
    onEndDateChanged: (LocalDate?) -> Unit,
    onClearFilters: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filters",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (selectedCourse != null || startDate != null || endDate != null) {
                    TextButton(onClick = onClearFilters) {
                        Icon(Icons.Default.Clear, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear")
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Course dropdown
            CourseDropdown(
                courses = courses,
                selectedCourse = selectedCourse,
                onCourseSelected = onCourseSelected
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Date range
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DatePickerField(
                    label = "Start Date",
                    date = startDate,
                    onDateChanged = onStartDateChanged,
                    modifier = Modifier.weight(1f)
                )
                DatePickerField(
                    label = "End Date",
                    date = endDate,
                    onDateChanged = onEndDateChanged,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseDropdown(
    courses: List<String>,
    selectedCourse: String?,
    onCourseSelected: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedCourse ?: "All Courses",
            onValueChange = {},
            readOnly = true,
            label = { Text("Course") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("All Courses") },
                onClick = {
                    onCourseSelected(null)
                    expanded = false
                }
            )
            courses.forEach { course ->
                DropdownMenuItem(
                    text = { Text(course) },
                    onClick = {
                        onCourseSelected(course)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    label: String,
    date: LocalDate?,
    onDateChanged: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }
    val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

    OutlinedButton(
        onClick = { showPicker = true },
        modifier = modifier
    ) {
        Text(
            text = date?.format(formatter) ?: label,
            style = MaterialTheme.typography.bodyMedium
        )
    }

    if (showPicker) {
        val initialMillis = date
            ?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
            ?: Instant.now().toEpochMilli()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                Button(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val picked = Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC).toLocalDate()
                        onDateChanged(picked)
                    }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                Row {
                    if (date != null) {
                        TextButton(onClick = {
                            onDateChanged(null)
                            showPicker = false
                        }) { Text("Clear") }
                    }
                    TextButton(onClick = { showPicker = false }) { Text("Cancel") }
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun StatisticsCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun ParStatRow(
    label: String,
    stats: ParStats,
    expanded: Boolean,
    onClick: () -> Unit
) {
    val avgScoreText = stats.averageScore?.let { String.format("%.2f", it) } ?: "-"
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = stats.holeCount > 0, onClick = onClick)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = avgScoreText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                if (stats.holeCount > 0) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (expanded)
                            Icons.Default.KeyboardArrowUp
                        else
                            Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        if (expanded && stats.holeCount > 0) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp, bottom = 4.dp)
            ) {
                StatRow("Holes", "${stats.holeCount}")
                stats.puttsPerHole?.let {
                    StatRow("Putts/Hole", String.format("%.2f", it))
                }
                stats.girPerHole?.let {
                    StatRow("GIR", String.format("%.0f%%", it * 100))
                }
                stats.penaltiesPerHole?.let {
                    StatRow("Penalties/Hole", String.format("%.2f", it))
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ClubStatCard(clubStat: ClubStatistics) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = clubStat.club.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${clubStat.shotCount} shots",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            clubStat.averageDistance?.let { distance ->
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$distance",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "yards",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

