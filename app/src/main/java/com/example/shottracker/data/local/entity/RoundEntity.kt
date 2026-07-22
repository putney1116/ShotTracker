package com.example.shottracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rounds",
    foreignKeys = [
        ForeignKey(
            entity = TeeEntity::class,
            parentColumns = ["teeId"],
            childColumns = ["teeId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("teeId")]
)
data class RoundEntity(
    @PrimaryKey(autoGenerate = true)
    val roundId: Long = 0,
    val teeId: Long?,
    val courseName: String,
    val startTime: Long,
    val endTime: Long?,
    val status: String,
    val holesPlayed: Int,
    val totalScore: Int?,
    val totalPutts: Int?,
    val totalPenalties: Int?,
    val pcc: Int?,
    val totalAdjustment: Int? = null,
    val handicapIndex: Double? = null
)

enum class RoundStatus {
    IN_PROGRESS,
    COMPLETED,
    ABANDONED
}
