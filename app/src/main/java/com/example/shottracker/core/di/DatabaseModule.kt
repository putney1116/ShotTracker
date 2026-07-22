package com.example.shottracker.core.di

import android.content.Context
import androidx.room.Room
import com.example.shottracker.data.local.MIGRATION_1_2
import com.example.shottracker.data.local.MIGRATION_2_3
import com.example.shottracker.data.local.MIGRATION_3_4
import com.example.shottracker.data.local.MIGRATION_4_5
import com.example.shottracker.data.local.MIGRATION_5_6
import com.example.shottracker.data.local.MIGRATION_6_7
import com.example.shottracker.data.local.ShotTrackerDatabase
import com.example.shottracker.data.local.dao.ClubDao
import com.example.shottracker.data.local.dao.CourseDao
import com.example.shottracker.data.local.dao.HoleInfoDao
import com.example.shottracker.data.local.dao.HoleScoreDao
import com.example.shottracker.data.local.dao.RoundDao
import com.example.shottracker.data.local.dao.ShotDao
import com.example.shottracker.data.local.dao.TeeDao
import com.example.shottracker.data.local.dao.TeeHoleInfoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ShotTrackerDatabase {
        return Room.databaseBuilder(
            context,
            ShotTrackerDatabase::class.java,
            ShotTrackerDatabase.DATABASE_NAME
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
            .build()
    }

    @Provides
    fun provideCourseDao(database: ShotTrackerDatabase): CourseDao = database.courseDao()

    @Provides
    fun provideHoleInfoDao(database: ShotTrackerDatabase): HoleInfoDao = database.holeInfoDao()

    @Provides
    fun provideTeeDao(database: ShotTrackerDatabase): TeeDao = database.teeDao()

    @Provides
    fun provideClubDao(database: ShotTrackerDatabase): ClubDao = database.clubDao()

    @Provides
    fun provideRoundDao(database: ShotTrackerDatabase): RoundDao = database.roundDao()

    @Provides
    fun provideHoleScoreDao(database: ShotTrackerDatabase): HoleScoreDao = database.holeScoreDao()

    @Provides
    fun provideShotDao(database: ShotTrackerDatabase): ShotDao = database.shotDao()

    @Provides
    fun provideTeeHoleInfoDao(database: ShotTrackerDatabase): TeeHoleInfoDao = database.teeHoleInfoDao()
}
