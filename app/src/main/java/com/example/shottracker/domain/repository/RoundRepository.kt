package com.example.shottracker.domain.repository

import com.example.shottracker.domain.model.HoleScore
import com.example.shottracker.domain.model.Round
import com.example.shottracker.domain.model.RoundStatus
import com.example.shottracker.domain.model.Shot
import kotlinx.coroutines.flow.Flow

interface RoundRepository {
    fun getAllRounds(): Flow<List<Round>>
    fun getActiveRound(): Flow<Round?>
    suspend fun getActiveRoundSync(): Round?
    suspend fun getRoundById(roundId: Long): Round?
    fun getRoundByIdFlow(roundId: Long): Flow<Round?>
    fun getRecentCompletedRounds(limit: Int): Flow<List<Round>>

    suspend fun startNewRound(
        courseName: String,
        teeId: Long?,
        courseId: Long?,
        holeCount: Int,
        handicapIndex: Double?
    ): Long
    suspend fun updateRound(round: Round)
    suspend fun updateRoundStatus(roundId: Long, status: RoundStatus)
    suspend fun deleteRound(roundId: Long)

    // Hole scores
    fun getHoleScoresForRound(roundId: Long): Flow<List<HoleScore>>
    suspend fun getHoleScoresForRoundSync(roundId: Long): List<HoleScore>
    suspend fun getHoleScore(roundId: Long, holeNumber: Int): HoleScore?
    fun getHoleScoreFlow(roundId: Long, holeNumber: Int): Flow<HoleScore?>
    suspend fun createHoleScore(holeScore: HoleScore): Long
    suspend fun updateHoleScore(holeScore: HoleScore)
    suspend fun updateHoleScoreStats(
        holeScoreId: Long,
        score: Int?,
        putts: Int?,
        penalties: Int?,
        adjustment: Int?,
        fairwayHit: Boolean?,
        gir: Boolean?
    )

    /**
     * Recalculates and persists the round's aggregate stats (totalScore, totalPutts,
     * totalPenalties, totalAdjustment, holesPlayed) from current hole_scores rows.
     * Used after edits to a completed round so the History card reflects the new totals.
     */
    suspend fun recalculateRoundTotals(roundId: Long)

    // Shots
    fun getShotsForHole(holeScoreId: Long): Flow<List<Shot>>
    suspend fun getShotsForHoleSync(holeScoreId: Long): List<Shot>
    suspend fun recordShot(shot: Shot): Long
    suspend fun deleteShot(shotId: Long)
    suspend fun updateShotDistance(shotId: Long, distance: Int)
    suspend fun getLastShot(holeScoreId: Long): Shot?
    suspend fun getAllShotsForRound(roundId: Long): List<Shot>

    // Statistics
    suspend fun getAverageDistanceForClub(clubId: Long): Double?
    suspend fun getShotsForClub(clubId: Long): List<Shot>
}
