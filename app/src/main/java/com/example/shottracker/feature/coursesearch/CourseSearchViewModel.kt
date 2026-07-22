package com.example.shottracker.feature.coursesearch

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shottracker.core.geo.GeocodingService
import com.example.shottracker.core.location.LocationService
import com.example.shottracker.data.remote.OsmCourseResult
import com.example.shottracker.data.remote.OverpassRemoteDataSource
import com.example.shottracker.domain.repository.CourseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Which kind of search ran last, so Retry can re-run the correct one. */
enum class LastSearch { NONE, GPS, QUERY }

data class CourseSearchUiState(
    val courses: List<OsmCourseResult> = emptyList(),
    val locationQuery: String = "",
    val isLoading: Boolean = false,
    val hasSearched: Boolean = false,
    val error: String? = null,
    val importingCourseName: String? = null,
    val importSuccess: String? = null,
    val currentPage: Int = 0,
    val lastSearch: LastSearch = LastSearch.NONE,
    val lastQuery: String = ""
) {
    companion object {
        const val PAGE_SIZE = 10
    }

    val totalPages: Int
        get() = ((courses.size + PAGE_SIZE - 1) / PAGE_SIZE).coerceAtLeast(1)

    val pagedCourses: List<OsmCourseResult>
        get() = courses.drop(currentPage * PAGE_SIZE).take(PAGE_SIZE)
}

@HiltViewModel
class CourseSearchViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val overpassDataSource: OverpassRemoteDataSource,
    private val locationService: LocationService,
    private val geocodingService: GeocodingService
) : ViewModel() {

    private val _uiState = MutableStateFlow(CourseSearchUiState())
    val uiState: StateFlow<CourseSearchUiState> = _uiState.asStateFlow()

    /** Search near the device's current GPS location. */
    fun searchNearby() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true, error = null, lastSearch = LastSearch.GPS
            )

            val location = locationService.getCurrentLocation()
            Log.d("CourseSearch", "Location: $location, hasPermission: ${locationService.hasLocationPermission()}")
            if (location == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    hasSearched = true,
                    error = "Could not get your location. Please enable GPS and grant location permission."
                )
                return@launch
            }
            runOverpass(location.latitude, location.longitude)
        }
    }

    /** Geocode the entered zip/place, then search there. */
    fun searchByQuery() {
        val query = _uiState.value.locationQuery.trim()
        if (query.isEmpty() || _uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true, error = null, lastSearch = LastSearch.QUERY, lastQuery = query
            )
            val loc = geocodingService.geocode(query)
            if (loc == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    hasSearched = true,
                    courses = emptyList(),
                    error = "Couldn't find \"$query\". Try a zip code or 'City, State'."
                )
                return@launch
            }
            runOverpass(loc.latitude, loc.longitude)
        }
    }

    private suspend fun runOverpass(lat: Double, lng: Double) {
        try {
            val results = overpassDataSource.searchNearbyCourses(lat, lng)
            Log.d("CourseSearch", "Found ${results.size} courses with 9+ greens")
            _uiState.value = _uiState.value.copy(
                courses = results,
                isLoading = false,
                hasSearched = true,
                currentPage = 0
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                hasSearched = true,
                error = e.message ?: "Failed to search for courses"
            )
        }
    }

    fun retryLastSearch() {
        when (_uiState.value.lastSearch) {
            LastSearch.QUERY -> searchByQuery()
            LastSearch.GPS -> searchNearby()
            LastSearch.NONE -> Unit
        }
    }

    fun importCourse(course: OsmCourseResult) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(importingCourseName = course.name, error = null)

            courseRepository.importCourseFromOsm(course.name, course.lat, course.lng)
                .onSuccess { savedCourse ->
                    _uiState.value = _uiState.value.copy(
                        importingCourseName = null,
                        importSuccess = "\"${savedCourse.name}\" imported with ${savedCourse.holes.size} holes!"
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        importingCourseName = null,
                        error = exception.message ?: "Failed to import course"
                    )
                }
        }
    }

    fun onLocationPermissionDenied() {
        _uiState.value = _uiState.value.copy(
            error = "Location permission denied. Enter a zip code or place instead."
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearImportSuccess() {
        _uiState.value = _uiState.value.copy(importSuccess = null)
    }

    fun onLocationQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(locationQuery = query)
    }

    fun nextPage() {
        val state = _uiState.value
        if (state.currentPage < state.totalPages - 1) {
            _uiState.value = state.copy(currentPage = state.currentPage + 1)
        }
    }

    fun previousPage() {
        val state = _uiState.value
        if (state.currentPage > 0) {
            _uiState.value = state.copy(currentPage = state.currentPage - 1)
        }
    }
}
