package com.example.shottracker.domain.handicap

import com.example.shottracker.domain.model.Round

/**
 * Result of computing the player's WHS handicap index from a list of completed rounds.
 *
 * @property index    The rounded handicap index (one decimal).
 * @property eligibleRoundCount  Count of rounds (max 20) used as input to the formula.
 * @property countedRoundIds  IDs of the rounds whose differentials were averaged.
 * @property formulaDescription Subtext for the UI, e.g. "Avg of lowest 8 of 20 differentials".
 */
data class HandicapIndexResult(
    val index: Double,
    val eligibleRoundCount: Int,
    val countedRoundIds: Set<Long>,
    val formulaDescription: String,
)

object HandicapCalculator {

    /**
     * Computes the WHS handicap index from the supplied rounds plus differentials.
     *
     * @param rounds      All known rounds, most-recent-first (matches RoundDao ordering).
     * @param differentials Map roundId → score differential. Rounds without an entry
     *                      are treated as having no differential (skipped).
     * @return null if fewer than 3 rounds have differentials.
     */
    fun computeIndex(
        rounds: List<Round>,
        differentials: Map<Long, Double>,
    ): HandicapIndexResult? {
        val recent = rounds
            .mapNotNull { round ->
                val diff = differentials[round.id] ?: return@mapNotNull null
                round.id to diff
            }
            .take(20)

        if (recent.size < 3) return null

        val (count, adjustment) = when (recent.size) {
            3 -> 1 to -2.0
            4 -> 1 to -1.0
            5 -> 1 to 0.0
            6 -> 2 to -1.0
            7, 8 -> 2 to 0.0
            9, 10, 11 -> 3 to 0.0
            12, 13, 14 -> 4 to 0.0
            15, 16 -> 5 to 0.0
            17, 18 -> 6 to 0.0
            19 -> 7 to 0.0
            else -> 8 to 0.0 // 20
        }

        val lowest = recent.sortedBy { it.second }.take(count)
        val avg = lowest.map { it.second }.average() + adjustment
        val rounded = Math.round(avg * 10.0) / 10.0
        return HandicapIndexResult(
            index = rounded,
            eligibleRoundCount = recent.size,
            countedRoundIds = lowest.map { it.first }.toSet(),
            formulaDescription = buildFormulaDescription(count, recent.size, adjustment),
        )
    }

    /** WHS course-handicap formula: round(handicapIndex × slope / 113). */
    fun courseHandicap(handicapIndex: Double, slope: Int): Int =
        Math.round(handicapIndex * slope / 113.0).toInt()

    /**
     * Strokes received on a single hole.
     * @param holeHandicap   1..18, the hole's stroke index for the player's tee.
     * @param courseHandicap WHS course handicap (can be negative for plus-handicaps).
     */
    fun strokesReceived(holeHandicap: Int, courseHandicap: Int): Int {
        val base = Math.floorDiv(courseHandicap, 18)
        val remainder = Math.floorMod(courseHandicap, 18)
        val extra = if (holeHandicap <= remainder) 1 else 0
        return base + extra
    }

    /** par + 2 + strokes received. */
    fun netDoubleBogey(par: Int, strokesReceived: Int): Int = par + 2 + strokesReceived

    private fun buildFormulaDescription(count: Int, total: Int, adjustment: Double): String {
        val base = if (count == 1) "Lowest of $total" else "Avg of lowest $count of $total"
        val adj = if (adjustment != 0.0) " − ${kotlin.math.abs(adjustment).toInt()}" else ""
        return "$base differentials$adj"
    }
}
