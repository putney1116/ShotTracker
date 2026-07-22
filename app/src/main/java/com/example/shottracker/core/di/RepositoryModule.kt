package com.example.shottracker.core.di

import com.example.shottracker.data.repository.ClubRepositoryImpl
import com.example.shottracker.data.repository.CourseRepositoryImpl
import com.example.shottracker.data.repository.RoundRepositoryImpl
import com.example.shottracker.domain.repository.ClubRepository
import com.example.shottracker.domain.repository.CourseRepository
import com.example.shottracker.domain.repository.RoundRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCourseRepository(impl: CourseRepositoryImpl): CourseRepository

    @Binds
    @Singleton
    abstract fun bindClubRepository(impl: ClubRepositoryImpl): ClubRepository

    @Binds
    @Singleton
    abstract fun bindRoundRepository(impl: RoundRepositoryImpl): RoundRepository
}
