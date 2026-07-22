package com.example.shottracker.domain.model

data class HoleScore(
    val id: Long = 0,
    val roundId: Long,
    val holeNumber: Int,
    val par: Int,
    val score: Int? = null,
    val putts: Int? = null,
    val penalties: Int? = null,
    val fairwayHit: Boolean? = null,
    val greenInRegulation: Boolean? = null,
    val adjustment: Int? = null,
    val shots: List<Shot> = emptyList()
) {
    val scoreToPar: Int?
        get() = score?.let { it - par }

    val scoreDisplay: String
        get() = score?.toString() ?: "-"

    val puttsDisplay: String
        get() = putts?.toString() ?: "-"

    val penaltiesDisplay: String
        get() = penalties?.toString() ?: "-"

    /**
     * Adjusted score for handicap purposes: raw score minus the per-hole adjustment.
     * Returns null when score is unset (adjustment alone is not meaningful without a score).
     */
    val adjustedScore: Int?
        get() = score?.let { it - (adjustment ?: 0) }

    /**
     * Green in Regulation: reached the green in par - 2 strokes (i.e., score - putts <= par - 2).
     * Null if score isn't entered yet.
     */
    val isGir: Boolean?
        get() = score?.let { (it - (putts ?: 0)) <= (par - 2) }

    val isComplete: Boolean
        get() = score != null
}

fun Int.toScoreName(par: Int): String = when (this - par) {
    -3 -> "Albatross"
    -2 -> "Eagle"
    -1 -> "Birdie"
    0 -> "Par"
    1 -> "Bogey"
    2 -> "Double Bogey"
    3 -> "Triple Bogey"
    else -> if (this - par > 0) "+${this - par}" else "${this - par}"
}
