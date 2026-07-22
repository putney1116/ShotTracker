package com.example.shottracker.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for [visibleMidpointT], the pure screen-space line-clipping used to keep
 * the on-map distance label visible while the camera is zoomed/rotated.
 *
 * All coordinates are screen pixels. The clip rectangle is [minX,maxX] x [minY,maxY].
 */
class MapGeometryTest {

    private fun pointAt(ax: Double, ay: Double, bx: Double, by: Double, t: Double) =
        Pair(ax + (bx - ax) * t, ay + (by - ay) * t)

    private fun assertInside(
        x: Double, y: Double,
        minX: Double, minY: Double, maxX: Double, maxY: Double
    ) {
        assertTrue("x=$x not in [$minX,$maxX]", x in (minX - 1e-6)..(maxX + 1e-6))
        assertTrue("y=$y not in [$minY,$maxY]", y in (minY - 1e-6)..(maxY + 1e-6))
    }

    @Test
    fun `midpoint inside rectangle returns one half`() {
        val t = visibleMidpointT(0.0, 0.0, 100.0, 100.0, 0.0, 0.0, 100.0, 100.0)
        assertEquals(0.5, t, 1e-9)
    }

    @Test
    fun `midpoint off screen but end B on screen clips toward B`() {
        // A is far off the left edge; B sits inside. The geometric midpoint (t=0.5)
        // is off screen, so the label must slide toward B to stay visible.
        val ax = -300.0; val ay = 50.0
        val bx = 50.0; val by = 50.0
        val t = visibleMidpointT(ax, ay, bx, by, 0.0, 0.0, 100.0, 100.0)

        assertTrue("expected clip toward B (t > 0.5) but was $t", t > 0.5)
        val (x, y) = pointAt(ax, ay, bx, by, t)
        assertInside(x, y, 0.0, 0.0, 100.0, 100.0)
    }

    @Test
    fun `midpoint off screen but end A on screen clips toward A`() {
        val ax = 50.0; val ay = 50.0
        val bx = 400.0; val by = 50.0
        val t = visibleMidpointT(ax, ay, bx, by, 0.0, 0.0, 100.0, 100.0)

        assertTrue("expected clip toward A (t < 0.5) but was $t", t < 0.5)
        val (x, y) = pointAt(ax, ay, bx, by, t)
        assertInside(x, y, 0.0, 0.0, 100.0, 100.0)
    }

    @Test
    fun `vertical segment with midpoint off top clips into view`() {
        // Exercises the dy branch: line is vertical, midpoint above the rectangle.
        val ax = 50.0; val ay = -300.0
        val bx = 50.0; val by = 50.0
        val t = visibleMidpointT(ax, ay, bx, by, 0.0, 0.0, 100.0, 100.0)

        assertTrue("expected clip toward B (t > 0.5) but was $t", t > 0.5)
        val (x, y) = pointAt(ax, ay, bx, by, t)
        assertInside(x, y, 0.0, 0.0, 100.0, 100.0)
    }

    @Test
    fun `segment entirely off screen falls back to midpoint`() {
        val t = visibleMidpointT(-300.0, -300.0, -200.0, -200.0, 0.0, 0.0, 100.0, 100.0)
        assertEquals(0.5, t, 1e-9)
    }

    @Test
    fun `horizontal segment entirely below rectangle falls back to midpoint`() {
        // dy == 0 and the whole line is outside the y-slab.
        val t = visibleMidpointT(0.0, 500.0, 100.0, 500.0, 0.0, 0.0, 100.0, 100.0)
        assertEquals(0.5, t, 1e-9)
    }
}
