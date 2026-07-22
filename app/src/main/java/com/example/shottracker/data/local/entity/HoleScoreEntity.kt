package com.example.shottracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "hole_scores",
    foreignKeys = [
        ForeignKey(
            entity = RoundEntity::class,
            parentColumns = ["roundId"],
            childColumns = ["roundId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("roundId")]
)
data class HoleScoreEntity(
    @PrimaryKey(autoGenerate = true)
    val holeScoreId: Long = 0,
    val roundId: Long,
    val holeNumber: Int,
    val par: Int,
    val score: Int?,
    val putts: Int?,
    val penalties: Int?,
    val fairwayHit: Boolean?,
    val greenInRegulation: Boolean?,
    val adjustment: Int? = null
)
