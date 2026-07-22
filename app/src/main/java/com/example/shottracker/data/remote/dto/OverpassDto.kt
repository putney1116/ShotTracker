package com.example.shottracker.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class OverpassResponse(
    val elements: List<OverpassElement> = emptyList()
)

@Serializable
data class OverpassElement(
    val type: String,
    val id: Long,
    val lat: Double? = null,
    val lon: Double? = null,
    val center: OverpassCenter? = null,
    val geometry: List<OverpassLatLon>? = null,
    val tags: Map<String, String>? = null
)

@Serializable
data class OverpassCenter(
    val lat: Double,
    val lon: Double
)

@Serializable
data class OverpassLatLon(
    val lat: Double,
    val lon: Double
)
