package com.example.shottracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clubs")
data class ClubEntity(
    @PrimaryKey(autoGenerate = true)
    val clubId: Long = 0,
    val name: String,
    val category: String,
    val loft: Double?,
    val displayOrder: Int
)

enum class ClubCategory(val displayName: String) {
    DRIVER("Driver"),
    WOOD("Fairway Wood"),
    HYBRID("Hybrid"),
    IRON("Iron"),
    WEDGE("Wedge"),
    PUTTER("Putter")
}
