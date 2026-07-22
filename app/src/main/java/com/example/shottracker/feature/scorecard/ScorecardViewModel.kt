package com.example.shottracker.feature.scorecard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shottracker.domain.model.HoleScore
import com.example.shottracker.domain.model.Round
import com.example.shottracker.domain.repository.CourseRepository
import com.example.shottracker.domain.repository.RoundRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScorecardUiState(
    val round: Round? = null,
    val holeScores: List<HoleScore> = emptyList(),
    val editingHole: HoleScore? = null,
    val editShots: Int? = null,
    val editPutts: Int? = null,
    val editPenalties: Int? = null,
    val editAdjustment: Int? = null,
    val holeHandicaps: Map<Int, Int> = emptyMap(),
    val isLoading: Boolean = true
)

@HiltViewModel
class ScorecardViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val roundRepository: RoundRepository,
    private val courseRepository: CourseRepository
) : ViewModel() {

    private val roundId: Long = savedStateHandle.get<Long>("roundId") ?: 0L
    private var loadedHandicapsForTeeId: Long? = null

    private val _uiState = MutableStateFlow(ScorecardUiState())
    val uiState: StateFlow<ScorecardUiState> = _uiState.asStateFlow()

    init {
        loadRound()
        loadHoleScores()
    }

    private fun loadRound() {
        viewModelScope.launch {
            roundRepository.getRoundByIdFlow(roundId).collect { round ->
                _uiState.value = _uiState.value.copy(round = round)
                val teeId = round?.teeId
                if (teeId != null && teeId != loadedHandicapsForTeeId) {
                    loadedHandicapsForTeeId = teeId
                    val map = courseRepository.getHoleHandicaps(teeId)
                    _uiState.value = _uiState.value.copy(holeHandicaps = map)
                }
            }
        }
    }

    private fun loadHoleScores() {
        viewModelScope.launch {
            roundRepository.getHoleScoresForRound(roundId).collect { scores ->
                _uiState.value = _uiState.value.copy(
                    holeScores = scores.sortedBy { it.holeNumber },
                    isLoading = false
                )
            }
        }
    }

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

            cancelEdit()
        }
    }

    fun cancelEdit() {
        _uiState.value = _uiState.value.copy(
            editingHole = null,
            editShots = null,
            editPutts = null,
            editPenalties = null,
            editAdjustment = null
        )
    }

    val totalScore: Int
        get() = _uiState.value.holeScores.sumOf { it.score ?: 0 }

    val totalPar: Int
        get() = _uiState.value.holeScores.sumOf { it.par }

    val totalPutts: Int
        get() = _uiState.value.holeScores.sumOf { it.putts ?: 0 }

    val totalPenalties: Int
        get() = _uiState.value.holeScores.sumOf { it.penalties ?: 0 }

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
}
