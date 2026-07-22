package com.example.shottracker.feature.round

import android.Manifest
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
import android.graphics.RectF
import android.graphics.Typeface
import android.location.Location
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GolfCourse
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.SportsGolf
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.outlined.StickyNote2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.Projection
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.example.shottracker.core.util.visibleMidpointT
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.roundToInt

private val overlayBackground = Color.Black.copy(alpha = 0.65f)
private val overlayText = Color.White
private val overlayTextDim = Color.White.copy(alpha = 0.7f)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ActiveRoundScreen(
    roundId: Long,
    onOpenScorecard: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onOpenMap: (Int) -> Unit,
    onRoundComplete: () -> Unit,
    viewModel: ActiveRoundViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Hardware back handling for note UI
    BackHandler(enabled = uiState.showNoteEditor || uiState.showNoteQuickView) {
        when {
            uiState.showDiscardNoteDialog -> viewModel.dismissDiscardNoteDialog()
            uiState.showNoteEditor -> viewModel.requestCloseEditor()
            uiState.showNoteQuickView -> viewModel.dismissNoteQuickView()
        }
    }

    // Refresh hole data when returning from scorecard
    LifecycleResumeEffect(Unit) {
        viewModel.refreshCurrentHole()
        onPauseOrDispose {}
    }

    // Location permission handling
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        if (fineLocationGranted) {
            viewModel.onLocationPermissionGranted()
        }
    }

    LaunchedEffect(uiState.hasLocationPermission) {
        if (!uiState.hasLocationPermission) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            viewModel.startLocationUpdates()
        }
    }

    // Map camera positioning
    val cameraPositionState = rememberCameraPositionState()
    var centeringLevel by remember { mutableIntStateOf(0) }
    var lastCenteredHole by remember { mutableIntStateOf(-1) }
    // Pixel size of the map surface; needed to clip distance labels to the
    // on-screen viewport (which is a rotated rectangle once a bearing is applied).
    var mapSizePx by remember { mutableStateOf(IntSize.Zero) }
    // Heights of the top and bottom overlays (px). Distance labels are kept out of
    // these bands so they aren't hidden behind the overlays that float over the map.
    var topOverlayHeightPx by remember { mutableIntStateOf(0) }
    var bottomOverlayHeightPx by remember { mutableIntStateOf(0) }
    val labelMarginPx = with(LocalDensity.current) { 28.dp.toPx() }

    LaunchedEffect(uiState.currentLocation, uiState.currentHoleInfo, uiState.currentHoleNumber, centeringLevel) {
        // Reset centering when hole changes
        if (uiState.currentHoleNumber != lastCenteredHole) {
            centeringLevel = 0
        }
        if (centeringLevel >= 2) return@LaunchedEffect

        val location = uiState.currentLocation
        val greenLat = uiState.currentHoleInfo?.greenCenterLat
        val greenLng = uiState.currentHoleInfo?.greenCenterLng

        // Best case: both user location and green available — bearing-oriented view
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

            // Offset center 55% toward green to account for top overlay being taller
            val midLat = location.latitude + (greenLat - location.latitude) * 0.55
            val midLng = location.longitude + (greenLng - location.longitude) * 0.55

            val metersPerPixel = distance / 450.0
            val zoom = if (metersPerPixel > 0) {
                (ln(156543.03392 * cos(Math.toRadians(midLat)) / metersPerPixel) / ln(2.0))
                    .toFloat().coerceIn(14f, 20f)
            } else 17f

            cameraPositionState.position = CameraPosition(
                LatLng(midLat, midLng), zoom, 0f, bearing
            )
            centeringLevel = 2
            lastCenteredHole = uiState.currentHoleNumber
            return@LaunchedEffect
        }

        // Fallback: only apply once while waiting for both pieces of data
        if (centeringLevel >= 1) return@LaunchedEffect

        if (greenLat != null && greenLng != null) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(greenLat, greenLng), 18f
            )
            centeringLevel = 1
            lastCenteredHole = uiState.currentHoleNumber
        } else if (location != null) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(location.latitude, location.longitude), 17f
            )
            centeringLevel = 1
            lastCenteredHole = uiState.currentHoleNumber
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
            myLocationButtonEnabled = false,
            zoomControlsEnabled = false,
            mapToolbarEnabled = false
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Layer 1: Full-screen satellite map
        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { mapSizePx = it },
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = mapUiSettings,
            onMapClick = { viewModel.onMapTapped(it) }
        ) {
            // Green markers
            uiState.currentHoleInfo?.let { hole ->
                val dotIcon = remember { BitmapDescriptorFactory.fromBitmap(createGreenDotBitmap()) }
                val flagIcon = remember { BitmapDescriptorFactory.fromBitmap(createGreenFlagBitmap()) }

                hole.greenFrontLat?.let { lat ->
                    hole.greenFrontLng?.let { lng ->
                        val pos = LatLng(lat, lng)
                        Marker(
                            state = MarkerState(position = pos),
                            icon = dotIcon,
                            anchor = androidx.compose.ui.geometry.Offset(0.5f, 0.5f),
                            onClick = { viewModel.onMapTapped(pos); true }
                        )
                    }
                }
                hole.greenCenterLat?.let { lat ->
                    hole.greenCenterLng?.let { lng ->
                        val pos = LatLng(lat, lng)
                        Marker(
                            state = MarkerState(position = pos),
                            icon = flagIcon,
                            anchor = androidx.compose.ui.geometry.Offset(0.5f, 1.0f),
                            onClick = { viewModel.onMapTapped(pos); true }
                        )
                    }
                }
                hole.greenBackLat?.let { lat ->
                    hole.greenBackLng?.let { lng ->
                        val pos = LatLng(lat, lng)
                        Marker(
                            state = MarkerState(position = pos),
                            icon = dotIcon,
                            anchor = androidx.compose.ui.geometry.Offset(0.5f, 0.5f),
                            onClick = { viewModel.onMapTapped(pos); true }
                        )
                    }
                }
            }

            // Shot markers
            uiState.shots.forEach { shot ->
                Marker(
                    state = MarkerState(position = LatLng(shot.latitude, shot.longitude)),
                    title = "Shot ${shot.shotNumber}",
                    snippet = shot.clubName ?: "Unknown club",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                )
            }

            // Target line from tap
            uiState.tappedLocation?.let { tapPoint ->
                // Read camera position to trigger recomposition when camera moves
                @Suppress("UNUSED_VARIABLE")
                val cameraPos = cameraPositionState.position
                val projection = cameraPositionState.projection

                // Target marker at tapped location
                val targetIcon = remember {
                    BitmapDescriptorFactory.fromBitmap(createTargetBitmap())
                }
                val targetMarkerState = rememberMarkerState(position = tapPoint)
                targetMarkerState.position = tapPoint
                Marker(
                    state = targetMarkerState,
                    icon = targetIcon,
                    anchor = androidx.compose.ui.geometry.Offset(0.5f, 0.5f),
                    flat = true,
                    onClick = { viewModel.clearTappedLocation(); true }
                )

                // Line from player to tapped point
                uiState.currentLocation?.let { loc ->
                    val playerPos = LatLng(loc.latitude, loc.longitude)
                    Polyline(
                        points = listOf(playerPos, tapPoint),
                        color = Color.White,
                        width = 8f
                    )
                    // Distance label at midpoint, slides along line to stay on screen
                    uiState.distanceToTap?.let { dist ->
                        val labelPos = visibleMidpointOnLine(
                            playerPos, tapPoint, projection, mapSizePx,
                            topInsetPx = topOverlayHeightPx + labelMarginPx,
                            bottomInsetPx = bottomOverlayHeightPx + labelMarginPx,
                        )
                        val playerToTapLabelState = rememberMarkerState(position = labelPos)
                        playerToTapLabelState.position = labelPos
                        val labelIcon = remember(dist) {
                            BitmapDescriptorFactory.fromBitmap(
                                createDistanceLabelBitmap("$dist", Color.White.toArgb())
                            )
                        }
                        Marker(
                            state = playerToTapLabelState,
                            icon = labelIcon,
                            anchor = androidx.compose.ui.geometry.Offset(0.5f, 0.5f),
                            onClick = { viewModel.clearTappedLocation(); true }
                        )
                    }
                }

                // Line from tapped point to green center
                val greenCenterLat = uiState.currentHoleInfo?.greenCenterLat
                val greenCenterLng = uiState.currentHoleInfo?.greenCenterLng
                if (greenCenterLat != null && greenCenterLng != null) {
                    val greenPos = LatLng(greenCenterLat, greenCenterLng)
                    Polyline(
                        points = listOf(tapPoint, greenPos),
                        color = Color.White,
                        width = 8f
                    )
                    // Distance label at midpoint, slides along line to stay on screen
                    uiState.distanceFromTapToGreen?.let { dist ->
                        val labelPos = visibleMidpointOnLine(
                            tapPoint, greenPos, projection, mapSizePx,
                            topInsetPx = topOverlayHeightPx + labelMarginPx,
                            bottomInsetPx = bottomOverlayHeightPx + labelMarginPx,
                        )
                        val tapToGreenLabelState = rememberMarkerState(position = labelPos)
                        tapToGreenLabelState.position = labelPos
                        val labelIcon = remember(dist) {
                            BitmapDescriptorFactory.fromBitmap(
                                createDistanceLabelBitmap("$dist", Color.White.toArgb())
                            )
                        }
                        Marker(
                            state = tapToGreenLabelState,
                            icon = labelIcon,
                            anchor = androidx.compose.ui.geometry.Offset(0.5f, 0.5f),
                            onClick = { viewModel.clearTappedLocation(); true }
                        )
                    }
                }
            }
        }

        // Drag overlay: intercepts touches near the target.
        // Quick tap clears the target; drag (movement past slop) repositions it.
        if (uiState.tappedLocation != null) {
            var isPotentialTap by remember { mutableStateOf(false) }
            var isDragging by remember { mutableStateOf(false) }
            var dragOffsetX by remember { mutableFloatStateOf(0f) }
            var dragOffsetY by remember { mutableFloatStateOf(0f) }
            var downX by remember { mutableFloatStateOf(0f) }
            var downY by remember { mutableFloatStateOf(0f) }
            val tapSlopPx = 24f
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInteropFilter { event ->
                        val projection = cameraPositionState.projection ?: return@pointerInteropFilter false
                        val tapLoc = uiState.tappedLocation ?: return@pointerInteropFilter false

                        when (event.action) {
                            android.view.MotionEvent.ACTION_DOWN -> {
                                val targetScreen = projection.toScreenLocation(tapLoc)
                                val dx = event.x - targetScreen.x
                                val dy = event.y - targetScreen.y
                                val dist = kotlin.math.sqrt((dx * dx + dy * dy).toDouble())
                                if (dist < 100.0) {
                                    isPotentialTap = true
                                    isDragging = false
                                    downX = event.x
                                    downY = event.y
                                    dragOffsetX = targetScreen.x - event.x
                                    dragOffsetY = targetScreen.y - event.y
                                    true // consume: touch is on or near target
                                } else {
                                    false // pass through to map
                                }
                            }
                            android.view.MotionEvent.ACTION_MOVE -> {
                                if (isPotentialTap || isDragging) {
                                    if (!isDragging) {
                                        val mdx = event.x - downX
                                        val mdy = event.y - downY
                                        val moved = kotlin.math.sqrt((mdx * mdx + mdy * mdy).toDouble())
                                        if (moved > tapSlopPx) {
                                            isDragging = true
                                            isPotentialTap = false
                                        }
                                    }
                                    if (isDragging) {
                                        val screenPt = android.graphics.Point(
                                            (event.x + dragOffsetX).toInt(),
                                            (event.y + dragOffsetY).toInt()
                                        )
                                        val newLatLng = projection.fromScreenLocation(screenPt)
                                        viewModel.onTargetDragged(newLatLng)
                                    }
                                    true
                                } else {
                                    false
                                }
                            }
                            android.view.MotionEvent.ACTION_UP -> {
                                val consumed = isPotentialTap || isDragging
                                if (isPotentialTap && !isDragging) {
                                    // Quick tap on target → clear it
                                    viewModel.clearTappedLocation()
                                }
                                isPotentialTap = false
                                isDragging = false
                                consumed
                            }
                            android.view.MotionEvent.ACTION_CANCEL -> {
                                val consumed = isPotentialTap || isDragging
                                isPotentialTap = false
                                isDragging = false
                                consumed
                            }
                            else -> false
                        }
                    }
            )
        }

        // Full-screen click-catcher for the note quick-view.
        // Placed BEFORE the interactive overlays so their buttons receive their own taps;
        // this catcher only handles taps on non-interactive areas (the map).
        if (uiState.showNoteQuickView) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = viewModel::dismissNoteQuickView
                    )
            )
        }

        // Layer 2: Top overlay
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .onSizeChanged { topOverlayHeightPx = it.height }
                .statusBarsPadding()
                .padding(top = 8.dp, start = 8.dp, end = 8.dp)
        ) {
            // Hole navigation bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(overlayBackground)
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = viewModel::showEndRoundDialog) {
                    Icon(
                        Icons.Default.Stop,
                        contentDescription = "End Round",
                        tint = overlayText
                    )
                }

                IconButton(
                    onClick = viewModel::previousHole,
                    enabled = uiState.currentHoleNumber > 1
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous hole",
                        tint = if (uiState.currentHoleNumber > 1) overlayText else overlayTextDim
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Hole ${uiState.currentHoleNumber}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = overlayText
                    )
                    Text(
                        text = "Par ${uiState.currentHoleScore?.par ?: 4}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = overlayTextDim
                    )
                }

                IconButton(
                    onClick = viewModel::nextHole,
                    enabled = uiState.currentHoleNumber < 18
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next hole",
                        tint = if (uiState.currentHoleNumber < 18) overlayText else overlayTextDim
                    )
                }

                IconButton(onClick = onOpenScorecard) {
                    Icon(
                        Icons.Default.TableChart,
                        contentDescription = "Scorecard",
                        tint = overlayText
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Distance to green - compact vertical layout
            Column(
                modifier = Modifier
                    .align(Alignment.End)
                    .clip(RoundedCornerShape(12.dp))
                    .background(overlayBackground)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.End
            ) {
                if (uiState.currentLocation == null) {
                    Text(
                        text = "Waiting for GPS...",
                        color = overlayTextDim,
                        style = MaterialTheme.typography.bodySmall
                    )
                } else if (uiState.distanceToGreen.front == null &&
                    uiState.distanceToGreen.center == null &&
                    uiState.distanceToGreen.back == null
                ) {
                    Text(
                        text = "No green data",
                        color = overlayTextDim,
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    OverlayDistanceItem("Back", uiState.distanceToGreen.back, icon = Icons.Default.KeyboardArrowUp)
                    OverlayDistanceItem("Center", uiState.distanceToGreen.center, icon = Icons.Default.GolfCourse)
                    OverlayDistanceItem("Front", uiState.distanceToGreen.front, icon = Icons.Default.KeyboardArrowDown)
                }
            }
        }

        // Recenter map button
        IconButton(
            onClick = {
                centeringLevel = 0
                viewModel.onRecenter()
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = 12.dp, top = 72.dp)
                .size(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(overlayBackground)
        ) {
            Icon(
                Icons.Default.MyLocation,
                contentDescription = "Recenter map",
                tint = overlayText,
                modifier = Modifier.size(20.dp)
            )
        }

        // Layer 3: Bottom overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .onSizeChanged { bottomOverlayHeightPx = it.height }
                .navigationBarsPadding()
                .padding(bottom = 8.dp, start = 8.dp, end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Shots, Pen, Putts, Score row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(overlayBackground)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shots — click adds a shot, long-press undoes the most recent
                StatCounter(
                    label = "Shots",
                    value = uiState.score - uiState.putts - uiState.penalties,
                    onClick = viewModel::recordShotWithoutLocation,
                    onLongClick = viewModel::removeLastShot,
                )

                // Penalty — click adds, long-press removes
                StatCounter(
                    label = "Pen",
                    value = uiState.penalties,
                    onClick = viewModel::incrementPenalties,
                    onLongClick = viewModel::decrementPenalties,
                )

                // Putts — click adds, long-press removes
                StatCounter(
                    label = "Putts",
                    value = uiState.putts,
                    onClick = viewModel::incrementPutts,
                    onLongClick = viewModel::decrementPutts,
                )

                Spacer(modifier = Modifier.weight(1f))

                // Score
                Text("Score", color = overlayText, style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${uiState.score}",
                    color = overlayText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Note icon — floats at the bottom-right below the score row.
        // Lives outside the bottom Column so adding it doesn't expand the column upward.
        if (uiState.currentHoleInfo != null) {
            val notes = uiState.currentHoleInfo?.notes
            val hasNote = !notes.isNullOrBlank()
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 8.dp, bottom = 8.dp)
                    .size(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(overlayBackground)
            ) {
                IconButton(
                    onClick = viewModel::onNoteIconTapped,
                    modifier = Modifier.fillMaxSize()
                ) {
                    @Suppress("DEPRECATION")
                    Icon(
                        imageVector = if (hasNote) Icons.Default.StickyNote2
                                      else Icons.Outlined.StickyNote2,
                        contentDescription = if (hasNote) "View hole note"
                                              else "Add hole note",
                        tint = overlayText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Note quick-view bubble (shown when showNoteQuickView == true).
        // The dismiss-on-outside-tap click-catcher is placed earlier in the Box (above the
        // map but below the interactive overlays) so the score row and top nav buttons
        // still receive their taps while the bubble is visible.
        if (uiState.showNoteQuickView) {
            val notes = uiState.currentHoleInfo?.notes.orEmpty()
            // Bubble itself, positioned above the bottom overlay row.
            // Tap inside the bubble → open editor. Tap outside → dismiss (via click-catcher above).
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(start = 16.dp, end = 8.dp, bottom = 64.dp)
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = viewModel::openNoteEditorFromQuickView
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .background(overlayBackground)
                    .padding(12.dp)
            ) {
                Text(
                    text = notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = overlayText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                        .verticalScroll(rememberScrollState())
                )
            }
        }
    }

    // Club selector bottom sheet
    if (uiState.showClubSelector) {
        ClubSelectorSheet(
            clubs = uiState.clubs,
            onClubSelected = viewModel::onClubSelected,
            onDismiss = viewModel::hideClubSelector
        )
    }

    // End round confirmation dialog
    if (uiState.showEndRoundDialog) {
        AlertDialog(
            onDismissRequest = viewModel::hideEndRoundDialog,
            title = { Text("End Round?") },
            text = { Text("Are you sure you want to end this round? Make sure you've entered all your scores.") },
            confirmButton = {
                Button(onClick = { viewModel.endRound(onRoundComplete) }) {
                    Text("Save Round")
                }
            },
            dismissButton = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { viewModel.discardRound(onRoundComplete) }) {
                        Text("Discard", color = MaterialTheme.colorScheme.error)
                    }
                    TextButton(onClick = viewModel::hideEndRoundDialog) {
                        Text("Cancel")
                    }
                }
            }
        )
    }

    // Full-screen note editor
    if (uiState.showNoteEditor) {
        val focusRequester = remember { FocusRequester() }
        val savedNotes = uiState.currentHoleInfo?.notes.orEmpty()
        val draft = uiState.noteEditorDraft
        val isDirty = draft.trim() != savedNotes
        val hasSavedNote = savedNotes.isNotEmpty()

        Dialog(
            onDismissRequest = viewModel::requestCloseEditor,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Top bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = viewModel::requestCloseEditor) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                        Text(
                            text = "Hole ${uiState.currentHoleNumber} notes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        TextButton(
                            onClick = viewModel::deleteNote,
                            enabled = hasSavedNote
                        ) {
                            Text(
                                "Delete",
                                color = if (hasSavedNote) MaterialTheme.colorScheme.error
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Button(
                            onClick = viewModel::saveNote,
                            enabled = isDirty
                        ) {
                            Text("Save")
                        }
                    }
                    // Body: multi-line text field
                    OutlinedTextField(
                        value = draft,
                        onValueChange = viewModel::updateNoteDraft,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .focusRequester(focusRequester),
                        placeholder = { Text("Notes for hole ${uiState.currentHoleNumber}…") },
                        singleLine = false
                    )
                    LaunchedEffect(Unit) { focusRequester.requestFocus() }
                }
            }
        }
    }

    // Discard-changes confirmation dialog
    if (uiState.showDiscardNoteDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDiscardNoteDialog,
            title = { Text("Discard changes?") },
            text = { Text("Your unsaved changes will be lost.") },
            confirmButton = {
                Button(onClick = viewModel::cancelNoteEdit) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDiscardNoteDialog) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun StatCounter(
    label: String,
    value: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = overlayText, style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$value",
            color = overlayText,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun OverlayDistanceItem(
    label: String,
    distance: Int?,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = overlayText,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = distance?.toString() ?: "-",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Normal,
            color = overlayText
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClubSelectorSheet(
    clubs: List<com.example.shottracker.domain.model.Club>,
    onClubSelected: (com.example.shottracker.domain.model.Club) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Select Club",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyColumn {
                items(clubs) { club ->
                    TextButton(
                        onClick = { onClubSelected(club) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = club.name,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }
        }
    }
}

/**
 * Places a distance label along the line A→B so it stays on screen.
 *
 * Works in screen-pixel space via [projection], so it is correct even when the
 * map has a bearing or tilt (the visible viewport is then a rotated rectangle,
 * whose axis-aligned lat/lng bounding box is much larger than what is actually
 * visible — clipping in lat/lng space would leave the label off screen).
 *
 * The label is placed at the geometric midpoint when that point is visible, and
 * otherwise slides along the line to the visible point closest to the midpoint.
 * Falls back to the geometric midpoint if the projection/size are unavailable or
 * the whole line is off screen.
 *
 * [topInsetPx] / [bottomInsetPx] keep the label clear of the overlays that float
 * over the map (hole/distance card up top, the score bar at the bottom).
 */
private fun visibleMidpointOnLine(
    a: LatLng,
    b: LatLng,
    projection: Projection?,
    sizePx: IntSize,
    topInsetPx: Float,
    bottomInsetPx: Float,
): LatLng {
    val mid = LatLng(
        (a.latitude + b.latitude) / 2.0,
        (a.longitude + b.longitude) / 2.0
    )
    if (projection == null || sizePx.width <= 0 || sizePx.height <= 0) return mid

    val pa = projection.toScreenLocation(a)
    val pb = projection.toScreenLocation(b)

    // Horizontal: small inset so the label doesn't hug the screen edge.
    val padX = sizePx.width * 0.08
    // Vertical: exclude the overlay bands. If the overlays would leave no room
    // (not yet measured, or unusually tall), fall back to a plain 10% inset.
    var minY = topInsetPx.toDouble()
    var maxY = sizePx.height - bottomInsetPx.toDouble()
    if (maxY - minY < sizePx.height * 0.15) {
        minY = sizePx.height * 0.10
        maxY = sizePx.height * 0.90
    }
    val t = visibleMidpointT(
        pa.x.toDouble(), pa.y.toDouble(),
        pb.x.toDouble(), pb.y.toDouble(),
        padX, minY,
        sizePx.width - padX, maxY,
    )

    val sx = (pa.x + (pb.x - pa.x) * t).roundToInt()
    val sy = (pa.y + (pb.y - pa.y) * t).roundToInt()
    return projection.fromScreenLocation(Point(sx, sy)) ?: mid
}

private fun createGreenDotBitmap(): Bitmap {
    val size = 40
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    val outlinePaint = Paint().apply {
        color = android.graphics.Color.DKGRAY
        style = Paint.Style.STROKE
        strokeWidth = 3f
        isAntiAlias = true
    }
    canvas.drawCircle(size / 2f, size / 2f, 14f, paint)
    canvas.drawCircle(size / 2f, size / 2f, 14f, outlinePaint)
    return bitmap
}

private fun createGreenFlagBitmap(): Bitmap {
    val width = 56
    val height = 76
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Flag pole
    val polePaint = Paint().apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }
    val poleX = width / 2f
    canvas.drawLine(poleX, 12f, poleX, height.toFloat(), polePaint)

    // Flag triangle
    val flagPaint = Paint().apply {
        color = android.graphics.Color.RED
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    val flagPath = android.graphics.Path().apply {
        moveTo(poleX, 12f)
        lineTo(poleX + 22f, 24f)
        lineTo(poleX, 36f)
        close()
    }
    canvas.drawPath(flagPath, flagPaint)

    // Base dot
    val basePaint = Paint().apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    canvas.drawCircle(poleX, height - 4f, 6f, basePaint)

    return bitmap
}

private fun createTargetBitmap(): Bitmap {
    val size = 80
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val cx = size / 2f
    val cy = size / 2f

    val ringPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }
    val fillPaint = Paint().apply {
        color = android.graphics.Color.RED
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    // Outer ring
    canvas.drawCircle(cx, cy, 36f, ringPaint)
    // Inner ring
    canvas.drawCircle(cx, cy, 20f, ringPaint)
    // Center dot
    canvas.drawCircle(cx, cy, 6f, fillPaint)
    // Crosshair lines
    canvas.drawLine(cx, cy - 38f, cx, cy - 22f, ringPaint)
    canvas.drawLine(cx, cy + 22f, cx, cy + 38f, ringPaint)
    canvas.drawLine(cx - 38f, cy, cx - 22f, cy, ringPaint)
    canvas.drawLine(cx + 22f, cy, cx + 38f, cy, ringPaint)

    return bitmap
}

private fun createDistanceLabelBitmap(text: String, textColor: Int): Bitmap {
    val textPaint = Paint().apply {
        color = textColor
        textSize = 48f
        isAntiAlias = true
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }
    val bgPaint = Paint().apply {
        color = android.graphics.Color.argb(180, 0, 0, 0)
        isAntiAlias = true
    }

    val textBounds = android.graphics.Rect()
    textPaint.getTextBounds(text, 0, text.length, textBounds)

    val paddingH = 20
    val paddingV = 12
    val width = textBounds.width() + paddingH * 2
    val height = textBounds.height() + paddingV * 2

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawRoundRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), 16f, 16f, bgPaint)
    canvas.drawText(text, width / 2f, height / 2f - textBounds.exactCenterY(), textPaint)

    return bitmap
}
