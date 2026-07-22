package com.example.shottracker.data.remote

import android.util.Log
import com.example.shottracker.data.remote.api.OverpassApiService
import com.example.shottracker.data.remote.dto.OverpassElement
import com.example.shottracker.data.remote.dto.OverpassLatLon
import com.example.shottracker.data.remote.dto.OverpassResponse
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

data class GreenData(
    val centerLat: Double,
    val centerLng: Double,
    val frontLat: Double? = null,
    val frontLng: Double? = null,
    val backLat: Double? = null,
    val backLng: Double? = null
)

data class OsmCourseResult(
    val name: String,
    val lat: Double,
    val lng: Double,
    val greenCount: Int
)

@Singleton
class OverpassRemoteDataSource @Inject constructor(
    private val apiService: OverpassApiService
) {

    /**
     * Searches for golf courses near a location that have green GPS data in OpenStreetMap.
     * Returns only courses with at least 9 greens (minimum for a playable course).
     */
    suspend fun searchNearbyCourses(
        lat: Double,
        lng: Double
    ): List<OsmCourseResult> {
        Log.d("Overpass", "Searching for courses near $lat, $lng")
        val query = buildSearchQuery(lat, lng)
        val response = queryWithRetry(query)
        Log.d("Overpass", "Search response: ${response.elements.size} total elements")

        val courses = mutableListOf<CourseCandidate>()
        val greens = mutableListOf<Pair<Double, Double>>()

        for (element in response.elements) {
            val leisure = element.tags?.get("leisure")
            val golf = element.tags?.get("golf")

            if (leisure == "golf_course") {
                val center = element.center ?: continue
                val name = element.tags["name"] ?: continue
                courses.add(CourseCandidate(name, center.lat, center.lon))
            } else if (golf == "green") {
                val center = element.center ?: continue
                greens.add(center.lat to center.lon)
            }
        }

        Log.d("Overpass", "Search found ${courses.size} courses, ${greens.size} greens within 25km")

        if (courses.isEmpty() || greens.isEmpty()) return emptyList()

        // Match each green to its nearest course
        val greenCounts = mutableMapOf<Int, Int>()
        for (green in greens) {
            var nearestIndex = -1
            var nearestDist = Double.MAX_VALUE
            for ((index, course) in courses.withIndex()) {
                val dist = distance(green.first, green.second, course.lat, course.lng)
                if (dist < nearestDist) {
                    nearestDist = dist
                    nearestIndex = index
                }
            }
            if (nearestIndex >= 0) {
                greenCounts[nearestIndex] = (greenCounts[nearestIndex] ?: 0) + 1
            }
        }

        // Only return courses with 9+ greens
        return courses.mapIndexedNotNull { index, course ->
            val count = greenCounts[index] ?: 0
            if (count >= 9) {
                OsmCourseResult(
                    name = course.name,
                    lat = course.lat,
                    lng = course.lng,
                    greenCount = count
                )
            } else {
                null
            }
        }.sortedBy { distance(it.lat, it.lng, lat, lng) }
    }

    /**
     * Fetches green coordinates from OpenStreetMap for a golf course at the given location.
     * Returns a map of hole number to GreenData with front/center/back coordinates.
     */
    suspend fun getGreenCoordinates(
        courseLat: Double,
        courseLng: Double
    ): Map<Int, GreenData> {
        val query = buildDetailQuery(courseLat, courseLng)
        val response = queryWithRetry(query)

        val greens = mutableListOf<GreenCandidate>()
        val holes = mutableListOf<HoleEndpoint>()
        val pins = mutableListOf<HoleEndpoint>()

        for (element in response.elements) {
            val golf = element.tags?.get("golf") ?: continue

            when (golf) {
                "green" -> {
                    val geometry = element.geometry ?: emptyList()
                    val centerLat: Double
                    val centerLng: Double
                    if (element.center != null) {
                        centerLat = element.center.lat
                        centerLng = element.center.lon
                    } else if (geometry.isNotEmpty()) {
                        centerLat = geometry.map { it.lat }.average()
                        centerLng = geometry.map { it.lon }.average()
                    } else {
                        continue
                    }
                    greens.add(GreenCandidate(centerLat, centerLng, geometry))
                }
                "hole" -> {
                    val ref = element.tags["ref"]?.toIntOrNull() ?: continue
                    val geom = element.geometry ?: continue
                    val firstPoint = geom.firstOrNull() ?: continue
                    val lastPoint = geom.lastOrNull() ?: continue
                    // lat/lng = green end (last), teeLat/teeLng = tee end (first)
                    holes.add(HoleEndpoint(ref, lastPoint.lat, lastPoint.lon, firstPoint.lat, firstPoint.lon))
                }
                "pin" -> {
                    val lat = element.lat ?: continue
                    val lon = element.lon ?: continue
                    val ref = element.tags["ref"]?.toIntOrNull()
                    if (ref != null) {
                        pins.add(HoleEndpoint(ref, lat, lon, lat, lon))
                    }
                }
            }
        }

        Log.d("Overpass", "Found ${greens.size} greens, ${holes.size} holes, ${pins.size} pins")

        if (greens.isEmpty() && pins.isEmpty()) return emptyMap()

        // Strategy 1: If we have pins with ref tags, use them directly (center only, no front/back)
        if (pins.isNotEmpty()) {
            Log.d("Overpass", "Using ${pins.size} pin locations as green centers")
            return pins.associate { it.holeNumber to GreenData(centerLat = it.lat, centerLng = it.lng) }
        }

        // Strategy 2: Match greens to holes, then calculate front/back from polygon geometry
        if (greens.isNotEmpty() && holes.isNotEmpty()) {
            Log.d("Overpass", "Matching ${greens.size} greens to ${holes.size} holes")
            return matchGreensToHoles(greens, holes)
        }

        Log.d("Overpass", "Cannot match greens to hole numbers (no hole ways or pins)")
        return emptyMap()
    }

    /**
     * Runs an Overpass query, rotating across mirror endpoints on failure. The main server
     * frequently returns a 504 on a cold first request when its dispatcher is busy; retrying
     * against a less-loaded mirror (with a short backoff) recovers transparently instead of
     * surfacing the error to the user.
     */
    private suspend fun queryWithRetry(data: String): OverpassResponse {
        val endpoints = OverpassApiService.ENDPOINTS
        var lastError: Exception? = null
        for ((index, url) in endpoints.withIndex()) {
            try {
                return apiService.queryUrl(url, data)
            } catch (e: Exception) {
                lastError = e
                Log.w("Overpass", "Endpoint ${index + 1}/${endpoints.size} ($url) failed: ${e.message}")
                if (index < endpoints.size - 1) delay(RETRY_BACKOFF_MS)
            }
        }
        throw lastError ?: IllegalStateException("Overpass query failed on all endpoints")
    }

    private fun buildSearchQuery(lat: Double, lng: Double): String {
        return """
            [out:json][timeout:90];
            (
              way["leisure"="golf_course"](around:100000,$lat,$lng);
              relation["leisure"="golf_course"](around:100000,$lat,$lng);
              way["golf"="green"](around:100000,$lat,$lng);
            );
            out body center qt;
        """.trimIndent()
    }

    private fun buildDetailQuery(lat: Double, lng: Double): String {
        return """
            [out:json][timeout:25];
            way["golf"="green"](around:2000,$lat,$lng);
            out body center geom qt;
            way["golf"="hole"](around:2000,$lat,$lng);
            out body geom qt;
            node["golf"="pin"](around:2000,$lat,$lng);
            out body qt;
        """.trimIndent()
    }

    private fun matchGreensToHoles(
        greens: List<GreenCandidate>,
        holes: List<HoleEndpoint>
    ): Map<Int, GreenData> {
        val result = mutableMapOf<Int, GreenData>()
        val usedGreens = mutableSetOf<Int>()

        for (hole in holes) {
            var nearestIndex = -1
            var nearestDist = Double.MAX_VALUE

            for ((index, green) in greens.withIndex()) {
                if (index in usedGreens) continue
                val dist = distance(hole.lat, hole.lng, green.centerLat, green.centerLng)
                if (dist < nearestDist) {
                    nearestDist = dist
                    nearestIndex = index
                }
            }

            if (nearestIndex >= 0) {
                val green = greens[nearestIndex]
                usedGreens.add(nearestIndex)

                if (green.geometry.size >= 3) {
                    // Use tee point (far from green) for approach direction
                    val frontBack = calculateFrontBack(green.geometry, hole.teeLat, hole.teeLng)
                    result[hole.holeNumber] = GreenData(
                        centerLat = green.centerLat,
                        centerLng = green.centerLng,
                        frontLat = frontBack.first.first,
                        frontLng = frontBack.first.second,
                        backLat = frontBack.second.first,
                        backLng = frontBack.second.second
                    )
                } else {
                    result[hole.holeNumber] = GreenData(
                        centerLat = green.centerLat,
                        centerLng = green.centerLng
                    )
                }
            }
        }

        return result
    }

    /**
     * Given a green polygon and the tee point,
     * finds the front (nearest to tee) and back (farthest from tee) of the green.
     */
    private fun calculateFrontBack(
        geometry: List<OverpassLatLon>,
        approachLat: Double,
        approachLng: Double
    ): Pair<Pair<Double, Double>, Pair<Double, Double>> {
        var frontLat = geometry[0].lat
        var frontLng = geometry[0].lon
        var backLat = geometry[0].lat
        var backLng = geometry[0].lon
        var minDist = Double.MAX_VALUE
        var maxDist = 0.0

        for (node in geometry) {
            val dist = distance(approachLat, approachLng, node.lat, node.lon)
            if (dist < minDist) {
                minDist = dist
                frontLat = node.lat
                frontLng = node.lon
            }
            if (dist > maxDist) {
                maxDist = dist
                backLat = node.lat
                backLng = node.lon
            }
        }

        return (frontLat to frontLng) to (backLat to backLng)
    }

    private fun distance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val dLat = lat1 - lat2
        val dLng = lng1 - lng2
        return sqrt(dLat * dLat + dLng * dLng)
    }

    private data class GreenCandidate(
        val centerLat: Double,
        val centerLng: Double,
        val geometry: List<OverpassLatLon>
    )

    private data class CourseCandidate(
        val name: String,
        val lat: Double,
        val lng: Double
    )

    private data class HoleEndpoint(
        val holeNumber: Int,
        val lat: Double,
        val lng: Double,
        val teeLat: Double,
        val teeLng: Double
    )

    private companion object {
        const val RETRY_BACKOFF_MS = 800L
    }
}
