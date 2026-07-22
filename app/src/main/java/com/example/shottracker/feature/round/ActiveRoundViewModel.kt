package com.example.shottracker.feature.round

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shottracker.core.dnd.DndController
import com.example.shottracker.core.location.LocationService
import com.example.shottracker.core.prefs.AppPreferences
import com.example.shottracker.core.util.DistanceCalculator
import com.example.shottracker.core.util.DistanceToGreen
import com.example.shottracker.domain.handicap.HandicapCalculator
import com.example.shottracker.domain.model.Club
import com.example.shottracker.domain.model.GpsLocation
import com.google.android.gms.maps.model.LatLng
import com.example.shottracker.domain.model.HoleInfo
import com.example.shottracker.domain.model.HoleScore
import com.example.shottracker.domain.model.Round
import com.example.shottracker.domain.model.RoundStatus
import com.example.shottracker.domain.model.Shot
import com.example.shottracker.domain.repository.ClubRepository
import com.example.shottracker.domain.repository.CourseRepository
import com.example.shottracker.domain.repository.RoundRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

data class ActiveRoundUiState(
    val round: Round? = null,
    val currentHoleNumber: Int = 1,
    val currentHoleScore: HoleScore? = null,
    val currentHoleInfo: HoleInfo? = null,
    val shots: List<Shot> = emptyList(),
    val clubs: List<Club> = emptyList(),
    val selectedClub: Club? = null,
    val currentLocation: GpsLocation? = null,
    val distanceToGreen: DistanceToGreen = DistanceToGreen.EMPTY,
    val hasLocationPermission: Boolean = false,
    val isLoading: Boolean = true,
    val showClubSelector: Boolean = false,
    val showEndRoundDialog: Boolean = false,
    val putts: Int = 0,
    val penalties: Int = 0,
    val score: Int = 0,
    val tappedLocation: LatLng? = null,
    val distanceToTap: Int? = null,
    val distanceFromTapToGreen: Int? = null,
    val showNoteQuickView: Boolean = false,
    val showNoteEditor: Boolean = false,
    val noteEditorDraft: String = "",
    val showDiscardNoteDialog: Boolean = false,
    val holeHandicaps: Map<Int, Int> = emptyMap(),
    val teeSlope: Int? = null,
    val teeRating: Double? = null
)

@HiltViewModel
class ActiveRoundViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val roundRepository: RoundRepository,
    private val courseRepository: CourseRepository,
    private val clubRepository: ClubRepository,
    private val locationService: LocationService,
    private val dndController: DndController,
    private val prefs: AppPreferences
) : ViewModel() {

    private val roundId: Long = savedStateHandle.get<Long>("roundId") ?: 0L
    private var courseId: Long? = null

    private val _uiState = MutableStateFlow(ActiveRoundUiState())
    val uiState: StateFlow<ActiveRoundUiState> = _uiState.asStateFlow()

    init {
        loadRound()
        loadClubs()
        checkLocationPermission()
    }

    private fun loadRound() {
        viewModelScope.launch {
            val round = roundRepository.getRoundById(roundId)
            if (round != null) {
                _uiState.value = _uiState.value.copy(
                    round = round,
                    isLoading = false
                )
                // Re-assert DND if the user opted in (covers resume / app relaunch mid-round).
                if (prefs.silenceDuringRound) dndController.enableForRound()
                resolveCourseId(round)
                loadTeeHandicaps(round.teeId)
                loadOrCreateCurrentHole()
            }
        }
    }

    private suspend fun loadTeeHandicaps(teeId: Long?) {
        if (teeId == null) return
        val tee = courseRepository.getTeeById(teeId)
        val map = courseRepository.getHoleHandicaps(teeId)
        _uiState.value = _uiState.value.copy(
            holeHandicaps = map,
            teeSlope = tee?.slope,
            teeRating = tee?.rating
        )
    }

    private suspend fun resolveCourseId(round: Round) {
        // Try to get courseId from the tee
        val teeId = round.teeId
        if (teeId != null) {
            val tee = courseRepository.getTeeById(teeId)
            if (tee != null) {
                courseId = tee.courseId
                return
            }
        }
        // Fall back to looking up course by name
        val course = courseRepository.getCourseByName(round.courseName)
        courseId = course?.id
    }

    private fun loadClubs() {
        viewModelScope.launch {
            clubRepository.getAllClubs().collect { clubs ->
                _uiState.value = _uiState.value.copy(clubs = clubs)
            }
        }
    }

    private fun checkLocationPermission() {
        _uiState.value = _uiState.value.copy(
            hasLocationPermission = locationService.hasLocationPermission()
        )
    }

    fun onLocationPermissionGranted() {
        _uiState.value = _uiState.value.copy(hasLocationPermission = true)
        startLocationUpdates()
    }

    fun startLocationUpdates() {
        if (!locationService.hasLocationPermission()) return

        viewModelScope.launch {
            locationService.getLocationUpdates().collect { location ->
                _uiState.value = _uiState.value.copy(currentLocation = location)
                updateDistanceToGreen(location)
            }
        }
    }

    private fun updateDistanceToGreen(location: GpsLocation) {
        val holeInfo = _uiState.value.currentHoleInfo ?: return

        val frontDistance = if (holeInfo.greenFrontLat != null && holeInfo.greenFrontLng != null) {
            DistanceCalculator.calculateDistanceYards(
                location.latitude, location.longitude,
                holeInfo.greenFrontLat, holeInfo.greenFrontLng
            )
        } else null

        val centerDistance = if (holeInfo.greenCenterLat != null && holeInfo.greenCenterLng != null) {
            DistanceCalculator.calculateDistanceYards(
                location.latitude, location.longitude,
                holeInfo.greenCenterLat, holeInfo.greenCenterLng
            )
        } else null

        val backDistance = if (holeInfo.greenBackLat != null && holeInfo.greenBackLng != null) {
            DistanceCalculator.calculateDistanceYards(
                location.latitude, location.longitude,
                holeInfo.greenBackLat, holeInfo.greenBackLng
            )
        } else null

        _uiState.value = _uiState.value.copy(
            distanceToGreen = DistanceToGreen(frontDistance, centerDistance, backDistance)
        )
    }

    private suspend fun loadOrCreateCurrentHole() {
        val holeNumber = _uiState.value.currentHoleNumber

        // Load hole info (with green coordinates) from the course
        val holeInfo = courseId?.let { id ->
            courseRepository.getHoleByNumber(id, holeNumber)
        }
        // Auto-show the note bubble only when this is a fresh hole load
        // (e.g., after nextHole/previousHole, which null out currentHoleInfo first).
        // On no-op refreshes (e.g., LifecycleResumeEffect or returning from the scorecard),
        // don't touch showNoteQuickView — preserves whether the user previously dismissed it.
        val isFreshLoad = _uiState.value.currentHoleInfo?.id != holeInfo?.id
        val shouldAutoShowNote = isFreshLoad && !holeInfo?.notes.isNullOrBlank()
        _uiState.value = if (shouldAutoShowNote) {
            _uiState.value.copy(currentHoleInfo = holeInfo, showNoteQuickView = true)
        } else {
            _uiState.value.copy(currentHoleInfo = holeInfo)
        }

        var holeScore = roundRepository.getHoleScore(roundId, holeNumber)

        if (holeScore == null) {
            // Create new hole score
            val par = holeInfo?.par ?: 4
            val holeScoreId = roundRepository.createHoleScore(
                HoleScore(
                    roundId = roundId,
                    holeNumber = holeNumber,
                    par = par
                )
            )
            holeScore = roundRepository.getHoleScore(roundId, holeNumber)
        }

        val shots = holeScore?.let {
            roundRepository.getShotsForHoleSync(it.id)
        } ?: emptyList()

        _uiState.value = _uiState.value.copy(
            currentHoleScore = holeScore,
            shots = shots,
            score = holeScore?.score ?: shots.size,
            putts = holeScore?.putts ?: 0,
            penalties = holeScore?.penalties ?: 0
        )

        // Recalculate distance if we have a location
        val location = _uiState.value.currentLocation
        if (location != null) {
            updateDistanceToGreen(location)
        }
    }

    fun refreshCurrentHole() {
        viewModelScope.launch {
            loadOrCreateCurrentHole()
        }
    }

    fun showClubSelector() {
        _uiState.value = _uiState.value.copy(showClubSelector = true)
    }

    fun hideClubSelector() {
        _uiState.value = _uiState.value.copy(showClubSelector = false)
    }

    fun onClubSelected(club: Club) {
        _uiState.value = _uiState.value.copy(
            selectedClub = club,
            showClubSelector = false
        )
    }

    fun recordShot() {
        viewModelScope.launch {
            val location = _uiState.value.currentLocation ?: return@launch
            val holeScoreId = _uiState.value.currentHoleScore?.id ?: return@launch
            val club = _uiState.value.selectedClub

            val shotNumber = _uiState.value.shots.size + 1
            val lastShot = _uiState.value.shots.lastOrNull()

            val shot = Shot(
                holeScoreId = holeScoreId,
                clubId = club?.id,
                clubName = club?.name,
                shotNumber = shotNumber,
                latitude = location.latitude,
                longitude = location.longitude,
                timestamp = Instant.now()
            )

            val shotId = roundRepository.recordShot(shot)

            // Calculate distance from last shot
            if (lastShot != null) {
                val distance = DistanceCalculator.calculateDistanceYards(
                    lastShot.latitude, lastShot.longitude,
                    location.latitude, location.longitude
                )
                roundRepository.updateShotDistance(lastShot.id, distance)
            }

            // Reload shots
            val updatedShots = roundRepository.getShotsForHoleSync(holeScoreId)
            _uiState.value = _uiState.value.copy(
                shots = updatedShots,
                score = _uiState.value.score + 1
            )
        }
    }

    fun onPuttsChanged(putts: Int) {
        val newPutts = putts.coerceIn(0, 10)
        val currentScore = _uiState.value.score
        val currentPutts = _uiState.value.putts
        _uiState.value = _uiState.value.copy(
            putts = newPutts,
            score = (currentScore - currentPutts) + newPutts
        )
    }

    fun incrementPutts() {
        onPuttsChanged(_uiState.value.putts + 1)
        viewModelScope.launch { saveCurrentHole() }
    }

    fun decrementPutts() {
        if (_uiState.value.putts <= 0) return
        onPuttsChanged(_uiState.value.putts - 1)
        viewModelScope.launch { saveCurrentHole() }
    }

    fun onPenaltiesChanged(penalties: Int) {
        val newPenalties = penalties.coerceIn(0, 10)
        val currentScore = _uiState.value.score
        val currentPenalties = _uiState.value.penalties
        _uiState.value = _uiState.value.copy(
            penalties = newPenalties,
            score = (currentScore - currentPenalties) + newPenalties
        )
    }

    fun incrementPenalties() {
        onPenaltiesChanged(_uiState.value.penalties + 1)
        viewModelScope.launch { saveCurrentHole() }
    }

    fun decrementPenalties() {
        if (_uiState.value.penalties <= 0) return
        onPenaltiesChanged(_uiState.value.penalties - 1)
        viewModelScope.launch { saveCurrentHole() }
    }

    fun recordShotWithoutLocation() {
        viewModelScope.launch {
            val holeScoreId = _uiState.value.currentHoleScore?.id ?: return@launch
            val club = _uiState.value.selectedClub

            val shotNumber = _uiState.value.shots.size + 1

            val shot = Shot(
                holeScoreId = holeScoreId,
                clubId = club?.id,
                clubName = club?.name,
                shotNumber = shotNumber,
                latitude = 0.0,
                longitude = 0.0,
                timestamp = Instant.now()
            )

            roundRepository.recordShot(shot)

            val updatedShots = roundRepository.getShotsForHoleSync(holeScoreId)
            _uiState.value = _uiState.value.copy(
                shots = updatedShots,
                score = _uiState.value.score + 1
            )
            saveCurrentHole()
        }
    }

    /** Long-press handler on the Shots button: undo the most recent shot for this hole. */
    fun removeLastShot() {
        viewModelScope.launch {
            val holeScoreId = _uiState.value.currentHoleScore?.id ?: return@launch
            val lastShot = roundRepository.getLastShot(holeScoreId) ?: return@launch
            roundRepository.deleteShot(lastShot.id)

            val updatedShots = roundRepository.getShotsForHoleSync(holeScoreId)
            val newScore = (_uiState.value.score - 1).coerceAtLeast(0)
            _uiState.value = _uiState.value.copy(
                shots = updatedShots,
                score = newScore
            )
            saveCurrentHole()
        }
    }

    fun onScoreChanged(score: Int) {
        _uiState.value = _uiState.value.copy(score = score.coerceIn(1, 20))
    }

    fun nextHole() {
        viewModelScope.launch {
            saveCurrentHole()

            val maxHoles = 18 // Could be dynamic based on course
            val newHoleNumber = (_uiState.value.currentHoleNumber + 1).coerceAtMost(maxHoles)

            // Clear hole info so the camera waits for new green coords before re-centering
            _uiState.value = _uiState.value.copy(
                currentHoleNumber = newHoleNumber,
                currentHoleInfo = null,
                selectedClub = null,
                shots = emptyList(),
                tappedLocation = null,
                distanceToTap = null,
                distanceFromTapToGreen = null,
                showNoteQuickView = false,
                showNoteEditor = false,
                noteEditorDraft = "",
                showDiscardNoteDialog = false
            )

            loadOrCreateCurrentHole()
        }
    }

    fun previousHole() {
        viewModelScope.launch {
            saveCurrentHole()

            val newHoleNumber = (_uiState.value.currentHoleNumber - 1).coerceAtLeast(1)

            // Clear hole info so the camera waits for new green coords before re-centering
            _uiState.value = _uiState.value.copy(
                currentHoleNumber = newHoleNumber,
                currentHoleInfo = null,
                selectedClub = null,
                tappedLocation = null,
                distanceToTap = null,
                distanceFromTapToGreen = null,
                showNoteQuickView = false,
                showNoteEditor = false,
                noteEditorDraft = "",
                showDiscardNoteDialog = false
            )

            loadOrCreateCurrentHole()
        }
    }

    private suspend fun saveCurrentHole() {
        val holeScore = _uiState.value.currentHoleScore ?: return
        val score = _uiState.value.score
        val putts = _uiState.value.putts
        val penalties = _uiState.value.penalties

        // Auto-cap adjustment based on Net Double Bogey when all prerequisites are present.
        // When prerequisites are missing, fall back to the previously-saved adjustment.
        val computedAdjustment = if (score > 0) computeAutoCap(score) else null
        val adjustmentToSave = if (autoCapPrerequisitesMet()) computedAdjustment else holeScore.adjustment

        roundRepository.updateHoleScoreStats(
            holeScoreId = holeScore.id,
            score = if (score > 0) score else null,
            putts = if (putts > 0) putts else null,
            penalties = if (penalties > 0) penalties else null,
            adjustment = adjustmentToSave,
            fairwayHit = null,
            gir = null
        )
    }

    private fun autoCapPrerequisitesMet(): Boolean {
        val round = _uiState.value.round ?: return false
        if (round.handicapIndex == null) return false
        if (_uiState.value.teeSlope == null) return false
        val holeNumber = _uiState.value.currentHoleNumber
        return _uiState.value.holeHandicaps[holeNumber] != null &&
            _uiState.value.currentHoleInfo != null
    }

    /**
     * Returns the NDB-capped adjustment for the current hole, or null if no adjustment is needed
     * (score at or below NDB) or if any prerequisite is missing.
     */
    private fun computeAutoCap(score: Int): Int? {
        val round = _uiState.value.round ?: return null
        val holeInfo = _uiState.value.currentHoleInfo ?: return null
        val holeNumber = _uiState.value.currentHoleNumber
        val handicapIndex = round.handicapIndex ?: return null
        val slope = _uiState.value.teeSlope ?: return null
        val holeHandicap = _uiState.value.holeHandicaps[holeNumber] ?: return null
        val courseHandicap = HandicapCalculator.courseHandicap(handicapIndex, slope)
        val strokes = HandicapCalculator.strokesReceived(holeHandicap, courseHandicap)
        val ndbCap = HandicapCalculator.netDoubleBogey(holeInfo.par, strokes)
        return if (score > ndbCap) score - ndbCap else null
    }

    fun onMapTapped(latLng: LatLng) {
        val location = _uiState.value.currentLocation
        val holeInfo = _uiState.value.currentHoleInfo

        // If tapping near the same spot, clear the target
        val existing = _uiState.value.tappedLocation
        if (existing != null) {
            val tapDist = DistanceCalculator.calculateDistanceMeters(
                existing.latitude, existing.longitude,
                latLng.latitude, latLng.longitude
            )
            if (tapDist < 5.0) {
                clearTappedLocation()
                return
            }
        }

        val distToTap = if (location != null) {
            DistanceCalculator.calculateDistanceYards(
                location.latitude, location.longitude,
                latLng.latitude, latLng.longitude
            )
        } else null

        val distTapToGreen = if (holeInfo?.greenCenterLat != null && holeInfo.greenCenterLng != null) {
            DistanceCalculator.calculateDistanceYards(
                latLng.latitude, latLng.longitude,
                holeInfo.greenCenterLat, holeInfo.greenCenterLng
            )
        } else null

        _uiState.value = _uiState.value.copy(
            tappedLocation = latLng,
            distanceToTap = distToTap,
            distanceFromTapToGreen = distTapToGreen
        )
    }

    fun onTargetDragged(latLng: LatLng) {
        val location = _uiState.value.currentLocation
        val holeInfo = _uiState.value.currentHoleInfo

        val distToTap = if (location != null) {
            DistanceCalculator.calculateDistanceYards(
                location.latitude, location.longitude,
                latLng.latitude, latLng.longitude
            )
        } else null

        val distTapToGreen = if (holeInfo?.greenCenterLat != null && holeInfo.greenCenterLng != null) {
            DistanceCalculator.calculateDistanceYards(
                latLng.latitude, latLng.longitude,
                holeInfo.greenCenterLat, holeInfo.greenCenterLng
            )
        } else null

        _uiState.value = _uiState.value.copy(
            tappedLocation = latLng,
            distanceToTap = distToTap,
            distanceFromTapToGreen = distTapToGreen
        )
    }

    fun clearTappedLocation() {
        _uiState.value = _uiState.value.copy(
            tappedLocation = null,
            distanceToTap = null,
            distanceFromTapToGreen = null
        )
    }

    /**
     * Recenter-button reset: clear any (now-stale) target line/marker and recompute the
     * front/center/back distances from the latest GPS fix. The camera re-framing itself is
     * handled in the UI layer.
     */
    fun onRecenter() {
        clearTappedLocation()
        _uiState.value.currentLocation?.let { updateDistanceToGreen(it) }
    }

    fun showEndRoundDialog() {
        _uiState.value = _uiState.value.copy(showEndRoundDialog = true)
    }

    fun hideEndRoundDialog() {
        _uiState.value = _uiState.value.copy(showEndRoundDialog = false)
    }

    fun endRound(onComplete: () -> Unit) {
        viewModelScope.launch {
            saveCurrentHole()
            roundRepository.updateRoundStatus(roundId, RoundStatus.COMPLETED)
            dndController.restore()
            onComplete()
        }
    }

    /** Discard the round entirely — deletes it from the DB and navigates away. */
    fun discardRound(onComplete: () -> Unit) {
        viewModelScope.launch {
            roundRepository.deleteRound(roundId)
            dndController.restore()
            onComplete()
        }
    }

    // --- Note handling ---

    fun onNoteIconTapped() {
        val notes = _uiState.value.currentHoleInfo?.notes
        if (notes.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                showNoteEditor = true,
                noteEditorDraft = ""
            )
        } else {
            _uiState.value = _uiState.value.copy(showNoteQuickView = true)
        }
    }

    fun dismissNoteQuickView() {
        _uiState.value = _uiState.value.copy(showNoteQuickView = false)
    }

    fun openNoteEditorFromQuickView() {
        val existing = _uiState.value.currentHoleInfo?.notes.orEmpty()
        _uiState.value = _uiState.value.copy(
            showNoteQuickView = false,
            showNoteEditor = true,
            noteEditorDraft = existing
        )
    }

    fun updateNoteDraft(text: String) {
        _uiState.value = _uiState.value.copy(noteEditorDraft = text)
    }

    fun saveNote() {
        val holeInfo = _uiState.value.currentHoleInfo ?: return
        val trimmed = _uiState.value.noteEditorDraft.trim()
        val newNotes = trimmed.ifBlank { null }

        viewModelScope.launch {
            val updated = holeInfo.copy(notes = newNotes)
            courseRepository.updateHole(updated)
            _uiState.value = _uiState.value.copy(
                currentHoleInfo = updated,
                showNoteEditor = false,
                showDiscardNoteDialog = false,
                noteEditorDraft = ""
            )
        }
    }

    fun deleteNote() {
        _uiState.value = _uiState.value.copy(noteEditorDraft = "")
        saveNote()
    }

    fun requestCloseEditor() {
        val draft = _uiState.value.noteEditorDraft.trim()
        val saved = _uiState.value.currentHoleInfo?.notes.orEmpty()
        if (draft == saved) {
            _uiState.value = _uiState.value.copy(
                showNoteEditor = false,
                noteEditorDraft = ""
            )
        } else {
            _uiState.value = _uiState.value.copy(showDiscardNoteDialog = true)
        }
    }

    fun cancelNoteEdit() {
        _uiState.value = _uiState.value.copy(
            showNoteEditor = false,
            showDiscardNoteDialog = false,
            noteEditorDraft = ""
        )
    }

    fun dismissDiscardNoteDialog() {
        _uiState.value = _uiState.value.copy(showDiscardNoteDialog = false)
    }
}
