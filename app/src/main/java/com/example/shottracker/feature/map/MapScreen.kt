package com.example.shottracker.feature.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import android.location.Location
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlin.math.cos
import kotlin.math.ln
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    roundId: Long,
    holeNumber: Int,
    onBack: () -> Unit,
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val cameraPositionState = rememberCameraPositionState()
    // 0 = not centered, 1 = fallback (single point), 2 = optimal (both points with bearing)
    var centeringLevel by remember { mutableIntStateOf(0) }

    LaunchedEffect(uiState.currentLocation, uiState.holeInfo) {
        if (centeringLevel >= 2) return@LaunchedEffect

        val location = uiState.currentLocation
        val greenLat = uiState.holeInfo?.greenCenterLat
        val greenLng = uiState.holeInfo?.greenCenterLng

        // Best case: both user location and green available
        // Orient so user is at bottom, green is at top
        if (location != null && greenLat != null && greenLng != null) {
            val userLoc = Location("").apply {
                latitude = location.latitude
                longitude = location.longitude
            }
            val greenLoc = Location("").apply {
                latitude = greenLat
                longitude = greenLng
            }
            val bearing = (userLoc.bearingTo(greenLoc) + 360f) % 360f
            val distance = userLoc.distanceTo(greenLoc)

            val midLat = (location.latitude + greenLat) / 2.0
            val midLng = (location.longitude + greenLng) / 2.0

            // Calculate zoom level to fit both points vertically with padding
            // Ground resolution = 156543.03392 * cos(lat) / 2^zoom  (meters/pixel)
            val metersPerPixel = distance / 500.0
            val zoom = if (metersPerPixel > 0) {
                ((ln(156543.03392 * cos(Math.toRadians(midLat)) / metersPerPixel) / ln(2.0)) - 2.0)
                    .toFloat().coerceIn(12f, 18f)
            } else 16f

            cameraPositionState.position = CameraPosition(
                LatLng(midLat, midLng), zoom, 0f, bearing
            )
            centeringLevel = 2
            return@LaunchedEffect
        }

        // Fallback: only apply once while waiting for both pieces of data
        if (centeringLevel >= 1) return@LaunchedEffect

        if (greenLat != null && greenLng != null) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(greenLat, greenLng), 18f
            )
            centeringLevel = 1
        } else if (location != null) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(location.latitude, location.longitude), 17f
            )
            centeringLevel = 1
        }
    }

    val hasPermission = uiState.currentLocation != null
    val mapProperties = remember(hasPermission) {
        MapProperties(
            mapType = MapType.SATELLITE,
            isMyLocationEnabled = hasPermission
        )
    }

    val mapUiSettings = remember {
        MapUiSettings(
            myLocationButtonEnabled = true,
            zoomControlsEnabled = true
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hole $holeNumber Map") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                uiSettings = mapUiSettings
            ) {
                // Green markers (if hole info has coordinates)
                uiState.holeInfo?.let { hole ->
                    hole.greenFrontLat?.let { lat ->
                        hole.greenFrontLng?.let { lng ->
                            Marker(
                                state = MarkerState(position = LatLng(lat, lng)),
                                title = "Front of Green",
                                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                            )
                        }
                    }

                    hole.greenCenterLat?.let { lat ->
                        hole.greenCenterLng?.let { lng ->
                            Marker(
                                state = MarkerState(position = LatLng(lat, lng)),
                                title = "Center of Green",
                                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)
                            )
                        }
                    }

                    hole.greenBackLat?.let { lat ->
                        hole.greenBackLng?.let { lng ->
                            Marker(
                                state = MarkerState(position = LatLng(lat, lng)),
                                title = "Back of Green",
                                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                            )
                        }
                    }
                }

                // Shot markers
                uiState.shots.forEachIndexed { index, shot ->
                    Marker(
                        state = MarkerState(position = LatLng(shot.latitude, shot.longitude)),
                        title = "Shot ${shot.shotNumber}",
                        snippet = shot.clubName ?: "Unknown club",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                    )
                }
            }
        }
    }
}
