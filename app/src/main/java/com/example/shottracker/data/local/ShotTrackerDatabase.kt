package com.example.shottracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.shottracker.data.local.dao.ClubDao
import com.example.shottracker.data.local.dao.CourseDao
import com.example.shottracker.data.local.dao.HoleInfoDao
import com.example.shottracker.data.local.dao.HoleScoreDao
import com.example.shottracker.data.local.dao.RoundDao
import com.example.shottracker.data.local.dao.ShotDao
import com.example.shottracker.data.local.dao.TeeDao
import com.example.shottracker.data.local.dao.TeeHoleInfoDao
import com.example.shottracker.data.local.entity.ClubEntity
import com.example.shottracker.data.local.entity.CourseEntity
import com.example.shottracker.data.local.entity.HoleInfoEntity
import com.example.shottracker.data.local.entity.HoleScoreEntity
import com.example.shottracker.data.local.entity.RoundEntity
import com.example.shottracker.data.local.entity.ShotEntity
import com.example.shottracker.data.local.entity.TeeEntity
import com.example.shottracker.data.local.entity.TeeHoleInfoEntity

@Database(
    entities = [
        CourseEntity::class,
        HoleInfoEntity::class,
        TeeEntity::class,
        TeeHoleInfoEntity::class,
        ClubEntity::class,
        RoundEntity::class,
        HoleScoreEntity::class,
        ShotEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class ShotTrackerDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun holeInfoDao(): HoleInfoDao
    abstract fun teeDao(): TeeDao
    abstract fun clubDao(): ClubDao
    abstract fun roundDao(): RoundDao
    abstract fun holeScoreDao(): HoleScoreDao
    abstract fun shotDao(): ShotDao
    abstract fun teeHoleInfoDao(): TeeHoleInfoDao

    companion object {
        const val DATABASE_NAME = "shot_tracker_db"
    }
}
