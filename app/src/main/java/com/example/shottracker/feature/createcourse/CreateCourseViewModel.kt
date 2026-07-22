package com.example.shottracker.feature.createcourse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shottracker.domain.model.Course
import com.example.shottracker.domain.model.HoleInfo
import com.example.shottracker.domain.model.Tee
import com.example.shottracker.domain.repository.CourseRepository
import com.example.shottracker.feature.coursemanagement.TeeFormState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateCourseViewModel @Inject constructor(
    private val repo: CourseRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateCourseUiState())
    val uiState: StateFlow<CreateCourseUiState> = _uiState.asStateFlow()

    private val _events = Channel<CreateCourseEvent>(capacity = Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun setName(value: String) {
        _uiState.update { it.copy(name = value, nameError = null) }
    }

    fun setCity(value: String) {
        _uiState.update { it.copy(city = value) }
    }

    fun setState(value: String) {
        _uiState.update { it.copy(state = value) }
    }

    fun setHoleCount(count: Int) {
        require(count == 9 || count == 18) { "Hole count must be 9 or 18" }
        _uiState.update { s ->
            val newPars = if (s.pars.size == count) s.pars
                          else if (count < s.pars.size) s.pars.take(count)
                          else s.pars + List(count - s.pars.size) { 4 }
            val newGreens = if (s.greens.size == count) s.greens
                            else if (count < s.greens.size) s.greens.take(count)
                            else s.greens + List(count - s.greens.size) { GreenDraft() }
            s.copy(holeCount = count, pars = newPars, greens = newGreens)
        }
    }

    fun updateHolePar(holeNumber: Int, par: Int) {
        val coerced = par.coerceIn(3, 6)
        _uiState.update { s ->
            if (holeNumber < 1 || holeNumber > s.pars.size) s
            else s.copy(pars = s.pars.mapIndexed { i, p ->
                if (i == holeNumber - 1) coerced else p
            })
        }
    }

    fun placeGreen(lat: Double, lng: Double) {
        _uiState.update { s ->
            if (s.step != WizardStep.Greens) return@update s
            val idx = s.currentGreenHole - 1
            val updated = s.greens[idx].set(s.selectedGreenTarget, lat, lng)
            val newGreens = s.greens.toMutableList().also { it[idx] = updated }
            val nextTarget = updated.firstUnfilledTarget() ?: GreenTarget.Back
            s.copy(greens = newGreens, selectedGreenTarget = nextTarget)
        }
    }

    fun selectTarget(target: GreenTarget) {
        _uiState.update { s ->
            if (s.step != WizardStep.Greens) return@update s
            val idx = s.currentGreenHole - 1
            val hole = s.greens[idx]
            if (hole.isPlaced(target)) {
                val cleared = hole.clear(target)
                val newGreens = s.greens.toMutableList().also { it[idx] = cleared }
                s.copy(greens = newGreens, selectedGreenTarget = target)
            } else {
                s.copy(selectedGreenTarget = target)
            }
        }
    }

    fun onNext() {
        val state = _uiState.value
        when (state.step) {
            WizardStep.Name -> validateAndAdvanceFromName(state)
            WizardStep.HoleCount -> _uiState.update { it.copy(step = WizardStep.Pars) }
            WizardStep.Pars -> _uiState.update {
                it.copy(step = WizardStep.Greens, currentGreenHole = 1,
                        selectedGreenTarget = it.greens[0].firstUnfilledTarget() ?: GreenTarget.Front)
            }
            WizardStep.Greens -> {
                val idx = state.currentGreenHole - 1
                if (!state.greens[idx].isComplete) return
                if (state.currentGreenHole < state.holeCount) {
                    val nextHole = state.currentGreenHole + 1
                    val nextDraft = state.greens[nextHole - 1]
                    _uiState.update { it.copy(
                        currentGreenHole = nextHole,
                        selectedGreenTarget = nextDraft.firstUnfilledTarget() ?: GreenTarget.Front,
                    ) }
                } else {
                    _uiState.update { it.copy(step = WizardStep.Tees) }
                }
            }
            WizardStep.Tees -> {
                if (state.tees.isEmpty()) {
                    _uiState.update { it.copy(teesError = "Add at least one tee") }
                } else {
                    _uiState.update { it.copy(step = WizardStep.Review, teesError = null) }
                }
            }
            else -> Unit
        }
    }

    fun openNewTeeForm() {
        _uiState.update { it.copy(teeForm = TeeFormState()) }
    }

    fun cancelTeeForm() {
        _uiState.update { it.copy(teeForm = null) }
    }

    fun updateTeeFormName(value: String) {
        _uiState.update { s ->
            s.teeForm?.let { s.copy(teeForm = it.copy(name = value, error = null)) } ?: s
        }
    }

    fun updateTeeFormColor(value: String) {
        _uiState.update { s ->
            s.teeForm?.let { s.copy(teeForm = it.copy(color = value)) } ?: s
        }
    }

    fun updateTeeFormRating(value: String) {
        _uiState.update { s ->
            s.teeForm?.let { s.copy(teeForm = it.copy(rating = value, error = null)) } ?: s
        }
    }

    fun updateTeeFormSlope(value: String) {
        _uiState.update { s ->
            s.teeForm?.let { s.copy(teeForm = it.copy(slope = value, error = null)) } ?: s
        }
    }

    fun updateTeeFormDistance(value: String) {
        _uiState.update { s ->
            s.teeForm?.let { s.copy(teeForm = it.copy(distance = value, error = null)) } ?: s
        }
    }

    fun saveTeeForm() {
        val s = _uiState.value
        val form = s.teeForm ?: return

        val name = form.name.trim()
        if (name.isEmpty()) {
            _uiState.update { it.copy(teeForm = form.copy(error = "Name is required")) }
            return
        }
        if (s.tees.any { it.name.equals(name, ignoreCase = true) }) {
            _uiState.update { it.copy(teeForm = form.copy(error = "A tee with this name already exists")) }
            return
        }
        val ratingText = form.rating.trim()
        val rating: Double? = if (ratingText.isEmpty()) null else ratingText.toDoubleOrNull()
        if (ratingText.isNotEmpty() && (rating == null || rating !in 60.0..80.0)) {
            _uiState.update { it.copy(teeForm = form.copy(
                error = "Rating must be a number between 60.0 and 80.0")) }
            return
        }
        val slopeText = form.slope.trim()
        val slope: Int? = if (slopeText.isEmpty()) null else slopeText.toIntOrNull()
        if (slopeText.isNotEmpty() && (slope == null || slope !in 55..155)) {
            _uiState.update { it.copy(teeForm = form.copy(
                error = "Slope must be an integer between 55 and 155")) }
            return
        }
        val distanceText = form.distance.trim()
        val distance: Int? = if (distanceText.isEmpty()) null else distanceText.toIntOrNull()
        if (distanceText.isNotEmpty() && (distance == null || distance !in 1..10000)) {
            _uiState.update { it.copy(teeForm = form.copy(
                error = "Distance must be a positive integer (yards)")) }
            return
        }

        val draft = TeeDraft(
            name = name,
            color = form.color.trim().takeIf { it.isNotEmpty() },
            rating = rating,
            slope = slope,
            totalDistance = distance,
        )
        _uiState.update { it.copy(tees = it.tees + draft, teeForm = null, teesError = null) }
    }

    fun deleteTeeDraft(name: String) {
        _uiState.update { it.copy(tees = it.tees.filterNot { t -> t.name == name }) }
    }

    fun onBack() {
        val s = _uiState.value
        when (s.step) {
            WizardStep.Name -> _uiState.update { it.copy(showDiscardDialog = true) }
            WizardStep.HoleCount -> _uiState.update { it.copy(step = WizardStep.Name) }
            WizardStep.Pars -> _uiState.update { it.copy(step = WizardStep.HoleCount) }
            WizardStep.Greens -> {
                if (s.currentGreenHole > 1) {
                    val prev = s.currentGreenHole - 1
                    val prevDraft = s.greens[prev - 1]
                    _uiState.update { it.copy(
                        currentGreenHole = prev,
                        selectedGreenTarget = prevDraft.firstUnfilledTarget() ?: GreenTarget.Front,
                    ) }
                } else {
                    _uiState.update { it.copy(step = WizardStep.Pars) }
                }
            }
            WizardStep.Tees -> {
                val lastHole = s.greens[s.holeCount - 1]
                _uiState.update { it.copy(
                    step = WizardStep.Greens,
                    currentGreenHole = s.holeCount,
                    selectedGreenTarget = lastHole.firstUnfilledTarget() ?: GreenTarget.Front,
                ) }
            }
            WizardStep.Review -> _uiState.update { it.copy(step = WizardStep.Tees) }
        }
    }

    fun onSave() {
        val s = _uiState.value
        if (s.step != WizardStep.Review || s.isSaving) return

        _uiState.update { it.copy(isSaving = true, saveError = null) }
        viewModelScope.launch {
            val course = Course(
                name = s.name.trim(),
                city = s.city.trim().ifBlank { null },
                state = s.state.trim().ifBlank { null },
            )
            val holes = (1..s.holeCount).map { n ->
                val g = s.greens[n - 1]
                HoleInfo(
                    courseId = 0, holeNumber = n, par = s.pars[n - 1],
                    greenFrontLat = g.frontLat,  greenFrontLng = g.frontLng,
                    greenCenterLat = g.centerLat, greenCenterLng = g.centerLng,
                    greenBackLat = g.backLat,    greenBackLng = g.backLng,
                )
            }
            val tees = s.tees.map { d ->
                Tee(
                    courseId = 0, name = d.name, color = d.color,
                    rating = d.rating, slope = d.slope, totalDistance = d.totalDistance,
                )
            }
            runCatching { repo.createCourse(course, holes, tees) }
                .onSuccess {
                    _events.send(CreateCourseEvent.Saved)
                    // Leave isSaving = true; the screen will navigate away
                }
                .onFailure { e ->
                    _uiState.update { it.copy(
                        isSaving = false,
                        saveError = e.message ?: "Save failed",
                    ) }
                }
        }
    }

    fun dismissDiscardDialog() {
        _uiState.update { it.copy(showDiscardDialog = false) }
    }

    fun clearSaveError() {
        _uiState.update { it.copy(saveError = null) }
    }

    private fun validateAndAdvanceFromName(state: CreateCourseUiState) {
        val trimmed = state.name.trim()
        if (trimmed.isEmpty() || trimmed.length > 80) {
            _uiState.update { it.copy(nameError = "Name must be 1–80 characters") }
            return
        }
        viewModelScope.launch {
            val existing = repo.getCourseByName(trimmed)
            if (existing != null) {
                _uiState.update { it.copy(nameError = "A course with this name already exists") }
            } else {
                _uiState.update { it.copy(step = WizardStep.HoleCount, nameError = null) }
            }
        }
    }
}
