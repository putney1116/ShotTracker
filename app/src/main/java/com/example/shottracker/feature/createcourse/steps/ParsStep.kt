package com.example.shottracker.feature.createcourse.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.shottracker.feature.createcourse.CreateCourseUiState

@Composable
fun ParsStep(
    state: CreateCourseUiState,
    onParChanged: (Int, Int) -> Unit,
    onNext: () -> Unit,
) {
    val totalPar = state.pars.sum()
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Total Par: $totalPar",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp),
        )
        HorizontalDivider()
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp),
        ) {
            items(count = state.holeCount, key = { it }) { idx ->
                ParRow(holeNumber = idx + 1, par = state.pars[idx],
                       onParChanged = { newPar -> onParChanged(idx + 1, newPar) })
                HorizontalDivider()
            }
        }
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        ) { Text("Next") }
    }
}

@Composable
private fun ParRow(
    holeNumber: Int,
    par: Int,
    onParChanged: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Hole $holeNumber", style = MaterialTheme.typography.bodyLarge,
             modifier = Modifier.weight(1f))
        IconButton(onClick = { onParChanged(par - 1) }, enabled = par > 3) {
            Icon(Icons.Default.Remove, contentDescription = "Decrease par")
        }
        Text(
            text = "Par $par",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(64.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        IconButton(onClick = { onParChanged(par + 1) }, enabled = par < 6) {
            Icon(Icons.Default.Add, contentDescription = "Increase par")
        }
    }
}
