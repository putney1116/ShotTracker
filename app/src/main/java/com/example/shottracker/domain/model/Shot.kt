package com.example.shottracker.domain.model

import java.time.Instant

data class Shot(
    val id: Long = 0,
    val holeScoreId: Long,
    val clubId: Long? = null,
    val clubName: String? = null,
    val shotNumber: Int,
    val latitude: Double,
    val longitude: Double,
    val distanceYards: Int? = null,
    val timestamp: Instant = Instant.now()
)

data class GpsLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float? = null
)
