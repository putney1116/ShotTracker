package com.example.shottracker.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shottracker.domain.handicap.HandicapCalculator
import com.example.shottracker.domain.model.Round
import com.example.shottracker.domain.repository.CourseRepository
import com.example.shottracker.domain.repository.RoundRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val rounds: List<Round> = emptyList(),
    val differentialsById: Map<Long, Double> = emptyMap(),
    val handicapIndex: Double? = null,
    val handicapEligibleRoundCount: Int = 0,
    val handicapCountedRoundIds: Set<Long> = emptySet(),
    val handicapFormulaDescription: String? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val roundRepository: RoundRepository,
    private val courseRepository: CourseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadRounds()
    }

    private fun loadRounds() {
        viewModelScope.launch {
            roundRepository.getAllRounds().collect { rounds ->
                val differentials = computeDifferentials(rounds)
                val result = HandicapCalculator.computeIndex(rounds, differentials)
                _uiState.value = _uiState.value.copy(
                    rounds = rounds,
                    differentialsById = differentials,
                    handicapIndex = result?.index,
                    handicapEligibleRoundCount = result?.eligibleRoundCount
                        ?: differentials.size,
                    handicapCountedRoundIds = result?.countedRoundIds ?: emptySet(),
                    handicapFormulaDescription = result?.formulaDescription,
                    isLoading = false
                )
            }
        }
    }

    private suspend fun computeDifferentials(rounds: List<Round>): Map<Long, Double> {
        val out = mutableMapOf<Long, Double>()
        for (round in rounds) {
            val score = round.totalScore ?: continue
            val teeId = round.teeId ?: continue
            val tee = courseRepository.getTeeById(teeId) ?: continue
            val rating = tee.rating ?: continue
            val slope = tee.slope ?: continue
            val pcc = round.pcc ?: 0
            val adjusted = score - (round.totalAdjustment ?: 0)
            out[round.id] = (adjusted - rating - pcc) * 113.0 / slope
        }
        return out
    }

    fun deleteRound(roundId: Long) {
        viewModelScope.launch {
            roundRepository.deleteRound(roundId)
        }
    }
}
