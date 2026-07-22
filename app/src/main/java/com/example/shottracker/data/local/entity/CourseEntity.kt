package com.example.shottracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey(autoGenerate = true)
    val courseId: Long = 0,
    val name: String,
    val city: String?,
    val state: String?
)
