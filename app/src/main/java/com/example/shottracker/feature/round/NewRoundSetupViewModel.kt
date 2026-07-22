package com.example.shottracker.feature.round

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shottracker.domain.handicap.HandicapCalculator
import com.example.shottracker.domain.model.Course
import com.example.shottracker.domain.model.HoleInfo
import com.example.shottracker.domain.model.Round
import com.example.shottracker.domain.model.Tee
import com.example.shottracker.domain.repository.CourseRepository
import com.example.shottracker.domain.repository.RoundRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import com.example.shottracker.core.dnd.DndController
import com.example.shottracker.core.prefs.AppPreferences
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NewRoundSetupUiState(
    val courses: List<Course> = emptyList(),
    val tees: List<Tee> = emptyList(),
    val selectedCourse: Course? = null,
    val selectedTee: Tee? = null,
    val courseName: String = "",
    val isCreatingCourse: Boolean = false,
    val numberOfHoles: Int = 18,
    val silenceDuringRound: Boolean = false,
    val needsDndPermission: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class NewRoundSetupViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val roundRepository: RoundRepository,
    private val prefs: AppPreferences,
    private val dndController: DndController
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        NewRoundSetupUiState(silenceDuringRound = prefs.silenceDuringRound)
    )
    val uiState: StateFlow<NewRoundSetupUiState> = _uiState.asStateFlow()

    init {
        loadCourses()
    }

    fun onSilenceDuringRoundChanged(enabled: Boolean) {
        prefs.silenceDuringRound = enabled
        val needsPerm = enabled && !dndController.hasPermission()
        _uiState.value = _uiState.value.copy(
            silenceDuringRound = enabled,
            needsDndPermission = needsPerm
        )
    }

    /** Re-check DND access (call on screen resume, e.g. returning from settings). */
    fun refreshDndPermission() {
        val needsPerm = _uiState.value.silenceDuringRound && !dndController.hasPermission()
        _uiState.value = _uiState.value.copy(needsDndPermission = needsPerm)
    }

    fun dismissDndPermissionPrompt() {
        _uiState.value = _uiState.value.copy(needsDndPermission = false)
    }

    fun dndSettingsIntent() = dndController.notificationPolicyAccessIntent()

    private fun loadCourses() {
        viewModelScope.launch {
            courseRepository.getAllCourses().collect { courses ->
                _uiState.value = _uiState.value.copy(courses = courses)
            }
        }
    }

    fun onCourseSelected(course: Course?) {
        _uiState.value = _uiState.value.copy(
            selectedCourse = course,
            selectedTee = null,
            isCreatingCourse = course == null
        )
        course?.let { loadTeesForCourse(it.id) }
    }

    fun onCreateNewCourse() {
        _uiState.value = _uiState.value.copy(
            selectedCourse = null,
            isCreatingCourse = true
        )
    }

    private fun loadTeesForCourse(courseId: Long) {
        viewModelScope.launch {
            courseRepository.getTeesForCourse(courseId).collect { tees ->
                // Default to the longest tee (max total distance) when none is selected yet.
                // Tees without a distance sort last; if no distances are set, the first tee wins.
                val current = _uiState.value.selectedTee
                val defaultTee = if (current == null) {
                    tees.maxByOrNull { it.totalDistance ?: Int.MIN_VALUE }
                } else current
                _uiState.value = _uiState.value.copy(tees = tees, selectedTee = defaultTee)
            }
        }
    }

    fun onTeeSelected(tee: Tee?) {
        _uiState.value = _uiState.value.copy(selectedTee = tee)
    }

    fun onCourseNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(courseName = name)
    }

    fun onNumberOfHolesChanged(holes: Int) {
        _uiState.value = _uiState.value.copy(numberOfHoles = holes)
    }

    fun startRound(onRoundStarted: (Long) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val resolvedCourseId: Long?
                val courseName = if (_uiState.value.isCreatingCourse) {
                    val name = _uiState.value.courseName.trim()
                    if (name.isEmpty()) {
                        _uiState.value = _uiState.value.copy(
                            error = "Please enter a course name",
                            isLoading = false
                        )
                        return@launch
                    }

                    // Create new course
                    val courseId = courseRepository.insertCourse(
                        Course(name = name)
                    )

                    // Create holes for the course
                    val holes = (1.._uiState.value.numberOfHoles).map { holeNum ->
                        HoleInfo(
                            courseId = courseId,
                            holeNumber = holeNum,
                            par = 4 // Default par
                        )
                    }
                    courseRepository.insertHoles(holes)

                    resolvedCourseId = courseId
                    name
                } else {
                    resolvedCourseId = _uiState.value.selectedCourse?.id
                    _uiState.value.selectedCourse?.name ?: "Unknown Course"
                }

                val handicapIndex = computeCurrentHandicapIndex()

                val roundId = roundRepository.startNewRound(
                    courseName = courseName,
                    teeId = _uiState.value.selectedTee?.id,
                    courseId = resolvedCourseId,
                    holeCount = _uiState.value.numberOfHoles,
                    handicapIndex = handicapIndex
                )

                // Enable Do Not Disturb for the round if the user opted in.
                if (prefs.silenceDuringRound) dndController.enableForRound()

                onRoundStarted(roundId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to start round: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private suspend fun computeCurrentHandicapIndex(): Double? {
        val rounds: List<Round> = roundRepository.getAllRounds().first()
        val differentials = mutableMapOf<Long, Double>()
        for (round in rounds) {
            val score = round.totalScore ?: continue
            val teeId = round.teeId ?: continue
            val tee = courseRepository.getTeeById(teeId) ?: continue
            val rating = tee.rating ?: continue
            val slope = tee.slope ?: continue
            val pcc = round.pcc ?: 0
            val adjusted = score - (round.totalAdjustment ?: 0)
            differentials[round.id] = (adjusted - rating - pcc) * 113.0 / slope
        }
        return HandicapCalculator.computeIndex(rounds, differentials)?.index
    }
}
