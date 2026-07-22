package com.example.shottracker.data.repository

import com.example.shottracker.data.local.dao.ClubDao
import com.example.shottracker.data.local.dao.HoleInfoDao
import com.example.shottracker.data.local.dao.HoleScoreDao
import com.example.shottracker.data.local.dao.RoundDao
import com.example.shottracker.data.local.dao.ShotDao
import com.example.shottracker.data.local.entity.RoundEntity
import com.example.shottracker.data.mapper.toDomain
import com.example.shottracker.data.mapper.toEntity
import com.example.shottracker.domain.model.HoleScore
import com.example.shottracker.domain.model.Round
import com.example.shottracker.domain.model.RoundStatus
import com.example.shottracker.domain.model.Shot
import com.example.shottracker.domain.repository.RoundRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoundRepositoryImpl @Inject constructor(
    private val roundDao: RoundDao,
    private val holeScoreDao: HoleScoreDao,
    private val shotDao: ShotDao,
    private val clubDao: ClubDao,
    private val holeInfoDao: HoleInfoDao
) : RoundRepository {

    override fun getAllRounds(): Flow<List<Round>> {
        return roundDao.getAllRounds().map { rounds ->
            rounds.map { it.toDomain() }
        }
    }

    override fun getActiveRound(): Flow<Round?> {
        return roundDao.getActiveRound().map { round ->
            round?.toDomain()
        }
    }

    override suspend fun getActiveRoundSync(): Round? {
        return roundDao.getActiveRoundSync()?.toDomain()
    }

    override suspend fun getRoundById(roundId: Long): Round? {
        val round = roundDao.getRoundById(roundId) ?: return null
        val holeScores = holeScoreDao.getHoleScoresForRoundSync(roundId).map { holeScore ->
            val shots = shotDao.getShotsForHoleSync(holeScore.holeScoreId).map { shot ->
                val clubName = shot.clubId?.let { clubDao.getClubById(it)?.name }
                shot.toDomain(clubName)
            }
            holeScore.toDomain(shots)
        }
        return round.toDomain(holeScores)
    }

    override fun getRoundByIdFlow(roundId: Long): Flow<Round?> {
        return roundDao.getRoundByIdFlow(roundId).map { round ->
            round?.toDomain()
        }
    }

    override fun getRecentCompletedRounds(limit: Int): Flow<List<Round>> {
        return roundDao.getRecentCompletedRounds(limit).map { rounds ->
            rounds.map { it.toDomain() }
        }
    }

    override suspend fun startNewRound(
        courseName: String,
        teeId: Long?,
        courseId: Long?,
        holeCount: Int,
        handicapIndex: Double?
    ): Long {
        val round = RoundEntity(
            teeId = teeId,
            courseName = courseName,
            startTime = Instant.now().toEpochMilli(),
            endTime = null,
            status = RoundStatus.IN_PROGRESS.name,
            holesPlayed = 0,
            totalScore = null,
            totalPutts = null,
            totalPenalties = null,
            pcc = null,
            handicapIndex = handicapIndex
        )
        val newRoundId = roundDao.insertRound(round)

        // Pre-create all hole scores so the scorecard shows every hole from the start
        val parsByHole: Map<Int, Int> = courseId?.let {
            holeInfoDao.getHolesForCourseSync(it).associate { h -> h.holeNumber to h.par }
        } ?: emptyMap()

        val holeScores = (1..holeCount).map { num ->
            HoleScore(
                roundId = newRoundId,
                holeNumber = num,
                par = parsByHole[num] ?: 4
            ).toEntity()
        }
        holeScoreDao.insertHoleScores(holeScores)

        return newRoundId
    }

    override suspend fun updateRound(round: Round) {
        roundDao.updateRound(round.toEntity())
    }

    override suspend fun updateRoundStatus(roundId: Long, status: RoundStatus) {
        val endTime = if (status == RoundStatus.COMPLETED || status == RoundStatus.ABANDONED) {
            Instant.now().toEpochMilli()
        } else null
        roundDao.updateRoundStatus(roundId, status.name, endTime)

        // Update stats
        val totalScore = holeScoreDao.getTotalScore(roundId)
        val totalPutts = holeScoreDao.getTotalPutts(roundId)
        val totalPenalties = holeScoreDao.getTotalPenalties(roundId)
        val totalAdjustment = holeScoreDao.getTotalAdjustment(roundId)
        val holesPlayed = holeScoreDao.getCompletedHoleCount(roundId)
        roundDao.updateRoundStats(roundId, holesPlayed, totalScore, totalPutts, totalPenalties, totalAdjustment)
    }

    override suspend fun recalculateRoundTotals(roundId: Long) {
        val totalScore = holeScoreDao.getTotalScore(roundId)
        val totalPutts = holeScoreDao.getTotalPutts(roundId)
        val totalPenalties = holeScoreDao.getTotalPenalties(roundId)
        val totalAdjustment = holeScoreDao.getTotalAdjustment(roundId)
        val holesPlayed = holeScoreDao.getCompletedHoleCount(roundId)
        roundDao.updateRoundStats(roundId, holesPlayed, totalScore, totalPutts, totalPenalties, totalAdjustment)
    }

    override suspend fun deleteRound(roundId: Long) {
        roundDao.deleteRoundById(roundId)
    }

    override fun getHoleScoresForRound(roundId: Long): Flow<List<HoleScore>> {
        return holeScoreDao.getHoleScoresForRound(roundId).map { scores ->
            scores.map { it.toDomain() }
        }
    }

    override suspend fun getHoleScoresForRoundSync(roundId: Long): List<HoleScore> {
        return holeScoreDao.getHoleScoresForRoundSync(roundId).map { it.toDomain() }
    }

    override suspend fun getHoleScore(roundId: Long, holeNumber: Int): HoleScore? {
        return holeScoreDao.getHoleScore(roundId, holeNumber)?.toDomain()
    }

    override fun getHoleScoreFlow(roundId: Long, holeNumber: Int): Flow<HoleScore?> {
        return holeScoreDao.getHoleScoreFlow(roundId, holeNumber).map { it?.toDomain() }
    }

    override suspend fun createHoleScore(holeScore: HoleScore): Long {
        return holeScoreDao.insertHoleScore(holeScore.toEntity())
    }

    override suspend fun updateHoleScore(holeScore: HoleScore) {
        holeScoreDao.updateHoleScore(holeScore.toEntity())
    }

    override suspend fun updateHoleScoreStats(
        holeScoreId: Long,
        score: Int?,
        putts: Int?,
        penalties: Int?,
        adjustment: Int?,
        fairwayHit: Boolean?,
        gir: Boolean?
    ) {
        holeScoreDao.updateHoleScoreStats(holeScoreId, score, putts, penalties, adjustment, fairwayHit, gir)
    }

    override fun getShotsForHole(holeScoreId: Long): Flow<List<Shot>> {
        return shotDao.getShotsForHole(holeScoreId).map { shots ->
            shots.map { shot ->
                val clubName = shot.clubId?.let { clubDao.getClubById(it)?.name }
                shot.toDomain(clubName)
            }
        }
    }

    override suspend fun getShotsForHoleSync(holeScoreId: Long): List<Shot> {
        return shotDao.getShotsForHoleSync(holeScoreId).map { shot ->
            val clubName = shot.clubId?.let { clubDao.getClubById(it)?.name }
            shot.toDomain(clubName)
        }
    }

    override suspend fun recordShot(shot: Shot): Long {
        return shotDao.insertShot(shot.toEntity())
    }

    override suspend fun deleteShot(shotId: Long) {
        shotDao.getShotById(shotId)?.let { shotDao.deleteShot(it) }
    }

    override suspend fun updateShotDistance(shotId: Long, distance: Int) {
        shotDao.updateShotDistance(shotId, distance)
    }

    override suspend fun getLastShot(holeScoreId: Long): Shot? {
        val shot = shotDao.getLastShot(holeScoreId) ?: return null
        val clubName = shot.clubId?.let { clubDao.getClubById(it)?.name }
        return shot.toDomain(clubName)
    }

    override suspend fun getAllShotsForRound(roundId: Long): List<Shot> {
        return shotDao.getAllShotsForRound(roundId).map { shot ->
            val clubName = shot.clubId?.let { clubDao.getClubById(it)?.name }
            shot.toDomain(clubName)
        }
    }

    override suspend fun getAverageDistanceForClub(clubId: Long): Double? {
        return shotDao.getAverageDistanceForClub(clubId)
    }

    override suspend fun getShotsForClub(clubId: Long): List<Shot> {
        return shotDao.getShotsForClub(clubId).map { shot ->
            val clubName = clubDao.getClubById(clubId)?.name
            shot.toDomain(clubName)
        }
    }
}
