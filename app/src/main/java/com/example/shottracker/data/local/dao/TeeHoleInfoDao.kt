package com.example.shottracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.shottracker.data.local.entity.TeeHoleInfoEntity

@Dao
interface TeeHoleInfoDao {
    @Query("SELECT * FROM tee_hole_info WHERE teeId = :teeId")
    suspend fun getForTee(teeId: Long): List<TeeHoleInfoEntity>

    /**
     * Returns rows joined to hole_info so callers can map holeNumber → handicap.
     * Only includes rows where handicap IS NOT NULL.
     */
    @Query("""
        SELECT thi.teeHoleInfoId, thi.teeId, thi.holeInfoId, thi.yardage, thi.handicap, hi.holeNumber
        FROM tee_hole_info thi
        INNER JOIN hole_info hi ON thi.holeInfoId = hi.holeInfoId
        WHERE thi.teeId = :teeId AND thi.handicap IS NOT NULL
        ORDER BY hi.holeNumber ASC
    """)
    suspend fun getHandicapRowsForTee(teeId: Long): List<TeeHoleInfoWithHoleNumber>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(row: TeeHoleInfoEntity): Long

    @Update
    suspend fun update(row: TeeHoleInfoEntity)

    @Query("DELETE FROM tee_hole_info WHERE teeId = :teeId")
    suspend fun deleteForTee(teeId: Long)

    @Transaction
    suspend fun replaceForTee(teeId: Long, rows: List<TeeHoleInfoEntity>) {
        deleteForTee(teeId)
        rows.forEach { insert(it.copy(teeHoleInfoId = 0, teeId = teeId)) }
    }
}

data class TeeHoleInfoWithHoleNumber(
    val teeHoleInfoId: Long,
    val teeId: Long,
    val holeInfoId: Long,
    val yardage: Int?,
    val handicap: Int?,
    val holeNumber: Int
)
