package com.example.shottracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.shottracker.data.local.entity.TeeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TeeDao {
    @Query("SELECT * FROM tees WHERE courseId = :courseId ORDER BY name ASC")
    fun getTeesForCourse(courseId: Long): Flow<List<TeeEntity>>

    @Query("SELECT * FROM tees WHERE courseId = :courseId ORDER BY name ASC")
    suspend fun getTeesForCourseSync(courseId: Long): List<TeeEntity>

    @Query("SELECT * FROM tees WHERE teeId = :teeId")
    suspend fun getTeeById(teeId: Long): TeeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTee(tee: TeeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTees(tees: List<TeeEntity>)

    @Update
    suspend fun updateTee(tee: TeeEntity)

    @Delete
    suspend fun deleteTee(tee: TeeEntity)

    @Query("DELETE FROM tees WHERE teeId = :teeId")
    suspend fun deleteTeeById(teeId: Long)

    @Query("DELETE FROM tees WHERE courseId = :courseId")
    suspend fun deleteTeesForCourse(courseId: Long)
}
