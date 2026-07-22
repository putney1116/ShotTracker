package com.example.shottracker.feature.createcourse.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shottracker.feature.createcourse.CreateCourseUiState
import com.example.shottracker.feature.createcourse.GreenTarget
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun GreensStep(
    state: CreateCourseUiState,
    onPlace: (Double, Double) -> Unit,
    onSelectTarget: (GreenTarget) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 2f)
    }
    val mapProperties = MapProperties(mapType = MapType.SATELLITE)

    val hole = state.greens[state.currentGreenHole - 1]
    val isComplete = hole.isComplete

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            onMapClick = { latLng -> onPlace(latLng.latitude, latLng.longitude) },
        ) {
            hole.frontLat?.let { lat ->
                hole.frontLng?.let { lng ->
                    Marker(
                        state = MarkerState(LatLng(lat, lng)),
                        title = "Front",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
                    )
                }
            }
            hole.centerLat?.let { lat ->
                hole.centerLng?.let { lng ->
                    Marker(
                        state = MarkerState(LatLng(lat, lng)),
                        title = "Center",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW),
                    )
                }
            }
            hole.backLat?.let { lat ->
                hole.backLng?.let { lng ->
                    Marker(
                        state = MarkerState(LatLng(lat, lng)),
                        title = "Back",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
                    )
                }
            }
        }

        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            tonalElevation = 4.dp,
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    TargetChip(state, GreenTarget.Front, hole.frontLat != null, onSelectTarget)
                    TargetChip(state, GreenTarget.Center, hole.centerLat != null, onSelectTarget)
                    TargetChip(state, GreenTarget.Back, hole.backLat != null, onSelectTarget)
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.weight(1f),
                    ) { Text(if (state.currentGreenHole == 1) "Back to Pars" else "Prev Hole") }
                    Button(
                        onClick = onNext,
                        enabled = isComplete,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(if (state.currentGreenHole == state.holeCount) "To Tees" else "Next Hole")
                    }
                }
            }
        }
    }
}

@Composable
private fun TargetChip(
    state: CreateCourseUiState,
    target: GreenTarget,
    placed: Boolean,
    onSelectTarget: (GreenTarget) -> Unit,
) {
    FilterChip(
        selected = state.selectedGreenTarget == target,
        onClick = { onSelectTarget(target) },
        label = { Text(if (placed) "${target.name} ✓" else target.name) },
    )
}
