package com.example.shottracker.feature.map

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shottracker.core.location.LocationService
import com.example.shottracker.domain.model.GpsLocation
import com.example.shottracker.domain.model.HoleInfo
import com.example.shottracker.domain.model.Shot
import com.example.shottracker.domain.repository.CourseRepository
import com.example.shottracker.domain.repository.RoundRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MapUiState(
    val currentLocation: GpsLocation? = null,
    val holeInfo: HoleInfo? = null,
    val shots: List<Shot> = emptyList(),
    val holeNumber: Int = 1,
    val isLoading: Boolean = true
)

@HiltViewModel
class MapViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val roundRepository: RoundRepository,
    private val courseRepository: CourseRepository,
    private val locationService: LocationService
) : ViewModel() {

    private val roundId: Long = savedStateHandle.get<Long>("roundId") ?: 0L
    private val holeNumber: Int = savedStateHandle.get<Int>("holeNumber") ?: 1

    private val _uiState = MutableStateFlow(MapUiState(holeNumber = holeNumber))
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        loadHoleData()
        fetchInitialLocation()
        startLocationUpdates()
    }

    private fun loadHoleData() {
        viewModelScope.launch {
            Log.d("MapVM", "Loading hole data: roundId=$roundId, holeNumber=$holeNumber")

            // Resolve courseId to load hole info with green coordinates
            val round = roundRepository.getRoundById(roundId)
            Log.d("MapVM", "Round: $round")
            var resolvedCourseId: Long? = null

            if (round != null) {
                val teeId = round.teeId
                if (teeId != null) {
                    val tee = courseRepository.getTeeById(teeId)
                    if (tee != null) resolvedCourseId = tee.courseId
                    Log.d("MapVM", "Resolved courseId from tee: $resolvedCourseId")
                }
                if (resolvedCourseId == null) {
                    val course = courseRepository.getCourseByName(round.courseName)
                    resolvedCourseId = course?.id
                    Log.d("MapVM", "Resolved courseId from name '${round.courseName}': $resolvedCourseId")
                }
            }

            // Load hole info (with green coordinates)
            val holeInfo = resolvedCourseId?.let { id ->
                courseRepository.getHoleByNumber(id, holeNumber)
            }
            Log.d("MapVM", "HoleInfo: $holeInfo")

            // Get hole score and shots
            val holeScore = roundRepository.getHoleScore(roundId, holeNumber)
            val shots = holeScore?.let {
                roundRepository.getShotsForHoleSync(it.id)
            } ?: emptyList()

            _uiState.value = _uiState.value.copy(
                holeInfo = holeInfo,
                shots = shots,
                isLoading = false
            )
        }
    }

    private fun fetchInitialLocation() {
        viewModelScope.launch {
            val location = locationService.getCurrentLocation()
            Log.d("MapVM", "Initial location: $location")
            if (location != null) {
                _uiState.value = _uiState.value.copy(currentLocation = location)
            }
        }
    }

    private fun startLocationUpdates() {
        viewModelScope.launch {
            locationService.getLocationUpdates().collect { location ->
                _uiState.value = _uiState.value.copy(currentLocation = location)
            }
        }
    }
}
