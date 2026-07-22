package com.example.shottracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.shottracker.data.local.entity.ShotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShotDao {
    @Query("SELECT * FROM shots WHERE holeScoreId = :holeScoreId ORDER BY shotNumber ASC")
    fun getShotsForHole(holeScoreId: Long): Flow<List<ShotEntity>>

    @Query("SELECT * FROM shots WHERE holeScoreId = :holeScoreId ORDER BY shotNumber ASC")
    suspend fun getShotsForHoleSync(holeScoreId: Long): List<ShotEntity>

    @Query("SELECT * FROM shots WHERE shotId = :shotId")
    suspend fun getShotById(shotId: Long): ShotEntity?

    @Query("SELECT * FROM shots WHERE holeScoreId = :holeScoreId ORDER BY shotNumber DESC LIMIT 1")
    suspend fun getLastShot(holeScoreId: Long): ShotEntity?

    @Query("SELECT COUNT(*) FROM shots WHERE holeScoreId = :holeScoreId")
    suspend fun getShotCount(holeScoreId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShot(shot: ShotEntity): Long

    @Update
    suspend fun updateShot(shot: ShotEntity)

    @Delete
    suspend fun deleteShot(shot: ShotEntity)

    @Query("DELETE FROM shots WHERE holeScoreId = :holeScoreId")
    suspend fun deleteShotsForHole(holeScoreId: Long)

    @Query("UPDATE shots SET distanceYards = :distance WHERE shotId = :shotId")
    suspend fun updateShotDistance(shotId: Long, distance: Int)

    @Query("""
        SELECT s.* FROM shots s
        INNER JOIN hole_scores hs ON s.holeScoreId = hs.holeScoreId
        WHERE hs.roundId = :roundId
        ORDER BY hs.holeNumber ASC, s.shotNumber ASC
    """)
    suspend fun getAllShotsForRound(roundId: Long): List<ShotEntity>

    @Query("""
        SELECT s.* FROM shots s
        WHERE s.clubId = :clubId AND s.distanceYards IS NOT NULL
        ORDER BY s.timestamp DESC
    """)
    suspend fun getShotsForClub(clubId: Long): List<ShotEntity>

    @Query("""
        SELECT AVG(distanceYards) FROM shots
        WHERE clubId = :clubId AND distanceYards IS NOT NULL
    """)
    suspend fun getAverageDistanceForClub(clubId: Long): Double?
}
