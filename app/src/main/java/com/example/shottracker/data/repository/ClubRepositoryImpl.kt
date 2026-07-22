package com.example.shottracker.data.repository

import com.example.shottracker.data.local.dao.ClubDao
import com.example.shottracker.data.mapper.toDomain
import com.example.shottracker.data.mapper.toEntity
import com.example.shottracker.domain.model.Club
import com.example.shottracker.domain.model.DefaultClubs
import com.example.shottracker.domain.repository.ClubRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClubRepositoryImpl @Inject constructor(
    private val clubDao: ClubDao
) : ClubRepository {

    override fun getAllClubs(): Flow<List<Club>> {
        return clubDao.getAllClubs().map { clubs ->
            clubs.map { it.toDomain() }
        }
    }

    override suspend fun getAllClubsSync(): List<Club> {
        return clubDao.getAllClubsSync().map { it.toDomain() }
    }

    override suspend fun getClubById(clubId: Long): Club? {
        return clubDao.getClubById(clubId)?.toDomain()
    }

    override suspend fun insertClub(club: Club): Long {
        return clubDao.insertClub(club.toEntity())
    }

    override suspend fun insertClubs(clubs: List<Club>) {
        clubDao.insertClubs(clubs.map { it.toEntity() })
    }

    override suspend fun updateClub(club: Club) {
        clubDao.updateClub(club.toEntity())
    }

    override suspend fun deleteClub(club: Club) {
        clubDao.deleteClub(club.toEntity())
    }

    override suspend fun initializeDefaultClubs() {
        if (clubDao.getClubCount() == 0) {
            clubDao.insertClubs(DefaultClubs.getDefaultClubSet().map { it.toEntity() })
        }
    }

    override suspend fun getClubCount(): Int {
        return clubDao.getClubCount()
    }
}
