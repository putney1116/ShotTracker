package com.example.shottracker.domain.model

data class Course(
    val id: Long = 0,
    val name: String,
    val city: String? = null,
    val state: String? = null,
    val holes: List<HoleInfo> = emptyList(),
    val tees: List<Tee> = emptyList()
)

data class HoleInfo(
    val id: Long = 0,
    val courseId: Long,
    val holeNumber: Int,
    val par: Int,
    val greenFrontLat: Double? = null,
    val greenFrontLng: Double? = null,
    val greenCenterLat: Double? = null,
    val greenCenterLng: Double? = null,
    val greenBackLat: Double? = null,
    val greenBackLng: Double? = null,
    val notes: String? = null
)

data class Tee(
    val id: Long = 0,
    val courseId: Long,
    val name: String,
    val color: String? = null,
    val rating: Double? = null,
    val slope: Int? = null,
    val totalDistance: Int? = null
)
