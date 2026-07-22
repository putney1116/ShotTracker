package com.example.shottracker.data.repository

import com.example.shottracker.data.local.dao.CourseDao
import com.example.shottracker.data.local.dao.HoleInfoDao
import com.example.shottracker.data.local.dao.TeeDao
import com.example.shottracker.data.local.dao.TeeHoleInfoDao
import com.example.shottracker.data.local.entity.TeeHoleInfoEntity
import com.example.shottracker.data.mapper.toDomain
import com.example.shottracker.data.mapper.toEntity
import android.util.Log
import com.example.shottracker.data.remote.OverpassRemoteDataSource
import com.example.shottracker.domain.model.Course
import com.example.shottracker.domain.model.HoleInfo
import com.example.shottracker.domain.model.Tee
import com.example.shottracker.domain.repository.CourseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CourseRepositoryImpl @Inject constructor(
    private val courseDao: CourseDao,
    private val holeInfoDao: HoleInfoDao,
    private val teeDao: TeeDao,
    private val teeHoleInfoDao: TeeHoleInfoDao,
    private val overpassDataSource: OverpassRemoteDataSource
) : CourseRepository {

    override fun getAllCourses(): Flow<List<Course>> {
        return courseDao.getAllCourses().map { courses ->
            courses.map { it.toDomain() }
        }
    }

    override suspend fun getCourseById(courseId: Long): Course? {
        return courseDao.getCourseById(courseId)?.toDomain()
    }

    override suspend fun getCourseByName(name: String): Course? {
        return courseDao.getCourseByName(name)?.toDomain()
    }

    override suspend fun insertCourse(course: Course): Long {
        return courseDao.insertCourse(course.toEntity())
    }

    override suspend fun updateCourse(course: Course) {
        courseDao.updateCourse(course.toEntity())
    }

    override suspend fun deleteCourse(courseId: Long) {
        courseDao.deleteCourseById(courseId)
    }

    override suspend fun createCourse(
        course: Course,
        holes: List<HoleInfo>,
        tees: List<Tee>,
    ): Long {
        val courseEntity = course.copy(id = 0).toEntity()
        val holeEntities = holes.map { it.copy(courseId = 0).toEntity() }
        val teeEntities = tees.map { it.copy(courseId = 0).toEntity() }
        return courseDao.createCourseWithHolesAndTees(courseEntity, holeEntities, teeEntities)
    }

    override fun getHolesForCourse(courseId: Long): Flow<List<HoleInfo>> {
        return holeInfoDao.getHolesForCourse(courseId).map { holes ->
            holes.map { it.toDomain() }
        }
    }

    override suspend fun getHoleByNumber(courseId: Long, holeNumber: Int): HoleInfo? {
        return holeInfoDao.getHoleByNumber(courseId, holeNumber)?.toDomain()
    }

    override suspend fun insertHoles(holes: List<HoleInfo>) {
        holeInfoDao.insertHoles(holes.map { it.toEntity() })
    }

    override suspend fun updateHole(hole: HoleInfo) {
        holeInfoDao.updateHole(hole.toEntity())
    }

    override fun getTeesForCourse(courseId: Long): Flow<List<Tee>> {
        return teeDao.getTeesForCourse(courseId).map { tees ->
            tees.map { it.toDomain() }
        }
    }

    override suspend fun getTeeById(teeId: Long): Tee? {
        return teeDao.getTeeById(teeId)?.toDomain()
    }

    override suspend fun insertTee(tee: Tee): Long {
        return teeDao.insertTee(tee.toEntity())
    }

    override suspend fun insertTees(tees: List<Tee>) {
        teeDao.insertTees(tees.map { it.toEntity() })
    }

    override suspend fun updateTee(tee: Tee) {
        teeDao.updateTee(tee.toEntity())
    }

    override suspend fun deleteTee(teeId: Long) {
        teeDao.deleteTeeById(teeId)
    }

    // Per-tee per-hole handicaps

    override suspend fun getHoleHandicaps(teeId: Long): Map<Int, Int> {
        val rows = teeHoleInfoDao.getHandicapRowsForTee(teeId)
        return rows.mapNotNull { row ->
            val h = row.handicap ?: return@mapNotNull null
            row.holeNumber to h
        }.toMap()
    }

    override suspend fun setHoleHandicaps(teeId: Long, holeHandicaps: Map<Int, Int>) {
        val tee = teeDao.getTeeById(teeId) ?: return
        val holes = holeInfoDao.getHolesForCourseSync(tee.courseId)
        val holeInfoIdByNumber: Map<Int, Long> = holes.associate { it.holeNumber to it.holeInfoId }

        val rows = holeHandicaps.mapNotNull { (holeNumber, handicap) ->
            val holeInfoId = holeInfoIdByNumber[holeNumber] ?: return@mapNotNull null
            TeeHoleInfoEntity(
                teeHoleInfoId = 0,
                teeId = teeId,
                holeInfoId = holeInfoId,
                yardage = null,
                handicap = handicap
            )
        }
        teeHoleInfoDao.replaceForTee(teeId, rows)
    }

    // OpenStreetMap import

    override suspend fun importCourseFromOsm(
        name: String,
        lat: Double,
        lng: Double
    ): Result<Course> {
        return try {
            Log.d("CourseImport", "Importing '$name' from OSM at $lat, $lng")

            // Query detailed green/hole/pin data for this course
            val greenCoords = overpassDataSource.getGreenCoordinates(lat, lng)
            Log.d("CourseImport", "Overpass returned ${greenCoords.size} green coordinates")

            // Create course in local DB
            val course = Course(id = 0, name = name)
            val localCourseId = courseDao.insertCourse(course.toEntity())

            // Create holes with green coordinates (front/center/back)
            val holes = greenCoords.map { (holeNumber, greenData) ->
                HoleInfo(
                    id = 0,
                    courseId = localCourseId,
                    holeNumber = holeNumber,
                    par = 4,
                    greenFrontLat = greenData.frontLat,
                    greenFrontLng = greenData.frontLng,
                    greenCenterLat = greenData.centerLat,
                    greenCenterLng = greenData.centerLng,
                    greenBackLat = greenData.backLat,
                    greenBackLng = greenData.backLng
                )
            }.sortedBy { it.holeNumber }

            if (holes.isNotEmpty()) {
                holeInfoDao.insertHoles(holes.map { it.toEntity() })
                Log.d("CourseImport", "Saved ${holes.size} holes with green data")
            }

            val savedCourse = course.copy(id = localCourseId, holes = holes)
            Result.success(savedCourse)
        } catch (e: Exception) {
            Log.e("CourseImport", "Import failed for '$name'", e)
            Result.failure(e)
        }
    }
}
