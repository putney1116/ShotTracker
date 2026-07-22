package com.example.shottracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.shottracker.data.local.entity.ClubEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClubDao {
    @Query("SELECT * FROM clubs ORDER BY displayOrder ASC")
    fun getAllClubs(): Flow<List<ClubEntity>>

    @Query("SELECT * FROM clubs ORDER BY displayOrder ASC")
    suspend fun getAllClubsSync(): List<ClubEntity>

    @Query("SELECT * FROM clubs WHERE clubId = :clubId")
    suspend fun getClubById(clubId: Long): ClubEntity?

    @Query("SELECT * FROM clubs WHERE category = :category ORDER BY displayOrder ASC")
    fun getClubsByCategory(category: String): Flow<List<ClubEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClub(club: ClubEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClubs(clubs: List<ClubEntity>)

    @Update
    suspend fun updateClub(club: ClubEntity)

    @Delete
    suspend fun deleteClub(club: ClubEntity)

    @Query("DELETE FROM clubs")
    suspend fun deleteAllClubs()

    @Query("SELECT COUNT(*) FROM clubs")
    suspend fun getClubCount(): Int
}
