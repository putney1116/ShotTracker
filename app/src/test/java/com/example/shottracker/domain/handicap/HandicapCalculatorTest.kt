package com.example.shottracker.domain.handicap

import com.example.shottracker.domain.model.Round
import com.example.shottracker.domain.model.RoundStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class HandicapCalculatorTest {

    private fun round(id: Long): Round = Round(
        id = id,
        courseName = "Test",
        startTime = Instant.EPOCH,
        status = RoundStatus.COMPLETED,
    )

    @Test
    fun `computeIndex returns null with fewer than 3 differentials`() {
        val rounds = listOf(round(1), round(2))
        val diffs = mapOf(1L to 12.0, 2L to 14.0)
        assertNull(HandicapCalculator.computeIndex(rounds, diffs))
    }

    @Test
    fun `computeIndex with 3 rounds uses lowest minus 2`() {
        val rounds = listOf(round(1), round(2), round(3))
        val diffs = mapOf(1L to 12.0, 2L to 14.0, 3L to 16.0)
        val result = HandicapCalculator.computeIndex(rounds, diffs)!!
        assertEquals(10.0, result.index, 0.001)
        assertEquals(setOf(1L), result.countedRoundIds)
        assertEquals("Lowest of 3 differentials − 2", result.formulaDescription)
    }

    @Test
    fun `computeIndex with 20 rounds averages lowest 8`() {
        val rounds = (1..20L).map { round(it) }
        val diffs = (1..20L).associateWith { it.toDouble() }
        val result = HandicapCalculator.computeIndex(rounds, diffs)!!
        assertEquals(4.5, result.index, 0.001)
        assertEquals((1L..8L).toSet(), result.countedRoundIds)
        assertEquals("Avg of lowest 8 of 20 differentials", result.formulaDescription)
    }

    @Test
    fun `computeIndex rounds to one decimal`() {
        val rounds = (1..3L).map { round(it) }
        val diffs = mapOf(1L to 13.34, 2L to 14.0, 3L to 15.0)
        val result = HandicapCalculator.computeIndex(rounds, diffs)!!
        assertEquals(11.3, result.index, 0.001)
    }

    @Test
    fun `courseHandicap rounds to nearest int`() {
        assertEquals(10, HandicapCalculator.courseHandicap(10.0, 113))
        assertEquals(15, HandicapCalculator.courseHandicap(12.4, 140))
        assertEquals(0, HandicapCalculator.courseHandicap(0.0, 130))
    }

    @Test
    fun `strokesReceived basic allocation`() {
        assertEquals(1, HandicapCalculator.strokesReceived(holeHandicap = 1, courseHandicap = 5))
        assertEquals(1, HandicapCalculator.strokesReceived(holeHandicap = 5, courseHandicap = 5))
        assertEquals(0, HandicapCalculator.strokesReceived(holeHandicap = 6, courseHandicap = 5))
    }

    @Test
    fun `strokesReceived for handicap 18 gives every hole 1 stroke`() {
        for (h in 1..18) {
            assertEquals(1, HandicapCalculator.strokesReceived(holeHandicap = h, courseHandicap = 18))
        }
    }

    @Test
    fun `strokesReceived for handicap 27 gives 2 strokes to holes 1-9 and 1 stroke to holes 10-18`() {
        for (h in 1..9) {
            assertEquals(2, HandicapCalculator.strokesReceived(holeHandicap = h, courseHandicap = 27))
        }
        for (h in 10..18) {
            assertEquals(1, HandicapCalculator.strokesReceived(holeHandicap = h, courseHandicap = 27))
        }
    }

    @Test
    fun `strokesReceived for negative course handicap (plus player)`() {
        // courseHandicap = -2 → floorDiv = -1, floorMod = 16 → holes 1..16 get -1 + 1 = 0, holes 17-18 get -1
        assertEquals(0, HandicapCalculator.strokesReceived(holeHandicap = 1, courseHandicap = -2))
        assertEquals(0, HandicapCalculator.strokesReceived(holeHandicap = 16, courseHandicap = -2))
        assertEquals(-1, HandicapCalculator.strokesReceived(holeHandicap = 17, courseHandicap = -2))
        assertEquals(-1, HandicapCalculator.strokesReceived(holeHandicap = 18, courseHandicap = -2))
    }

    @Test
    fun `netDoubleBogey is par plus 2 plus strokes`() {
        assertEquals(6, HandicapCalculator.netDoubleBogey(par = 4, strokesReceived = 0))
        assertEquals(7, HandicapCalculator.netDoubleBogey(par = 4, strokesReceived = 1))
        assertEquals(5, HandicapCalculator.netDoubleBogey(par = 3, strokesReceived = 0))
    }
}
