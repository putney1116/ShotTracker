package com.example.shottracker.domain.model

data class Club(
    val id: Long = 0,
    val name: String,
    val category: ClubCategory,
    val loft: Double? = null,
    val displayOrder: Int = 0
)

enum class ClubCategory(val displayName: String) {
    DRIVER("Driver"),
    WOOD("Fairway Wood"),
    HYBRID("Hybrid"),
    IRON("Iron"),
    WEDGE("Wedge"),
    PUTTER("Putter")
}

object DefaultClubs {
    fun getDefaultClubSet(): List<Club> = listOf(
        Club(name = "Driver", category = ClubCategory.DRIVER, displayOrder = 1),
        Club(name = "3 Wood", category = ClubCategory.WOOD, displayOrder = 2),
        Club(name = "5 Wood", category = ClubCategory.WOOD, displayOrder = 3),
        Club(name = "4 Hybrid", category = ClubCategory.HYBRID, displayOrder = 4),
        Club(name = "5 Iron", category = ClubCategory.IRON, displayOrder = 5),
        Club(name = "6 Iron", category = ClubCategory.IRON, displayOrder = 6),
        Club(name = "7 Iron", category = ClubCategory.IRON, displayOrder = 7),
        Club(name = "8 Iron", category = ClubCategory.IRON, displayOrder = 8),
        Club(name = "9 Iron", category = ClubCategory.IRON, displayOrder = 9),
        Club(name = "PW", category = ClubCategory.WEDGE, displayOrder = 10),
        Club(name = "GW", category = ClubCategory.WEDGE, displayOrder = 11),
        Club(name = "SW", category = ClubCategory.WEDGE, displayOrder = 12),
        Club(name = "LW", category = ClubCategory.WEDGE, displayOrder = 13),
        Club(name = "Putter", category = ClubCategory.PUTTER, displayOrder = 14)
    )
}
