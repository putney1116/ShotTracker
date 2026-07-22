package com.example.shottracker.domain.model

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

data class Round(
    val id: Long = 0,
    val teeId: Long? = null,
    val courseName: String,
    val startTime: Instant,
    val endTime: Instant? = null,
    val status: RoundStatus,
    val holesPlayed: Int = 0,
    val totalScore: Int? = null,
    val totalPutts: Int? = null,
    val totalPenalties: Int? = null,
    val pcc: Int? = null,
    val totalAdjustment: Int? = null,
    val handicapIndex: Double? = null,
    val holeScores: List<HoleScore> = emptyList()
) {
    val startDateTime: LocalDateTime
        get() = LocalDateTime.ofInstant(startTime, ZoneId.systemDefault())

    val endDateTime: LocalDateTime?
        get() = endTime?.let { LocalDateTime.ofInstant(it, ZoneId.systemDefault()) }

    val isActive: Boolean
        get() = status == RoundStatus.IN_PROGRESS

    val scoreToPar: Int?
        get() {
            val score = totalScore ?: return null
            val par = holeScores.filter { it.score != null }.sumOf { it.par }
            return if (par > 0) score - par else null
        }
}

enum class RoundStatus {
    IN_PROGRESS,
    COMPLETED,
    ABANDONED
}
