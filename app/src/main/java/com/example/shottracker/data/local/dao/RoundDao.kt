package com.example.shottracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.shottracker.data.local.entity.RoundEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoundDao {
    @Query("SELECT * FROM rounds ORDER BY startTime DESC")
    fun getAllRounds(): Flow<List<RoundEntity>>

    @Query("SELECT * FROM rounds WHERE status = :status ORDER BY startTime DESC")
    fun getRoundsByStatus(status: String): Flow<List<RoundEntity>>

    @Query("SELECT * FROM rounds WHERE status = 'IN_PROGRESS' LIMIT 1")
    fun getActiveRound(): Flow<RoundEntity?>

    @Query("SELECT * FROM rounds WHERE status = 'IN_PROGRESS' LIMIT 1")
    suspend fun getActiveRoundSync(): RoundEntity?

    @Query("SELECT * FROM rounds WHERE roundId = :roundId")
    suspend fun getRoundById(roundId: Long): RoundEntity?

    @Query("SELECT * FROM rounds WHERE roundId = :roundId")
    fun getRoundByIdFlow(roundId: Long): Flow<RoundEntity?>

    @Query("SELECT * FROM rounds WHERE status = 'COMPLETED' ORDER BY startTime DESC LIMIT :limit")
    fun getRecentCompletedRounds(limit: Int): Flow<List<RoundEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRound(round: RoundEntity): Long

    @Update
    suspend fun updateRound(round: RoundEntity)

    @Delete
    suspend fun deleteRound(round: RoundEntity)

    @Query("DELETE FROM rounds WHERE roundId = :roundId")
    suspend fun deleteRoundById(roundId: Long)

    @Query("UPDATE rounds SET status = :status, endTime = :endTime WHERE roundId = :roundId")
    suspend fun updateRoundStatus(roundId: Long, status: String, endTime: Long?)

    @Query("UPDATE rounds SET holesPlayed = :holesPlayed, totalScore = :totalScore, totalPutts = :totalPutts, totalPenalties = :totalPenalties, totalAdjustment = :totalAdjustment WHERE roundId = :roundId")
    suspend fun updateRoundStats(
        roundId: Long,
        holesPlayed: Int,
        totalScore: Int?,
        totalPutts: Int?,
        totalPenalties: Int?,
        totalAdjustment: Int?
    )
}
