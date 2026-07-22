package com.example.shottracker.domain.repository

import com.example.shottracker.domain.model.Course
import com.example.shottracker.domain.model.HoleInfo
import com.example.shottracker.domain.model.Tee
import kotlinx.coroutines.flow.Flow

interface CourseRepository {
    // Local database operations
    fun getAllCourses(): Flow<List<Course>>
    suspend fun getCourseById(courseId: Long): Course?
    suspend fun getCourseByName(name: String): Course?
    suspend fun insertCourse(course: Course): Long
    suspend fun updateCourse(course: Course)
    suspend fun deleteCourse(courseId: Long)
    suspend fun createCourse(course: Course, holes: List<HoleInfo>, tees: List<Tee>): Long

    fun getHolesForCourse(courseId: Long): Flow<List<HoleInfo>>
    suspend fun getHoleByNumber(courseId: Long, holeNumber: Int): HoleInfo?
    suspend fun insertHoles(holes: List<HoleInfo>)
    suspend fun updateHole(hole: HoleInfo)

    fun getTeesForCourse(courseId: Long): Flow<List<Tee>>
    suspend fun getTeeById(teeId: Long): Tee?
    suspend fun insertTee(tee: Tee): Long
    suspend fun insertTees(tees: List<Tee>)
    suspend fun updateTee(tee: Tee)
    suspend fun deleteTee(teeId: Long)

    // Per-tee per-hole handicaps (stroke index)
    /** Returns map of holeNumber → stroke index for the given tee. Empty if none set. */
    suspend fun getHoleHandicaps(teeId: Long): Map<Int, Int>

    /** Replaces all handicap rows for the tee with the supplied map (holeNumber → handicap).
     *  Pass an empty map to clear all handicaps for the tee. */
    suspend fun setHoleHandicaps(teeId: Long, holeHandicaps: Map<Int, Int>)

    // OpenStreetMap import
    suspend fun importCourseFromOsm(name: String, lat: Double, lng: Double): Result<Course>
}
