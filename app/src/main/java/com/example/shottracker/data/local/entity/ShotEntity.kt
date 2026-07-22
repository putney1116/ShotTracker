package com.example.shottracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "shots",
    foreignKeys = [
        ForeignKey(
            entity = HoleScoreEntity::class,
            parentColumns = ["holeScoreId"],
            childColumns = ["holeScoreId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ClubEntity::class,
            parentColumns = ["clubId"],
            childColumns = ["clubId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("holeScoreId"), Index("clubId")]
)
data class ShotEntity(
    @PrimaryKey(autoGenerate = true)
    val shotId: Long = 0,
    val holeScoreId: Long,
    val clubId: Long?,
    val shotNumber: Int,
    val latitude: Double,
    val longitude: Double,
    val distanceYards: Int?,
    val timestamp: Long
)
