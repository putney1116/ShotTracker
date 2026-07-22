package com.example.shottracker.feature.coursemanagement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shottracker.domain.model.Course
import com.example.shottracker.domain.model.HoleInfo
import com.example.shottracker.domain.model.Tee
import com.example.shottracker.domain.repository.CourseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TeeFormState(
    val teeId: Long = 0,
    val name: String = "",
    val color: String = "",
    val rating: String = "",
    val slope: String = "",
    val distance: String = "",
    val error: String? = null
)

data class CourseManagementUiState(
    val courses: List<Course> = emptyList(),
    val courseToDelete: Course? = null,
    val editingCourse: Course? = null,
    val editingHoles: List<HoleInfo> = emptyList(),
    val editingTees: List<Tee> = emptyList(),
    val teeForm: TeeFormState? = null,
    val editingHandicapsTeeId: Long? = null,
    val handicapEdits: Map<Int, String> = emptyMap(),
    val handicapError: String? = null,
    val isSaving: Boolean = false
)

@HiltViewModel
class CourseManagementViewModel @Inject constructor(
    private val courseRepository: CourseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CourseManagementUiState())
    val uiState: StateFlow<CourseManagementUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            courseRepository.getAllCourses().collect { courses ->
                _uiState.value = _uiState.value.copy(courses = courses)
            }
        }
    }

    fun confirmDelete(course: Course) {
        _uiState.value = _uiState.value.copy(courseToDelete = course)
    }

    fun dismissDelete() {
        _uiState.value = _uiState.value.copy(courseToDelete = null)
    }

    fun deleteCourse() {
        val course = _uiState.value.courseToDelete ?: return
        viewModelScope.launch {
            courseRepository.deleteCourse(course.id)
            _uiState.value = _uiState.value.copy(courseToDelete = null)
        }
    }

    fun selectCourse(course: Course) {
        viewModelScope.launch {
            val existing = courseRepository.getHolesForCourse(course.id).first()
            // Ensure all 18 holes are represented (fill any missing with par 4)
            val byNumber = existing.associateBy { it.holeNumber }
            val full = (1..18).map { num ->
                byNumber[num] ?: HoleInfo(courseId = course.id, holeNumber = num, par = 4)
            }
            val tees = courseRepository.getTeesForCourse(course.id).first()
            _uiState.value = _uiState.value.copy(
                editingCourse = course,
                editingHoles = full,
                editingTees = tees
            )
        }
    }

    fun closeCourseEditor() {
        _uiState.value = _uiState.value.copy(
            editingCourse = null,
            editingHoles = emptyList(),
            editingTees = emptyList(),
            teeForm = null
        )
    }

    fun updateHolePar(holeNumber: Int, par: Int) {
        val coerced = par.coerceIn(3, 6)
        _uiState.value = _uiState.value.copy(
            editingHoles = _uiState.value.editingHoles.map { hole ->
                if (hole.holeNumber == holeNumber) hole.copy(par = coerced) else hole
            }
        )
    }

    fun saveHoleEdits() {
        val holes = _uiState.value.editingHoles
        if (holes.isEmpty()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            // Use updateHole for existing holes (id != 0). insertHoles uses REPLACE which
            // would DELETE + INSERT existing rows, triggering FK CASCADE on tee_hole_info
            // and wiping any stored per-tee handicaps. Update-in-place leaves the row alive.
            val (existing, fresh) = holes.partition { it.id != 0L }
            existing.forEach { courseRepository.updateHole(it) }
            if (fresh.isNotEmpty()) {
                courseRepository.insertHoles(fresh)
            }
            _uiState.value = _uiState.value.copy(
                isSaving = false,
                editingCourse = null,
                editingHoles = emptyList(),
                editingTees = emptyList(),
                teeForm = null
            )
        }
    }

    // Tee management

    fun openNewTeeForm() {
        _uiState.value = _uiState.value.copy(teeForm = TeeFormState())
    }

    fun openEditTeeForm(tee: Tee) {
        _uiState.value = _uiState.value.copy(
            teeForm = TeeFormState(
                teeId = tee.id,
                name = tee.name,
                color = tee.color.orEmpty(),
                rating = tee.rating?.toString().orEmpty(),
                slope = tee.slope?.toString().orEmpty(),
                distance = tee.totalDistance?.toString().orEmpty()
            )
        )
    }

    fun cancelTeeForm() {
        _uiState.value = _uiState.value.copy(teeForm = null)
    }

    fun updateTeeFormName(value: String) {
        _uiState.value.teeForm?.let {
            _uiState.value = _uiState.value.copy(teeForm = it.copy(name = value, error = null))
        }
    }

    fun updateTeeFormColor(value: String) {
        _uiState.value.teeForm?.let {
            _uiState.value = _uiState.value.copy(teeForm = it.copy(color = value))
        }
    }

    fun updateTeeFormRating(value: String) {
        _uiState.value.teeForm?.let {
            _uiState.value = _uiState.value.copy(teeForm = it.copy(rating = value, error = null))
        }
    }

    fun updateTeeFormSlope(value: String) {
        _uiState.value.teeForm?.let {
            _uiState.value = _uiState.value.copy(teeForm = it.copy(slope = value, error = null))
        }
    }

    fun updateTeeFormDistance(value: String) {
        _uiState.value.teeForm?.let {
            _uiState.value = _uiState.value.copy(teeForm = it.copy(distance = value, error = null))
        }
    }

    fun saveTeeForm() {
        val form = _uiState.value.teeForm ?: return
        val course = _uiState.value.editingCourse ?: return

        val name = form.name.trim()
        if (name.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                teeForm = form.copy(error = "Name is required")
            )
            return
        }

        val ratingValue: Double? = form.rating.trim().takeIf { it.isNotEmpty() }?.toDoubleOrNull()
        if (form.rating.trim().isNotEmpty() && ratingValue == null) {
            _uiState.value = _uiState.value.copy(
                teeForm = form.copy(error = "Rating must be a number (e.g., 71.5)")
            )
            return
        }
        val slopeValue: Int? = form.slope.trim().takeIf { it.isNotEmpty() }?.toIntOrNull()
        if (form.slope.trim().isNotEmpty() && slopeValue == null) {
            _uiState.value = _uiState.value.copy(
                teeForm = form.copy(error = "Slope must be an integer (e.g., 132)")
            )
            return
        }
        val distanceValue: Int? = form.distance.trim().takeIf { it.isNotEmpty() }?.toIntOrNull()
        if (form.distance.trim().isNotEmpty() && (distanceValue == null || distanceValue !in 1..10000)) {
            _uiState.value = _uiState.value.copy(
                teeForm = form.copy(error = "Distance must be a positive integer (yards)")
            )
            return
        }

        viewModelScope.launch {
            val tee = Tee(
                id = form.teeId,
                courseId = course.id,
                name = name,
                color = form.color.trim().takeIf { it.isNotEmpty() },
                rating = ratingValue,
                slope = slopeValue,
                totalDistance = distanceValue
            )
            // updateTee for existing (id != 0); insertTee for new. insertTee uses REPLACE,
            // which would DELETE + INSERT existing tees, cascading to wipe tee_hole_info
            // (the per-hole handicaps).
            if (tee.id != 0L) {
                courseRepository.updateTee(tee)
            } else {
                courseRepository.insertTee(tee)
            }
            val refreshed = courseRepository.getTeesForCourse(course.id).first()
            _uiState.value = _uiState.value.copy(
                editingTees = refreshed,
                teeForm = null
            )
        }
    }

    fun deleteTee(teeId: Long) {
        val course = _uiState.value.editingCourse ?: return
        viewModelScope.launch {
            courseRepository.deleteTee(teeId)
            val refreshed = courseRepository.getTeesForCourse(course.id).first()
            _uiState.value = _uiState.value.copy(editingTees = refreshed)
        }
    }

    // Per-hole handicap editor

    fun openHandicapEditor(teeId: Long) {
        viewModelScope.launch {
            val existing = courseRepository.getHoleHandicaps(teeId)
            val holes = _uiState.value.editingHoles
            val edits = holes.associate { hole ->
                hole.holeNumber to (existing[hole.holeNumber]?.toString().orEmpty())
            }
            _uiState.value = _uiState.value.copy(
                editingHandicapsTeeId = teeId,
                handicapEdits = edits,
                handicapError = null
            )
        }
    }

    fun cancelHandicapEditor() {
        _uiState.value = _uiState.value.copy(
            editingHandicapsTeeId = null,
            handicapEdits = emptyMap(),
            handicapError = null
        )
    }

    fun updateHandicapEntry(holeNumber: Int, value: String) {
        val sanitized = value.filter { it.isDigit() }.take(2)
        _uiState.value = _uiState.value.copy(
            handicapEdits = _uiState.value.handicapEdits + (holeNumber to sanitized),
            handicapError = null
        )
    }

    fun clearAllHandicapEdits() {
        val cleared = _uiState.value.handicapEdits.mapValues { "" }
        _uiState.value = _uiState.value.copy(
            handicapEdits = cleared,
            handicapError = null
        )
    }

    fun saveHandicapEdits() {
        val teeId = _uiState.value.editingHandicapsTeeId ?: return
        val edits = _uiState.value.handicapEdits
        val holeCount = _uiState.value.editingHoles.size

        val nonBlank = edits.filterValues { it.isNotBlank() }
        if (nonBlank.isEmpty()) {
            viewModelScope.launch {
                courseRepository.setHoleHandicaps(teeId, emptyMap())
                _uiState.value = _uiState.value.copy(
                    editingHandicapsTeeId = null,
                    handicapEdits = emptyMap(),
                    handicapError = null
                )
            }
            return
        }
        if (nonBlank.size != edits.size) {
            _uiState.value = _uiState.value.copy(
                handicapError = "Either fill every hole or leave them all blank."
            )
            return
        }

        val parsed = edits.mapValues { it.value.toIntOrNull() }
        if (parsed.any { it.value == null }) {
            _uiState.value = _uiState.value.copy(handicapError = "Values must be integers.")
            return
        }
        val values = parsed.values.filterNotNull()
        if (values.any { it < 1 || it > holeCount }) {
            _uiState.value = _uiState.value.copy(
                handicapError = "Values must be between 1 and $holeCount."
            )
            return
        }
        if (values.toSet().size != values.size) {
            _uiState.value = _uiState.value.copy(handicapError = "Values must be unique.")
            return
        }

        val map = parsed.mapNotNull { (k, v) -> v?.let { k to it } }.toMap()
        viewModelScope.launch {
            courseRepository.setHoleHandicaps(teeId, map)
            _uiState.value = _uiState.value.copy(
                editingHandicapsTeeId = null,
                handicapEdits = emptyMap(),
                handicapError = null
            )
        }
    }
}
