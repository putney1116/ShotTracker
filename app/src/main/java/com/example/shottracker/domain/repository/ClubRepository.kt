package com.example.shottracker.domain.repository

import com.example.shottracker.domain.model.Club
import kotlinx.coroutines.flow.Flow

interface ClubRepository {
    fun getAllClubs(): Flow<List<Club>>
    suspend fun getAllClubsSync(): List<Club>
    suspend fun getClubById(clubId: Long): Club?
    suspend fun insertClub(club: Club): Long
    suspend fun insertClubs(clubs: List<Club>)
    suspend fun updateClub(club: Club)
    suspend fun deleteClub(club: Club)
    suspend fun initializeDefaultClubs()
    suspend fun getClubCount(): Int
}
