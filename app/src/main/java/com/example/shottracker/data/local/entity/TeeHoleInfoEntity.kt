package com.example.shottracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tee_hole_info",
    foreignKeys = [
        ForeignKey(
            entity = TeeEntity::class,
            parentColumns = ["teeId"],
            childColumns = ["teeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = HoleInfoEntity::class,
            parentColumns = ["holeInfoId"],
            childColumns = ["holeInfoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("teeId"), Index("holeInfoId")]
)
data class TeeHoleInfoEntity(
    @PrimaryKey(autoGenerate = true)
    val teeHoleInfoId: Long = 0,
    val teeId: Long,
    val holeInfoId: Long,
    val yardage: Int? = null,
    val handicap: Int? = null
)
