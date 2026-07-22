package com.example.shottracker.core.util

/**
 * Parametric (Liang–Barsky style) clip of the segment A(ax,ay) → B(bx,by) against
 * the axis-aligned rectangle [minX,maxX] × [minY,maxY]. Returns the parameter
 * t ∈ [0,1] whose point P(t) = A + t·(B−A) lies inside the rectangle and is as
 * close as possible to the segment's midpoint (t = 0.5).
 *
 * Coordinates are **screen pixels**, so this is correct under any map rotation or
 * tilt: the caller projects the two lat/lng endpoints to screen space first, then
 * maps the returned t back to a lat/lng. (Clipping in axis-aligned lat/lng bounds
 * is wrong when the map has a bearing, because that bounding box is larger than
 * the true on-screen viewport.)
 *
 * When the segment never enters the rectangle, returns 0.5 so the caller falls
 * back to the geometric midpoint — there is nothing on screen to anchor to.
 */
fun visibleMidpointT(
    ax: Double, ay: Double,
    bx: Double, by: Double,
    minX: Double, minY: Double,
    maxX: Double, maxY: Double,
): Double {
    var tMin = 0.0
    var tMax = 1.0

    val dx = bx - ax
    if (dx != 0.0) {
        val t1 = (minX - ax) / dx
        val t2 = (maxX - ax) / dx
        tMin = maxOf(tMin, minOf(t1, t2))
        tMax = minOf(tMax, maxOf(t1, t2))
    } else if (ax < minX || ax > maxX) {
        return 0.5 // vertical segment outside the horizontal slab
    }

    val dy = by - ay
    if (dy != 0.0) {
        val t1 = (minY - ay) / dy
        val t2 = (maxY - ay) / dy
        tMin = maxOf(tMin, minOf(t1, t2))
        tMax = minOf(tMax, maxOf(t1, t2))
    } else if (ay < minY || ay > maxY) {
        return 0.5 // horizontal segment outside the vertical slab
    }

    if (tMin > tMax) return 0.5 // no part of the segment is inside the rectangle
    return 0.5.coerceIn(tMin, tMax)
}
