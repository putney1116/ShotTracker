package com.example.shottracker.feature.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shottracker.domain.model.Club
import com.example.shottracker.domain.model.HoleScore
import com.example.shottracker.domain.model.Round
import com.example.shottracker.domain.model.RoundStatus
import com.example.shottracker.domain.repository.ClubRepository
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

data class ClubStatistics(
    val club: Club,
    val averageDistance: Int?,
    val shotCount: Int
)

data class ParStats(
    val averageScore: Double? = null,
    val puttsPerHole: Double? = null,
    val girPerHole: Double? = null,
    val penaltiesPerHole: Double? = null,
    val holeCount: Int = 0
)

data class StatisticsUiState(
    // Filters
    val availableCourses: List<String> = emptyList(),
    val selectedCourse: String? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,

    // Round-level stats
    val totalRounds: Int = 0,
    val averageScore: Double? = null,
    val bestScore: Int? = null,
    val averageShotsPerRound: Double? = null,
    val averagePuttsPerRound: Double? = null,
    val averagePenaltiesPerRound: Double? = null,
    val averageGirPerRound: Double? = null,

    // Hole-level stats
    val averagePuttsPerHole: Double? = null,
    val averageGirPerHole: Double? = null,
    val averagePenaltiesPerHole: Double? = null,
    val par3Stats: ParStats = ParStats(),
    val par4Stats: ParStats = ParStats(),
    val par5Stats: ParStats = ParStats(),

    // Club distances (all-time, not filtered)
    val clubStatistics: List<ClubStatistics> = emptyList(),

    val isLoading: Boolean = true
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val roundRepository: RoundRepository,
    private val clubRepository: ClubRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    private var allRounds: List<Round> = emptyList()
    private var holeScoresByRoundId: Map<Long, List<HoleScore>> = emptyMap()

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            val rounds = roundRepository.getAllRounds().first()
                .filter { it.status == RoundStatus.COMPLETED }

            holeScoresByRoundId = rounds.associate { round ->
                round.id to roundRepository.getHoleScoresForRoundSync(round.id)
            }
            allRounds = rounds

            // Load club stats (all-time, unfiltered)
            val clubs = clubRepository.getAllClubsSync()
            val clubStats = clubs.map { club ->
                val avgDistance = roundRepository.getAverageDistanceForClub(club.id)
                val shots = roundRepository.getShotsForClub(club.id)
                ClubStatistics(
                    club = club,
                    averageDistance = avgDistance?.toInt(),
                    shotCount = shots.size
                )
            }.filter { it.shotCount > 0 }
                .sortedBy { it.club.displayOrder }

            val courses = rounds.map { it.courseName }.distinct().sorted()

            _uiState.value = _uiState.value.copy(
                availableCourses = courses,
                clubStatistics = clubStats,
                isLoading = false
            )
            recomputeStats()
        }
    }

    fun setSelectedCourse(course: String?) {
        _uiState.value = _uiState.value.copy(selectedCourse = course)
        recomputeStats()
    }

    fun setStartDate(date: LocalDate?) {
        _uiState.value = _uiState.value.copy(startDate = date)
        recomputeStats()
    }

    fun setEndDate(date: LocalDate?) {
        _uiState.value = _uiState.value.copy(endDate = date)
        recomputeStats()
    }

    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            selectedCourse = null,
            startDate = null,
            endDate = null
        )
        recomputeStats()
    }

    private fun recomputeStats() {
        val state = _uiState.value
        val zoneId = ZoneId.systemDefault()

        val filteredRounds = allRounds.filter { round ->
            val matchCourse = state.selectedCourse?.let { it == round.courseName } ?: true
            val roundDate = round.startTime.atZone(zoneId).toLocalDate()
            val afterStart = state.startDate?.let { !roundDate.isBefore(it) } ?: true
            val beforeEnd = state.endDate?.let { !roundDate.isAfter(it) } ?: true
            matchCourse && afterStart && beforeEnd
        }

        if (filteredRounds.isEmpty()) {
            _uiState.value = state.copy(
                totalRounds = 0,
                averageScore = null,
                bestScore = null,
                averageShotsPerRound = null,
                averagePuttsPerRound = null,
                averagePenaltiesPerRound = null,
                averageGirPerRound = null,
                averagePuttsPerHole = null,
                averageGirPerHole = null,
                averagePenaltiesPerHole = null,
                par3Stats = ParStats(),
                par4Stats = ParStats(),
                par5Stats = ParStats()
            )
            return
        }

        val allHoleScores = filteredRounds.flatMap { holeScoresByRoundId[it.id].orEmpty() }
        val completedHoles = allHoleScores.filter { it.score != null }

        val totalScores = filteredRounds.mapNotNull { it.totalScore }
        val averageScore = totalScores.takeIf { it.isNotEmpty() }?.average()
        val bestScore = totalScores.minOrNull()

        // Per-round per-round averages computed from filtered hole scores
        val perRoundShots = filteredRounds.map { round ->
            val holes = holeScoresByRoundId[round.id].orEmpty().filter { it.score != null }
            holes.sumOf { (it.score ?: 0) - (it.putts ?: 0) - (it.penalties ?: 0) }
        }
        val perRoundPutts = filteredRounds.map { round ->
            holeScoresByRoundId[round.id].orEmpty().sumOf { it.putts ?: 0 }
        }
        val perRoundPenalties = filteredRounds.map { round ->
            holeScoresByRoundId[round.id].orEmpty().sumOf { it.penalties ?: 0 }
        }
        val perRoundGir = filteredRounds.map { round ->
            holeScoresByRoundId[round.id].orEmpty().count { it.isGir == true }
        }

        val averageShotsPerRound = perRoundShots.takeIf { it.isNotEmpty() }?.average()
        val averagePuttsPerRound = perRoundPutts.takeIf { it.isNotEmpty() }?.average()
        val averagePenaltiesPerRound = perRoundPenalties.takeIf { it.isNotEmpty() }?.average()
        val averageGirPerRound = perRoundGir.takeIf { it.isNotEmpty() }?.average()

        // Treat missing putts/penalties on completed holes as 0
        val averagePuttsPerHole = completedHoles.takeIf { it.isNotEmpty() }
            ?.map { (it.putts ?: 0).toDouble() }?.average()

        val averagePenaltiesPerHole = completedHoles.takeIf { it.isNotEmpty() }
            ?.map { (it.penalties ?: 0).toDouble() }?.average()

        // GIR per hole: rate of completed holes with GIR achieved (0..1)
        val averageGirPerHole = completedHoles.takeIf { it.isNotEmpty() }?.let {
            it.count { hole -> hole.isGir == true }.toDouble() / it.size
        }

        _uiState.value = state.copy(
            totalRounds = filteredRounds.size,
            averageScore = averageScore,
            bestScore = bestScore,
            averageShotsPerRound = averageShotsPerRound,
            averagePuttsPerRound = averagePuttsPerRound,
            averagePenaltiesPerRound = averagePenaltiesPerRound,
            averageGirPerRound = averageGirPerRound,
            averagePuttsPerHole = averagePuttsPerHole,
            averageGirPerHole = averageGirPerHole,
            averagePenaltiesPerHole = averagePenaltiesPerHole,
            par3Stats = computeParStats(completedHoles, 3),
            par4Stats = computeParStats(completedHoles, 4),
            par5Stats = computeParStats(completedHoles, 5)
        )
    }

    private fun computeParStats(completedHoles: List<HoleScore>, par: Int): ParStats {
        val parHoles = completedHoles.filter { it.par == par }
        if (parHoles.isEmpty()) return ParStats()

        val averageScore = parHoles.mapNotNull { it.score }.takeIf { it.isNotEmpty() }?.average()

        // Treat missing putts/penalties on completed holes as 0
        val puttsPerHole = parHoles.map { (it.putts ?: 0).toDouble() }.average()
        val penaltiesPerHole = parHoles.map { (it.penalties ?: 0).toDouble() }.average()
        val girPerHole = parHoles.count { it.isGir == true }.toDouble() / parHoles.size

        return ParStats(
            averageScore = averageScore,
            puttsPerHole = puttsPerHole,
            girPerHole = girPerHole,
            penaltiesPerHole = penaltiesPerHole,
            holeCount = parHoles.size
        )
    }
}
