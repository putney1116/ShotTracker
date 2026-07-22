package com.example.shottracker.core.util

import com.example.shottracker.domain.model.GpsLocation
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object DistanceCalculator {
    private const val EARTH_RADIUS_METERS = 6371000.0
    private const val METERS_TO_YARDS = 1.09361

    /**
     * Calculate distance between two GPS coordinates using the Haversine formula.
     * Returns distance in yards.
     */
    fun calculateDistanceYards(
        lat1: Double,
        lng1: Double,
        lat2: Double,
        lng2: Double
    ): Int {
        val meters = calculateDistanceMeters(lat1, lng1, lat2, lng2)
        return (meters * METERS_TO_YARDS).toInt()
    }

    /**
     * Calculate distance between two GPS locations.
     * Returns distance in yards.
     */
    fun calculateDistanceYards(from: GpsLocation, to: GpsLocation): Int {
        return calculateDistanceYards(from.latitude, from.longitude, to.latitude, to.longitude)
    }

    /**
     * Calculate distance in meters using Haversine formula.
     */
    fun calculateDistanceMeters(
        lat1: Double,
        lng1: Double,
        lat2: Double,
        lng2: Double
    ): Double {
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        val deltaLat = Math.toRadians(lat2 - lat1)
        val deltaLng = Math.toRadians(lng2 - lng1)

        val a = sin(deltaLat / 2) * sin(deltaLat / 2) +
                cos(lat1Rad) * cos(lat2Rad) *
                sin(deltaLng / 2) * sin(deltaLng / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EARTH_RADIUS_METERS * c
    }
}

data class DistanceToGreen(
    val front: Int?,
    val center: Int?,
    val back: Int?
) {
    companion object {
        val EMPTY = DistanceToGreen(null, null, null)
    }

    val hasData: Boolean
        get() = front != null || center != null || back != null
}
