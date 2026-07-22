package com.example.shottracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "hole_info",
    foreignKeys = [
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["courseId"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("courseId")]
)
data class HoleInfoEntity(
    @PrimaryKey(autoGenerate = true)
    val holeInfoId: Long = 0,
    val courseId: Long,
    val holeNumber: Int,
    val par: Int,
    val greenFrontLat: Double?,
    val greenFrontLng: Double?,
    val greenCenterLat: Double?,
    val greenCenterLng: Double?,
    val greenBackLat: Double?,
    val greenBackLng: Double?,
    val notes: String? = null
)
