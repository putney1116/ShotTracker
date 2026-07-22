package com.example.shottracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.shottracker.data.local.entity.HoleScoreEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HoleScoreDao {
    @Query("SELECT * FROM hole_scores WHERE roundId = :roundId ORDER BY holeNumber ASC")
    fun getHoleScoresForRound(roundId: Long): Flow<List<HoleScoreEntity>>

    @Query("SELECT * FROM hole_scores WHERE roundId = :roundId ORDER BY holeNumber ASC")
    suspend fun getHoleScoresForRoundSync(roundId: Long): List<HoleScoreEntity>

    @Query("SELECT * FROM hole_scores WHERE holeScoreId = :holeScoreId")
    suspend fun getHoleScoreById(holeScoreId: Long): HoleScoreEntity?

    @Query("SELECT * FROM hole_scores WHERE roundId = :roundId AND holeNumber = :holeNumber LIMIT 1")
    suspend fun getHoleScore(roundId: Long, holeNumber: Int): HoleScoreEntity?

    @Query("SELECT * FROM hole_scores WHERE roundId = :roundId AND holeNumber = :holeNumber LIMIT 1")
    fun getHoleScoreFlow(roundId: Long, holeNumber: Int): Flow<HoleScoreEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHoleScore(holeScore: HoleScoreEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHoleScores(holeScores: List<HoleScoreEntity>)

    @Update
    suspend fun updateHoleScore(holeScore: HoleScoreEntity)

    @Delete
    suspend fun deleteHoleScore(holeScore: HoleScoreEntity)

    @Query("DELETE FROM hole_scores WHERE roundId = :roundId")
    suspend fun deleteHoleScoresForRound(roundId: Long)

    @Query("UPDATE hole_scores SET score = :score, putts = :putts, penalties = :penalties, adjustment = :adjustment, fairwayHit = :fairwayHit, greenInRegulation = :gir WHERE holeScoreId = :holeScoreId")
    suspend fun updateHoleScoreStats(
        holeScoreId: Long,
        score: Int?,
        putts: Int?,
        penalties: Int?,
        adjustment: Int?,
        fairwayHit: Boolean?,
        gir: Boolean?
    )

    @Query("SELECT SUM(score) FROM hole_scores WHERE roundId = :roundId AND score IS NOT NULL")
    suspend fun getTotalScore(roundId: Long): Int?

    @Query("SELECT SUM(putts) FROM hole_scores WHERE roundId = :roundId AND putts IS NOT NULL")
    suspend fun getTotalPutts(roundId: Long): Int?

    @Query("SELECT SUM(penalties) FROM hole_scores WHERE roundId = :roundId AND penalties IS NOT NULL")
    suspend fun getTotalPenalties(roundId: Long): Int?

    @Query("SELECT SUM(adjustment) FROM hole_scores WHERE roundId = :roundId AND adjustment IS NOT NULL")
    suspend fun getTotalAdjustment(roundId: Long): Int?

    @Query("SELECT COUNT(*) FROM hole_scores WHERE roundId = :roundId AND score IS NOT NULL")
    suspend fun getCompletedHoleCount(roundId: Long): Int
}
