package com.example.shottracker.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.shottracker.data.local.ShotTrackerDatabase
import com.example.shottracker.data.local.entity.CourseEntity
import com.example.shottracker.data.local.entity.HoleInfoEntity
import com.example.shottracker.data.local.entity.TeeEntity
import com.example.shottracker.data.local.entity.TeeHoleInfoEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TeeHoleInfoDaoTest {

    private lateinit var db: ShotTrackerDatabase
    private lateinit var dao: TeeHoleInfoDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ShotTrackerDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.teeHoleInfoDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun replaceForTee_overwritesExistingRows() = runTest {
        val courseId = db.courseDao().insertCourse(CourseEntity(name = "Test", city = null, state = null))
        val holes = (1..18).map {
            HoleInfoEntity(
                holeInfoId = 0,
                courseId = courseId,
                holeNumber = it,
                par = 4,
                greenFrontLat = null,
                greenFrontLng = null,
                greenCenterLat = null,
                greenCenterLng = null,
                greenBackLat = null,
                greenBackLng = null
            )
        }
        db.holeInfoDao().insertHoles(holes)
        val teeId = db.teeDao().insertTee(
            TeeEntity(teeId = 0, courseId = courseId, name = "Black", color = null, rating = 72.0, slope = 132)
        )

        val storedHoles = db.holeInfoDao().getHolesForCourseSync(courseId)
        val first = storedHoles.take(9).mapIndexed { idx, h ->
            TeeHoleInfoEntity(teeId = teeId, holeInfoId = h.holeInfoId, yardage = null, handicap = idx + 1)
        }
        dao.replaceForTee(teeId, first)
        assertEquals(9, dao.getForTee(teeId).size)

        val second = storedHoles.take(18).mapIndexed { idx, h ->
            TeeHoleInfoEntity(teeId = teeId, holeInfoId = h.holeInfoId, yardage = null, handicap = idx + 1)
        }
        dao.replaceForTee(teeId, second)
        val rows = dao.getForTee(teeId)
        assertEquals(18, rows.size)
        assertEquals((1..18).toSet(), rows.mapNotNull { it.handicap }.toSet())
    }

    @Test
    fun getHandicapRowsForTee_joinsHoleNumber() = runTest {
        val courseId = db.courseDao().insertCourse(CourseEntity(name = "Test", city = null, state = null))
        val holes = (1..18).map {
            HoleInfoEntity(
                holeInfoId = 0,
                courseId = courseId,
                holeNumber = it,
                par = 4,
                greenFrontLat = null,
                greenFrontLng = null,
                greenCenterLat = null,
                greenCenterLng = null,
                greenBackLat = null,
                greenBackLng = null
            )
        }
        db.holeInfoDao().insertHoles(holes)
        val teeId = db.teeDao().insertTee(
            TeeEntity(teeId = 0, courseId = courseId, name = "Black", color = null, rating = 72.0, slope = 132)
        )
        val stored = db.holeInfoDao().getHolesForCourseSync(courseId)
        val rows = stored.mapIndexed { idx, h ->
            TeeHoleInfoEntity(teeId = teeId, holeInfoId = h.holeInfoId, yardage = null, handicap = idx + 1)
        }
        dao.replaceForTee(teeId, rows)

        val joined = dao.getHandicapRowsForTee(teeId)
        assertEquals(18, joined.size)
        joined.forEach { row ->
            // Hole at index i has holeNumber i+1, and we assigned handicap = i+1.
            assertEquals(row.holeNumber, row.handicap)
        }
    }
}
