package com.example.shottracker.feature.createcourse

import com.example.shottracker.domain.model.Course
import com.example.shottracker.domain.model.HoleInfo
import com.example.shottracker.domain.model.Tee
import com.example.shottracker.domain.repository.CourseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeCourseRepository : CourseRepository {

    // --- knobs the tests configure ---
    var courseByNameResult: Course? = null
    var createCourseFails: Boolean = false
    var createCourseException: Throwable = RuntimeException("simulated failure")

    // --- captures ---
    data class CreateCall(val course: Course, val holes: List<HoleInfo>, val tees: List<Tee>)
    val createCalls = mutableListOf<CreateCall>()

    override fun getAllCourses(): Flow<List<Course>> = flowOf(emptyList())
    override suspend fun getCourseById(courseId: Long): Course? = null
    override suspend fun getCourseByName(name: String): Course? = courseByNameResult
    override suspend fun insertCourse(course: Course): Long = 0L
    override suspend fun updateCourse(course: Course) {}
    override suspend fun deleteCourse(courseId: Long) {}

    override fun getHolesForCourse(courseId: Long): Flow<List<HoleInfo>> = flowOf(emptyList())
    override suspend fun getHoleByNumber(courseId: Long, holeNumber: Int): HoleInfo? = null
    override suspend fun insertHoles(holes: List<HoleInfo>) {}
    override suspend fun updateHole(hole: HoleInfo) {}

    override fun getTeesForCourse(courseId: Long): Flow<List<Tee>> = flowOf(emptyList())
    override suspend fun getTeeById(teeId: Long): Tee? = null
    override suspend fun insertTee(tee: Tee): Long = 0L
    override suspend fun insertTees(tees: List<Tee>) {}
    override suspend fun updateTee(tee: Tee) {}
    override suspend fun deleteTee(teeId: Long) {}

    override suspend fun getHoleHandicaps(teeId: Long): Map<Int, Int> = emptyMap()
    override suspend fun setHoleHandicaps(teeId: Long, holeHandicaps: Map<Int, Int>) {}

    override suspend fun importCourseFromOsm(name: String, lat: Double, lng: Double): Result<Course> =
        Result.failure(NotImplementedError())

    override suspend fun createCourse(
        course: Course,
        holes: List<HoleInfo>,
        tees: List<Tee>,
    ): Long {
        createCalls.add(CreateCall(course, holes, tees))
        if (createCourseFails) throw createCourseException
        return 42L
    }
}
