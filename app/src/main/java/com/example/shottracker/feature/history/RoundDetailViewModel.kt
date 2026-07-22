package com.example.shottracker.feature.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shottracker.domain.handicap.HandicapCalculator
import com.example.shottracker.domain.model.HoleScore
import com.example.shottracker.domain.model.Round
import com.example.shottracker.domain.model.Tee
import com.example.shottracker.domain.repository.CourseRepository
import com.example.shottracker.domain.repository.RoundRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class RoundDetailUiState(
    val round: Round? = null,
    val holeScores: List<HoleScore> = emptyList(),
    val tee: Tee? = null,
    val availableTees: List<Tee> = emptyList(),
    val isEditing: Boolean = false,
    val editingHole: HoleScore? = null,
    val editShots: Int? = null,
    val editPutts: Int? = null,
    val editPenalties: Int? = null,
    val editAdjustment: Int? = null,
    val holeHandicaps: Map<Int, Int> = emptyMap(),
    val isHandicapCounted: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class RoundDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val roundRepository: RoundRepository,
    private val courseRepository: CourseRepository
) : ViewModel() {

    private val roundId: Long = savedStateHandle.get<Long>("roundId") ?: 0L

    private val _uiState = MutableStateFlow(RoundDetailUiState())
    val uiState: StateFlow<RoundDetailUiState> = _uiState.asStateFlow()

    init {
        loadRound()
    }

    private fun loadRound() {
        viewModelScope.launch {
            val round = roundRepository.getRoundById(roundId)
            val holeScores = roundRepository.getHoleScoresForRoundSync(roundId)
            val tee = round?.teeId?.let { courseRepository.getTeeById(it) }

            // Find course id (via tee or by name) to load all tees for the course
            val courseId = tee?.courseId
                ?: round?.let { courseRepository.getCourseByName(it.courseName)?.id }
            val tees = courseId?.let { courseRepository.getTeesForCourse(it).first() }
                ?: emptyList()

            val handicaps = tee?.let { courseRepository.getHoleHandicaps(it.id) } ?: emptyMap()

            // Determine whether this round is one of the rounds counted toward the
            // current handicap index (matches the asterisk + primary color on HistoryScreen).
            val isCounted = computeIsCounted()

            _uiState.value = _uiState.value.copy(
                round = round,
                holeScores = holeScores.sortedBy { it.holeNumber },
                tee = tee,
                availableTees = tees,
                holeHandicaps = handicaps,
                isHandicapCounted = isCounted,
                isLoading = false
            )
        }
    }

    private suspend fun computeIsCounted(): Boolean {
        val allRounds = roundRepository.getAllRounds().first()
        val differentials = mutableMapOf<Long, Double>()
        for (r in allRounds) {
            val score = r.totalScore ?: continue
            val teeIdInner = r.teeId ?: continue
            val teeInner = courseRepository.getTeeById(teeIdInner) ?: continue
            val rating = teeInner.rating ?: continue
            val slope = teeInner.slope ?: continue
            val pcc = r.pcc ?: 0
            val adjusted = score - (r.totalAdjustment ?: 0)
            differentials[r.id] = (adjusted - rating - pcc) * 113.0 / slope
        }
        val result = HandicapCalculator.computeIndex(allRounds, differentials) ?: return false
        return roundId in result.countedRoundIds
    }

    val totalScore: Int
        get() = _uiState.value.holeScores.sumOf { it.score ?: 0 }

    val totalPar: Int
        get() = _uiState.value.holeScores.sumOf { it.par }

    val totalPutts: Int
        get() = _uiState.value.holeScores.sumOf { it.putts ?: 0 }

    val totalPenalties: Int
        get() = _uiState.value.holeScores.sumOf { it.penalties ?: 0 }

    val totalShots: Int
        get() = totalScore - totalPutts - totalPenalties

    val totalGir: Int
        get() = _uiState.value.holeScores.count { it.isGir == true }

    val scoreToPar: Int
        get() = _uiState.value.holeScores
            .filter { it.score != null }
            .sumOf { (it.score ?: 0) - it.par }

    val totalAdjustment: Int
        get() = _uiState.value.holeScores.sumOf { it.adjustment ?: 0 }

    val adjustedScore: Int
        get() = totalScore - totalAdjustment

    /**
     * Handicap differential = (adjustedScore - course rating - PCC) * 113 / slope
     * Uses the adjusted score (totalScore − totalAdjustment) per USGA / WHS rules.
     * Returns null if score, rating, or slope are missing.
     */
    val handicapDifferential: Double?
        get() {
            val tee = _uiState.value.tee ?: return null
            val rating = tee.rating ?: return null
            val slope = tee.slope ?: return null
            val score = totalScore
            if (score == 0) return null
            val pcc = _uiState.value.round?.pcc ?: 0
            val adjusted = score - totalAdjustment
            return (adjusted - rating - pcc) * 113.0 / slope
        }

    // Edit mode

    fun enterEditMode() {
        _uiState.value = _uiState.value.copy(isEditing = true)
    }

    fun exitEditMode() {
        _uiState.value = _uiState.value.copy(
            isEditing = false,
            editingHole = null,
            editShots = null,
            editPutts = null,
            editPenalties = null,
            editAdjustment = null
        )
    }

    fun setRoundDate(date: LocalDate) {
        val round = _uiState.value.round ?: return
        viewModelScope.launch {
            // Preserve time-of-day from existing startTime
            val zone = ZoneId.systemDefault()
            val existingTime = round.startTime.atZone(zone).toLocalTime()
            val newInstant = date.atTime(existingTime).atZone(zone).toInstant()
            roundRepository.updateRound(round.copy(startTime = newInstant))
            loadRound()
        }
    }

    fun setRoundTee(teeId: Long?) {
        val round = _uiState.value.round ?: return
        viewModelScope.launch {
            roundRepository.updateRound(round.copy(teeId = teeId))
            loadRound()
        }
    }

    fun setRoundPcc(pcc: Int?) {
        val round = _uiState.value.round ?: return
        val coerced = pcc?.coerceIn(-3, 3)
        viewModelScope.launch {
            roundRepository.updateRound(round.copy(pcc = coerced))
            loadRound()
        }
    }

    // Hole editing (mirrors ScorecardViewModel)

    fun startEditingHole(holeScore: HoleScore) {
        val shots = holeScore.score?.let { it - (holeScore.putts ?: 0) - (holeScore.penalties ?: 0) }
        _uiState.value = _uiState.value.copy(
            editingHole = holeScore,
            editShots = shots,
            editPutts = holeScore.putts,
            editPenalties = holeScore.penalties,
            editAdjustment = holeScore.adjustment
        )
    }

    fun onEditShotsChanged(shots: Int?) {
        _uiState.value = _uiState.value.copy(editShots = shots?.coerceIn(0, 20))
    }

    fun onEditPuttsChanged(putts: Int?) {
        _uiState.value = _uiState.value.copy(editPutts = putts?.coerceIn(0, 10))
    }

    fun onEditPenaltiesChanged(penalties: Int?) {
        _uiState.value = _uiState.value.copy(editPenalties = penalties?.coerceIn(0, 10))
    }

    fun incrementEditAdjustment() {
        val current = _uiState.value.editAdjustment ?: 0
        _uiState.value = _uiState.value.copy(
            editAdjustment = (current + 1).coerceIn(0, 10)
        )
    }

    fun decrementEditAdjustment() {
        val current = _uiState.value.editAdjustment ?: 0
        if (current <= 0) return
        _uiState.value = _uiState.value.copy(editAdjustment = current - 1)
    }

    fun saveHoleEdit() {
        viewModelScope.launch {
            val editingHole = _uiState.value.editingHole ?: return@launch
            val shots = _uiState.value.editShots ?: 0
            val putts = _uiState.value.editPutts ?: 0
            val penalties = _uiState.value.editPenalties ?: 0
            val computedScore = shots + putts + penalties
            val adjustment = _uiState.value.editAdjustment?.takeIf { it > 0 }

            roundRepository.updateHoleScoreStats(
                holeScoreId = editingHole.id,
                score = if (computedScore > 0) computedScore else null,
                putts = _uiState.value.editPutts?.takeIf { it > 0 },
                penalties = _uiState.value.editPenalties?.takeIf { it > 0 },
                adjustment = adjustment,
                fairwayHit = editingHole.fairwayHit,
                gir = editingHole.greenInRegulation
            )
            roundRepository.recalculateRoundTotals(roundId)
            cancelHoleEdit()
            loadRound()
        }
    }

    fun cancelHoleEdit() {
        _uiState.value = _uiState.value.copy(
            editingHole = null,
            editShots = null,
            editPutts = null,
            editPenalties = null,
            editAdjustment = null
        )
    }
}
