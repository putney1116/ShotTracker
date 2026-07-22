package com.example.shottracker.feature.createcourse

import com.example.shottracker.feature.coursemanagement.TeeFormState

sealed class WizardStep {
    data object Name : WizardStep()
    data object HoleCount : WizardStep()
    data object Pars : WizardStep()
    data object Greens : WizardStep()
    data object Tees : WizardStep()
    data object Review : WizardStep()
}

enum class GreenTarget { Front, Center, Back }

data class GreenDraft(
    val frontLat: Double? = null, val frontLng: Double? = null,
    val centerLat: Double? = null, val centerLng: Double? = null,
    val backLat: Double? = null,  val backLng: Double? = null,
) {
    val isComplete: Boolean
        get() = frontLat != null && centerLat != null && backLat != null

    fun firstUnfilledTarget(): GreenTarget? = when {
        frontLat == null -> GreenTarget.Front
        centerLat == null -> GreenTarget.Center
        backLat == null -> GreenTarget.Back
        else -> null
    }

    fun set(target: GreenTarget, lat: Double, lng: Double): GreenDraft = when (target) {
        GreenTarget.Front  -> copy(frontLat = lat, frontLng = lng)
        GreenTarget.Center -> copy(centerLat = lat, centerLng = lng)
        GreenTarget.Back   -> copy(backLat = lat, backLng = lng)
    }

    fun clear(target: GreenTarget): GreenDraft = when (target) {
        GreenTarget.Front  -> copy(frontLat = null, frontLng = null)
        GreenTarget.Center -> copy(centerLat = null, centerLng = null)
        GreenTarget.Back   -> copy(backLat = null, backLng = null)
    }

    fun isPlaced(target: GreenTarget): Boolean = when (target) {
        GreenTarget.Front  -> frontLat != null
        GreenTarget.Center -> centerLat != null
        GreenTarget.Back   -> backLat != null
    }
}

data class TeeDraft(
    val name: String,
    val color: String?,
    val rating: Double?,
    val slope: Int?,
    val totalDistance: Int? = null,
)

data class CreateCourseUiState(
    val step: WizardStep = WizardStep.Name,
    val name: String = "",
    val city: String = "",
    val state: String = "",
    val nameError: String? = null,
    val holeCount: Int = 18,
    val pars: List<Int> = List(18) { 4 },
    val greens: List<GreenDraft> = List(18) { GreenDraft() },
    val tees: List<TeeDraft> = emptyList(),
    val teesError: String? = null,
    val teeForm: TeeFormState? = null,
    val currentGreenHole: Int = 1,
    val selectedGreenTarget: GreenTarget = GreenTarget.Front,
    val showDiscardDialog: Boolean = false,
    val isSaving: Boolean = false,
    val saveError: String? = null,
)

sealed class CreateCourseEvent {
    data object Saved : CreateCourseEvent()
}
