package com.example.shottracker.data.mapper

import com.example.shottracker.data.local.entity.ClubEntity
import com.example.shottracker.domain.model.Club
import com.example.shottracker.domain.model.ClubCategory

fun ClubEntity.toDomain(): Club = Club(
    id = clubId,
    name = name,
    category = ClubCategory.entries.find { it.name == category } ?: ClubCategory.IRON,
    loft = loft,
    displayOrder = displayOrder
)

fun Club.toEntity(): ClubEntity = ClubEntity(
    clubId = id,
    name = name,
    category = category.name,
    loft = loft,
    displayOrder = displayOrder
)
