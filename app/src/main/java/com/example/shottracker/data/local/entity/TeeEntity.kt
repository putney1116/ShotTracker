package com.example.shottracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tees",
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
data class TeeEntity(
    @PrimaryKey(autoGenerate = true)
    val teeId: Long = 0,
    val courseId: Long,
    val name: String,
    val color: String?,
    val rating: Double?,
    val slope: Int?,
    val totalDistance: Int? = null
)
