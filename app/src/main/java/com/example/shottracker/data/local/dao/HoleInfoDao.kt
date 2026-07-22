package com.example.shottracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.shottracker.data.local.entity.HoleInfoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HoleInfoDao {
    @Query("SELECT * FROM hole_info WHERE courseId = :courseId ORDER BY holeNumber ASC")
    fun getHolesForCourse(courseId: Long): Flow<List<HoleInfoEntity>>

    @Query("SELECT * FROM hole_info WHERE courseId = :courseId ORDER BY holeNumber ASC")
    suspend fun getHolesForCourseSync(courseId: Long): List<HoleInfoEntity>

    @Query("SELECT * FROM hole_info WHERE holeInfoId = :holeInfoId")
    suspend fun getHoleById(holeInfoId: Long): HoleInfoEntity?

    @Query("SELECT * FROM hole_info WHERE courseId = :courseId AND holeNumber = :holeNumber LIMIT 1")
    suspend fun getHoleByNumber(courseId: Long, holeNumber: Int): HoleInfoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHole(hole: HoleInfoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHoles(holes: List<HoleInfoEntity>)

    @Update
    suspend fun updateHole(hole: HoleInfoEntity)

    @Delete
    suspend fun deleteHole(hole: HoleInfoEntity)

    @Query("DELETE FROM hole_info WHERE courseId = :courseId")
    suspend fun deleteHolesForCourse(courseId: Long)
}
