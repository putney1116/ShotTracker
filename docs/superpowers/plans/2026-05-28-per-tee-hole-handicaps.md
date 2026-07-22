# Per-Tee Hole Handicaps + NDB Auto-Cap Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Capture per-tee stroke index (handicap) for each hole, snapshot the player's handicap index onto each new round at start, and use both inputs to auto-fill the per-hole adjustment up to the Net Double Bogey cap as strokes are recorded during play.

**Architecture:** Repurpose the existing-but-unused `tee_hole_info` table by making `yardage` nullable and adding a nullable `handicap` column. Add `handicapIndex: Double?` to `rounds`. Extract WHS handicap math into a new `HandicapCalculator` domain object so both `HistoryViewModel` and `NewRoundSetupViewModel` use it. Course management gains a "Set Handicaps" dialog per tee. `ActiveRoundViewModel.saveCurrentHole` becomes the single point where the NDB cap is computed and written; the scorecard edit dialogs continue to save the user's manual chip value verbatim. The scorecard / round-detail tables gain a conditional `Hcp` column.

**Tech Stack:** Kotlin 2.2.10, Jetpack Compose (Material 3), Room 2.8.4, Hilt DI, kotlinx-coroutines-test + Turbine for VM unit tests, room-testing for DAO instrumented tests.

**Spec:** `docs/superpowers/specs/2026-05-28-per-tee-hole-handicaps-design.md`

**Repository note:** Project is NOT a git repository. No `git commit` steps. Each major task ends with a `:app:compileDebugKotlin` verification.

**Testing scope:** `HandicapCalculator` and `CourseRepository` get unit / DAO tests. `ActiveRoundViewModel` and the scorecard VMs keep the existing no-unit-test posture (LocationService coupling + manual smoke testing) per the project's documented constraints.

---

## File Inventory

**Modified files:**
- `app/src/main/java/com/example/shottracker/data/local/entity/TeeHoleInfoEntity.kt` — make `yardage` nullable, add `handicap: Int?`
- `app/src/main/java/com/example/shottracker/data/local/entity/RoundEntity.kt` — add `handicapIndex: Double?`
- `app/src/main/java/com/example/shottracker/data/local/Migrations.kt` — add `MIGRATION_6_7`
- `app/src/main/java/com/example/shottracker/data/local/ShotTrackerDatabase.kt` — bump `version` to 7, expose new DAO
- `app/src/main/java/com/example/shottracker/core/di/DatabaseModule.kt` — register `MIGRATION_6_7`, provide new DAO
- `app/src/main/java/com/example/shottracker/domain/model/Round.kt` — add `handicapIndex: Double?`
- `app/src/main/java/com/example/shottracker/data/mapper/RoundMapper.kt` — round-trip `handicapIndex`
- `app/src/main/java/com/example/shottracker/domain/repository/CourseRepository.kt` — add `getHoleHandicaps` and `setHoleHandicaps`
- `app/src/main/java/com/example/shottracker/data/repository/CourseRepositoryImpl.kt` — implement both
- `app/src/main/java/com/example/shottracker/domain/repository/RoundRepository.kt` — extend `startNewRound` with `handicapIndex`
- `app/src/main/java/com/example/shottracker/data/repository/RoundRepositoryImpl.kt` — implement updated signature
- `app/src/main/java/com/example/shottracker/feature/history/HistoryViewModel.kt` — delegate index math to `HandicapCalculator`
- `app/src/main/java/com/example/shottracker/feature/round/NewRoundSetupViewModel.kt` — compute + pass `handicapIndex` at round start
- `app/src/main/java/com/example/shottracker/feature/round/ActiveRoundViewModel.kt` — load tee handicap map; auto-cap in `saveCurrentHole`
- `app/src/main/java/com/example/shottracker/feature/coursemanagement/CourseManagementViewModel.kt` — handicap editor state + methods
- `app/src/main/java/com/example/shottracker/feature/coursemanagement/CourseManagementScreen.kt` — "Set Handicaps" button + `EditHoleHandicapsDialog`
- `app/src/main/java/com/example/shottracker/feature/scorecard/ScorecardViewModel.kt` — load + expose tee handicap map
- `app/src/main/java/com/example/shottracker/feature/scorecard/ScorecardScreen.kt` — conditional `Hcp` column
- `app/src/main/java/com/example/shottracker/feature/history/RoundDetailViewModel.kt` — load + expose tee handicap map
- `app/src/main/java/com/example/shottracker/feature/history/RoundDetailScreen.kt` — mirror `Hcp` column

**New files:**
- `app/src/main/java/com/example/shottracker/data/local/dao/TeeHoleInfoDao.kt`
- `app/src/main/java/com/example/shottracker/domain/handicap/HandicapCalculator.kt`
- `app/src/test/java/com/example/shottracker/domain/handicap/HandicapCalculatorTest.kt`
- `app/src/androidTest/java/com/example/shottracker/data/local/dao/TeeHoleInfoDaoTest.kt`

**Verification commands (Windows PowerShell):**
- `.\gradlew.bat :app:compileDebugKotlin` — fast compile check
- `.\gradlew.bat :app:testDebugUnitTest --tests "com.example.shottracker.domain.handicap.*"` — calculator tests
- `.\gradlew.bat :app:assembleDebug` — full APK build

---

### Task 1: Schema — `tee_hole_info.handicap` and `rounds.handicapIndex`

**Files:**
- Modify: `app/src/main/java/com/example/shottracker/data/local/entity/TeeHoleInfoEntity.kt`
- Modify: `app/src/main/java/com/example/shottracker/data/local/entity/RoundEntity.kt`
- Modify: `app/src/main/java/com/example/shottracker/data/local/Migrations.kt`
- Modify: `app/src/main/java/com/example/shottracker/data/local/ShotTrackerDatabase.kt`
- Modify: `app/src/main/java/com/example/shottracker/core/di/DatabaseModule.kt`

- [ ] **Step 1: Make `yardage` nullable and add `handicap` to `TeeHoleInfoEntity`**

In `TeeHoleInfoEntity.kt`, replace the `data class` with:

```kotlin
data class TeeHoleInfoEntity(
    @PrimaryKey(autoGenerate = true)
    val teeHoleInfoId: Long = 0,
    val teeId: Long,
    val holeInfoId: Long,
    val yardage: Int? = null,
    val handicap: Int? = null
)
```

- [ ] **Step 2: Add `handicapIndex` to `RoundEntity`**

In `RoundEntity.kt`, append `val handicapIndex: Double? = null` after `totalAdjustment`. The data class becomes:

```kotlin
data class RoundEntity(
    @PrimaryKey(autoGenerate = true)
    val roundId: Long = 0,
    val teeId: Long?,
    val courseName: String,
    val startTime: Long,
    val endTime: Long?,
    val status: String,
    val holesPlayed: Int,
    val totalScore: Int?,
    val totalPutts: Int?,
    val totalPenalties: Int?,
    val pcc: Int?,
    val totalAdjustment: Int? = null,
    val handicapIndex: Double? = null
)
```

- [ ] **Step 3: Add `MIGRATION_6_7`**

Append to `Migrations.kt`:

```kotlin
/**
 * v6 -> v7: per-tee per-hole handicap (stroke index) + per-round handicap-index snapshot.
 *   tee_hole_info.yardage is now nullable (rebuilt table)
 *   tee_hole_info.handicap (nullable INTEGER; null = no stroke index for this hole/tee)
 *   rounds.handicapIndex (nullable REAL; snapshotted at round-start)
 */
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE tee_hole_info RENAME TO tee_hole_info_old")
        db.execSQL("""
            CREATE TABLE tee_hole_info (
              teeHoleInfoId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
              teeId INTEGER NOT NULL,
              holeInfoId INTEGER NOT NULL,
              yardage INTEGER,
              handicap INTEGER,
              FOREIGN KEY(teeId) REFERENCES tees(teeId) ON DELETE CASCADE,
              FOREIGN KEY(holeInfoId) REFERENCES hole_info(holeInfoId) ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX index_tee_hole_info_teeId ON tee_hole_info(teeId)")
        db.execSQL("CREATE INDEX index_tee_hole_info_holeInfoId ON tee_hole_info(holeInfoId)")
        db.execSQL("""
            INSERT INTO tee_hole_info (teeHoleInfoId, teeId, holeInfoId, yardage, handicap)
            SELECT teeHoleInfoId, teeId, holeInfoId, yardage, NULL FROM tee_hole_info_old
        """.trimIndent())
        db.execSQL("DROP TABLE tee_hole_info_old")
        db.execSQL("ALTER TABLE rounds ADD COLUMN handicapIndex REAL")
    }
}
```

- [ ] **Step 4: Bump database version to 7**

In `ShotTrackerDatabase.kt`, change `version = 6` to `version = 7`.

- [ ] **Step 5: Register migration**

In `DatabaseModule.kt`, add `MIGRATION_6_7` to the imports and the `addMigrations(...)` chain. The line becomes:

```kotlin
.addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
```

- [ ] **Step 6: Verify compile**

Run: `.\gradlew.bat :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

---

### Task 2: Round mapper + domain model

**Files:**
- Modify: `app/src/main/java/com/example/shottracker/domain/model/Round.kt`
- Modify: `app/src/main/java/com/example/shottracker/data/mapper/RoundMapper.kt`

- [ ] **Step 1: Add `handicapIndex` to `Round`**

In `Round.kt`, append `val handicapIndex: Double? = null` after `totalAdjustment`. The data class becomes:

```kotlin
data class Round(
    val id: Long = 0,
    val teeId: Long? = null,
    val courseName: String,
    val startTime: Instant,
    val endTime: Instant? = null,
    val status: RoundStatus,
    val holesPlayed: Int = 0,
    val totalScore: Int? = null,
    val totalPutts: Int? = null,
    val totalPenalties: Int? = null,
    val pcc: Int? = null,
    val totalAdjustment: Int? = null,
    val handicapIndex: Double? = null,
    val holeScores: List<HoleScore> = emptyList()
)
```

- [ ] **Step 2: Round-trip `handicapIndex` in `RoundMapper`**

In `RoundMapper.kt`, update both functions:

```kotlin
fun RoundEntity.toDomain(holeScores: List<HoleScore> = emptyList()): Round = Round(
    id = roundId,
    teeId = teeId,
    courseName = courseName,
    startTime = Instant.ofEpochMilli(startTime),
    endTime = endTime?.let { Instant.ofEpochMilli(it) },
    status = RoundStatus.entries.find { it.name == status } ?: RoundStatus.IN_PROGRESS,
    holesPlayed = holesPlayed,
    totalScore = totalScore,
    totalPutts = totalPutts,
    totalPenalties = totalPenalties,
    pcc = pcc,
    totalAdjustment = totalAdjustment,
    handicapIndex = handicapIndex,
    holeScores = holeScores
)

fun Round.toEntity(): RoundEntity = RoundEntity(
    roundId = id,
    teeId = teeId,
    courseName = courseName,
    startTime = startTime.toEpochMilli(),
    endTime = endTime?.toEpochMilli(),
    status = status.name,
    holesPlayed = holesPlayed,
    totalScore = totalScore,
    totalPutts = totalPutts,
    totalPenalties = totalPenalties,
    pcc = pcc,
    totalAdjustment = totalAdjustment,
    handicapIndex = handicapIndex
)
```

- [ ] **Step 3: Verify compile**

Run: `.\gradlew.bat :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

---

### Task 3: TeeHoleInfoDao

**Files:**
- Create: `app/src/main/java/com/example/shottracker/data/local/dao/TeeHoleInfoDao.kt`
- Modify: `app/src/main/java/com/example/shottracker/data/local/ShotTrackerDatabase.kt`
- Modify: `app/src/main/java/com/example/shottracker/core/di/DatabaseModule.kt`

- [ ] **Step 1: Create the DAO file**

Create `TeeHoleInfoDao.kt`:

```kotlin
package com.example.shottracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.shottracker.data.local.entity.TeeHoleInfoEntity

@Dao
interface TeeHoleInfoDao {
    @Query("SELECT * FROM tee_hole_info WHERE teeId = :teeId")
    suspend fun getForTee(teeId: Long): List<TeeHoleInfoEntity>

    /**
     * Returns rows joined to hole_info so callers can map holeNumber → handicap.
     * Only includes rows where handicap IS NOT NULL.
     */
    @Query("""
        SELECT thi.teeHoleInfoId, thi.teeId, thi.holeInfoId, thi.yardage, thi.handicap, hi.holeNumber
        FROM tee_hole_info thi
        INNER JOIN hole_info hi ON thi.holeInfoId = hi.holeInfoId
        WHERE thi.teeId = :teeId AND thi.handicap IS NOT NULL
        ORDER BY hi.holeNumber ASC
    """)
    suspend fun getHandicapRowsForTee(teeId: Long): List<TeeHoleInfoWithHoleNumber>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(row: TeeHoleInfoEntity): Long

    @Update
    suspend fun update(row: TeeHoleInfoEntity)

    @Query("DELETE FROM tee_hole_info WHERE teeId = :teeId")
    suspend fun deleteForTee(teeId: Long)

    @Transaction
    suspend fun replaceForTee(teeId: Long, rows: List<TeeHoleInfoEntity>) {
        deleteForTee(teeId)
        rows.forEach { insert(it.copy(teeHoleInfoId = 0, teeId = teeId)) }
    }
}

data class TeeHoleInfoWithHoleNumber(
    val teeHoleInfoId: Long,
    val teeId: Long,
    val holeInfoId: Long,
    val yardage: Int?,
    val handicap: Int?,
    val holeNumber: Int
)
```

- [ ] **Step 2: Expose DAO on the database**

In `ShotTrackerDatabase.kt`, add the abstract function after `shotDao()`:

```kotlin
abstract fun teeHoleInfoDao(): TeeHoleInfoDao
```

Add the import:

```kotlin
import com.example.shottracker.data.local.dao.TeeHoleInfoDao
```

- [ ] **Step 3: Provide DAO in Hilt**

In `DatabaseModule.kt`, add:

```kotlin
@Provides
fun provideTeeHoleInfoDao(database: ShotTrackerDatabase): TeeHoleInfoDao = database.teeHoleInfoDao()
```

Add the import:

```kotlin
import com.example.shottracker.data.local.dao.TeeHoleInfoDao
```

- [ ] **Step 4: Verify compile**

Run: `.\gradlew.bat :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

---

### Task 4: `CourseRepository` — `getHoleHandicaps` / `setHoleHandicaps`

**Files:**
- Modify: `app/src/main/java/com/example/shottracker/domain/repository/CourseRepository.kt`
- Modify: `app/src/main/java/com/example/shottracker/data/repository/CourseRepositoryImpl.kt`

- [ ] **Step 1: Extend the interface**

In `CourseRepository.kt`, add two methods at the end (before the closing brace, after the OSM section header):

```kotlin
    // Per-tee per-hole handicaps (stroke index)
    /** Returns map of holeNumber → stroke index for the given tee. Empty if none set. */
    suspend fun getHoleHandicaps(teeId: Long): Map<Int, Int>

    /** Replaces all handicap rows for the tee with the supplied map (holeNumber → handicap).
     *  Pass an empty map to clear all handicaps for the tee. */
    suspend fun setHoleHandicaps(teeId: Long, holeHandicaps: Map<Int, Int>)
```

- [ ] **Step 2: Inject `TeeHoleInfoDao` into the impl**

In `CourseRepositoryImpl.kt`, add `TeeHoleInfoDao` to the constructor:

```kotlin
import com.example.shottracker.data.local.dao.TeeHoleInfoDao
import com.example.shottracker.data.local.entity.TeeHoleInfoEntity
```

```kotlin
@Singleton
class CourseRepositoryImpl @Inject constructor(
    private val courseDao: CourseDao,
    private val holeInfoDao: HoleInfoDao,
    private val teeDao: TeeDao,
    private val teeHoleInfoDao: TeeHoleInfoDao,
    private val overpassDataSource: OverpassRemoteDataSource
) : CourseRepository {
```

- [ ] **Step 3: Implement `getHoleHandicaps`**

Append to `CourseRepositoryImpl` (before the OSM section):

```kotlin
    override suspend fun getHoleHandicaps(teeId: Long): Map<Int, Int> {
        val rows = teeHoleInfoDao.getHandicapRowsForTee(teeId)
        return rows.mapNotNull { row ->
            val h = row.handicap ?: return@mapNotNull null
            row.holeNumber to h
        }.toMap()
    }
```

- [ ] **Step 4: Implement `setHoleHandicaps`**

Append below `getHoleHandicaps`:

```kotlin
    override suspend fun setHoleHandicaps(teeId: Long, holeHandicaps: Map<Int, Int>) {
        // Look up the tee to find its course, then map holeNumber → holeInfoId.
        val tee = teeDao.getTeeById(teeId) ?: return
        val holes = holeInfoDao.getHolesForCourseSync(tee.courseId)
        val holeInfoIdByNumber: Map<Int, Long> = holes.associate { it.holeNumber to it.holeInfoId }

        val rows = holeHandicaps.mapNotNull { (holeNumber, handicap) ->
            val holeInfoId = holeInfoIdByNumber[holeNumber] ?: return@mapNotNull null
            TeeHoleInfoEntity(
                teeHoleInfoId = 0,
                teeId = teeId,
                holeInfoId = holeInfoId,
                yardage = null,
                handicap = handicap
            )
        }
        teeHoleInfoDao.replaceForTee(teeId, rows)
    }
```

- [ ] **Step 5: Verify compile**

Run: `.\gradlew.bat :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

---

### Task 5: `HandicapCalculator` domain object

**Files:**
- Create: `app/src/main/java/com/example/shottracker/domain/handicap/HandicapCalculator.kt`

- [ ] **Step 1: Create the file**

```kotlin
package com.example.shottracker.domain.handicap

import com.example.shottracker.domain.model.Round
import com.example.shottracker.domain.model.RoundStatus

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

    /**
     * Computes the score differentials map used as input to `computeIndex`.
     *
     * @param rounds        All known rounds, most-recent-first.
     * @param teeLookup     teeId → (rating, slope). Null entries / missing teeIds are skipped.
     */
    fun computeDifferentials(
        rounds: List<Round>,
        teeLookup: suspend (Long) -> Pair<Double?, Int?>?,
    ): suspend () -> Map<Long, Double> = {
        val out = mutableMapOf<Long, Double>()
        for (round in rounds) {
            if (round.status != RoundStatus.COMPLETED) continue
            val score = round.totalScore ?: continue
            val teeId = round.teeId ?: continue
            val (rating, slope) = teeLookup(teeId) ?: continue
            if (rating == null || slope == null) continue
            val pcc = round.pcc ?: 0
            val adjusted = score - (round.totalAdjustment ?: 0)
            out[round.id] = (adjusted - rating - pcc) * 113.0 / slope
        }
        out
    }

    private fun buildFormulaDescription(count: Int, total: Int, adjustment: Double): String {
        val base = if (count == 1) "Lowest of $total" else "Avg of lowest $count of $total"
        val adj = if (adjustment != 0.0) " − ${kotlin.math.abs(adjustment).toInt()}" else ""
        return "$base differentials$adj"
    }
}
```

- [ ] **Step 2: Verify compile**

Run: `.\gradlew.bat :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

---

### Task 6: `HandicapCalculator` unit tests

**Files:**
- Create: `app/src/test/java/com/example/shottracker/domain/handicap/HandicapCalculatorTest.kt`

- [ ] **Step 1: Write the test file**

```kotlin
package com.example.shottracker.domain.handicap

import com.example.shottracker.domain.model.Round
import com.example.shottracker.domain.model.RoundStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class HandicapCalculatorTest {

    private fun round(id: Long): Round = Round(
        id = id,
        courseName = "Test",
        startTime = Instant.EPOCH,
        status = RoundStatus.COMPLETED,
    )

    @Test
    fun `computeIndex returns null with fewer than 3 differentials`() {
        val rounds = listOf(round(1), round(2))
        val diffs = mapOf(1L to 12.0, 2L to 14.0)
        assertNull(HandicapCalculator.computeIndex(rounds, diffs))
    }

    @Test
    fun `computeIndex with 3 rounds uses lowest minus 2`() {
        val rounds = listOf(round(1), round(2), round(3))
        val diffs = mapOf(1L to 12.0, 2L to 14.0, 3L to 16.0)
        val result = HandicapCalculator.computeIndex(rounds, diffs)!!
        // lowest = 12.0, minus 2.0 → 10.0
        assertEquals(10.0, result.index, 0.001)
        assertEquals(setOf(1L), result.countedRoundIds)
        assertEquals("Lowest of 3 differentials − 2", result.formulaDescription)
    }

    @Test
    fun `computeIndex with 20 rounds averages lowest 8`() {
        val rounds = (1..20L).map { round(it) }
        val diffs = (1..20L).associateWith { it.toDouble() } // diffs = 1..20
        val result = HandicapCalculator.computeIndex(rounds, diffs)!!
        // lowest 8 = 1..8, avg = 4.5
        assertEquals(4.5, result.index, 0.001)
        assertEquals((1L..8L).toSet(), result.countedRoundIds)
        assertEquals("Avg of lowest 8 of 20 differentials", result.formulaDescription)
    }

    @Test
    fun `computeIndex rounds to one decimal`() {
        val rounds = (1..3L).map { round(it) }
        val diffs = mapOf(1L to 13.34, 2L to 14.0, 3L to 15.0)
        val result = HandicapCalculator.computeIndex(rounds, diffs)!!
        // lowest = 13.34, minus 2.0 = 11.34, rounded to 1 dp = 11.3
        assertEquals(11.3, result.index, 0.001)
    }

    @Test
    fun `courseHandicap rounds to nearest int`() {
        // 10.0 * 113 / 113 = 10.0 → 10
        assertEquals(10, HandicapCalculator.courseHandicap(10.0, 113))
        // 12.4 * 140 / 113 ≈ 15.36 → 15
        assertEquals(15, HandicapCalculator.courseHandicap(12.4, 140))
        // 0.0 → 0
        assertEquals(0, HandicapCalculator.courseHandicap(0.0, 130))
    }

    @Test
    fun `strokesReceived basic allocation`() {
        // courseHandicap 5: holes with handicap 1..5 get 1 stroke, others 0
        assertEquals(1, HandicapCalculator.strokesReceived(holeHandicap = 1, courseHandicap = 5))
        assertEquals(1, HandicapCalculator.strokesReceived(holeHandicap = 5, courseHandicap = 5))
        assertEquals(0, HandicapCalculator.strokesReceived(holeHandicap = 6, courseHandicap = 5))
    }

    @Test
    fun `strokesReceived for handicap 18 gives every hole 1 stroke`() {
        for (h in 1..18) {
            assertEquals(1, HandicapCalculator.strokesReceived(holeHandicap = h, courseHandicap = 18))
        }
    }

    @Test
    fun `strokesReceived for handicap 27 gives 2 strokes to holes 1-9 and 1 stroke to holes 10-18`() {
        for (h in 1..9) {
            assertEquals(2, HandicapCalculator.strokesReceived(holeHandicap = h, courseHandicap = 27))
        }
        for (h in 10..18) {
            assertEquals(1, HandicapCalculator.strokesReceived(holeHandicap = h, courseHandicap = 27))
        }
    }

    @Test
    fun `strokesReceived for negative course handicap (plus player)`() {
        // courseHandicap = -2 → base = -1, remainder = 16 → holes 1..16 get -1 + 1 = 0, holes 17-18 get -1
        assertEquals(0, HandicapCalculator.strokesReceived(holeHandicap = 1, courseHandicap = -2))
        assertEquals(0, HandicapCalculator.strokesReceived(holeHandicap = 16, courseHandicap = -2))
        assertEquals(-1, HandicapCalculator.strokesReceived(holeHandicap = 17, courseHandicap = -2))
        assertEquals(-1, HandicapCalculator.strokesReceived(holeHandicap = 18, courseHandicap = -2))
    }

    @Test
    fun `netDoubleBogey is par plus 2 plus strokes`() {
        assertEquals(6, HandicapCalculator.netDoubleBogey(par = 4, strokesReceived = 0))
        assertEquals(7, HandicapCalculator.netDoubleBogey(par = 4, strokesReceived = 1))
        assertEquals(5, HandicapCalculator.netDoubleBogey(par = 3, strokesReceived = 0))
    }
}
```

- [ ] **Step 2: Run the tests**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.example.shottracker.domain.handicap.HandicapCalculatorTest"`
Expected: All 10 tests pass.

---

### Task 7: Refactor `HistoryViewModel` to use `HandicapCalculator`

**Files:**
- Modify: `app/src/main/java/com/example/shottracker/feature/history/HistoryViewModel.kt`

- [ ] **Step 1: Replace `computeHandicapIndex` and helpers**

Replace the entire `HistoryViewModel.kt` content with:

```kotlin
package com.example.shottracker.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shottracker.domain.handicap.HandicapCalculator
import com.example.shottracker.domain.model.Round
import com.example.shottracker.domain.repository.CourseRepository
import com.example.shottracker.domain.repository.RoundRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val rounds: List<Round> = emptyList(),
    val differentialsById: Map<Long, Double> = emptyMap(),
    val handicapIndex: Double? = null,
    val handicapEligibleRoundCount: Int = 0,
    val handicapCountedRoundIds: Set<Long> = emptySet(),
    val handicapFormulaDescription: String? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val roundRepository: RoundRepository,
    private val courseRepository: CourseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadRounds()
    }

    private fun loadRounds() {
        viewModelScope.launch {
            roundRepository.getAllRounds().collect { rounds ->
                val differentials = computeDifferentials(rounds)
                val result = HandicapCalculator.computeIndex(rounds, differentials)
                _uiState.value = _uiState.value.copy(
                    rounds = rounds,
                    differentialsById = differentials,
                    handicapIndex = result?.index,
                    handicapEligibleRoundCount = result?.eligibleRoundCount
                        ?: rounds.count { it.totalScore != null && it.teeId != null },
                    handicapCountedRoundIds = result?.countedRoundIds ?: emptySet(),
                    handicapFormulaDescription = result?.formulaDescription,
                    isLoading = false
                )
            }
        }
    }

    private suspend fun computeDifferentials(rounds: List<Round>): Map<Long, Double> {
        val out = mutableMapOf<Long, Double>()
        for (round in rounds) {
            val score = round.totalScore ?: continue
            val teeId = round.teeId ?: continue
            val tee = courseRepository.getTeeById(teeId) ?: continue
            val rating = tee.rating ?: continue
            val slope = tee.slope ?: continue
            val pcc = round.pcc ?: 0
            val adjusted = score - (round.totalAdjustment ?: 0)
            out[round.id] = (adjusted - rating - pcc) * 113.0 / slope
        }
        return out
    }

    fun deleteRound(roundId: Long) {
        viewModelScope.launch {
            roundRepository.deleteRound(roundId)
        }
    }
}
```

- [ ] **Step 2: Verify compile**

Run: `.\gradlew.bat :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Manual sanity check**

Build + install the app (`.\gradlew.bat :app:installDebug`). Open History → the handicap index value and formula subtext should be identical to before the refactor for any existing rounds.

---

### Task 8: `RoundRepository.startNewRound` accepts `handicapIndex`

**Files:**
- Modify: `app/src/main/java/com/example/shottracker/domain/repository/RoundRepository.kt`
- Modify: `app/src/main/java/com/example/shottracker/data/repository/RoundRepositoryImpl.kt`

- [ ] **Step 1: Update the interface signature**

In `RoundRepository.kt`, change the `startNewRound` line to:

```kotlin
    suspend fun startNewRound(
        courseName: String,
        teeId: Long?,
        courseId: Long?,
        holeCount: Int,
        handicapIndex: Double?
    ): Long
```

- [ ] **Step 2: Update the impl**

In `RoundRepositoryImpl.kt`, update `startNewRound`:

```kotlin
    override suspend fun startNewRound(
        courseName: String,
        teeId: Long?,
        courseId: Long?,
        holeCount: Int,
        handicapIndex: Double?
    ): Long {
        val round = RoundEntity(
            teeId = teeId,
            courseName = courseName,
            startTime = Instant.now().toEpochMilli(),
            endTime = null,
            status = RoundStatus.IN_PROGRESS.name,
            holesPlayed = 0,
            totalScore = null,
            totalPutts = null,
            totalPenalties = null,
            pcc = null,
            handicapIndex = handicapIndex
        )
        val newRoundId = roundDao.insertRound(round)

        // Pre-create all hole scores so the scorecard shows every hole from the start
        val parsByHole: Map<Int, Int> = courseId?.let {
            holeInfoDao.getHolesForCourseSync(it).associate { h -> h.holeNumber to h.par }
        } ?: emptyMap()

        val holeScores = (1..holeCount).map { num ->
            HoleScore(
                roundId = newRoundId,
                holeNumber = num,
                par = parsByHole[num] ?: 4
            ).toEntity()
        }
        holeScoreDao.insertHoleScores(holeScores)

        return newRoundId
    }
```

- [ ] **Step 3: Verify compile**

Run: `.\gradlew.bat :app:compileDebugKotlin`
Expected: BUILD FAILED. The build will fail on the existing caller in `NewRoundSetupViewModel.kt`. That's fixed in Task 9. Verify the failure is exactly that and proceed.

---

### Task 9: `NewRoundSetupViewModel` snapshots handicap index at round start

**Files:**
- Modify: `app/src/main/java/com/example/shottracker/feature/round/NewRoundSetupViewModel.kt`

- [ ] **Step 1: Add the calculation + pass the value through**

Replace the `startRound` function and add a helper. The new function body:

```kotlin
    fun startRound(onRoundStarted: (Long) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val resolvedCourseId: Long?
                val courseName = if (_uiState.value.isCreatingCourse) {
                    val name = _uiState.value.courseName.trim()
                    if (name.isEmpty()) {
                        _uiState.value = _uiState.value.copy(
                            error = "Please enter a course name",
                            isLoading = false
                        )
                        return@launch
                    }

                    val courseId = courseRepository.insertCourse(Course(name = name))
                    val holes = (1.._uiState.value.numberOfHoles).map { holeNum ->
                        HoleInfo(courseId = courseId, holeNumber = holeNum, par = 4)
                    }
                    courseRepository.insertHoles(holes)
                    resolvedCourseId = courseId
                    name
                } else {
                    resolvedCourseId = _uiState.value.selectedCourse?.id
                    _uiState.value.selectedCourse?.name ?: "Unknown Course"
                }

                val handicapIndex = computeCurrentHandicapIndex()

                val roundId = roundRepository.startNewRound(
                    courseName = courseName,
                    teeId = _uiState.value.selectedTee?.id,
                    courseId = resolvedCourseId,
                    holeCount = _uiState.value.numberOfHoles,
                    handicapIndex = handicapIndex
                )

                onRoundStarted(roundId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to start round: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    private suspend fun computeCurrentHandicapIndex(): Double? {
        val rounds = roundRepository.getAllRounds().first()
        val differentials = mutableMapOf<Long, Double>()
        for (round in rounds) {
            val score = round.totalScore ?: continue
            val teeId = round.teeId ?: continue
            val tee = courseRepository.getTeeById(teeId) ?: continue
            val rating = tee.rating ?: continue
            val slope = tee.slope ?: continue
            val pcc = round.pcc ?: 0
            val adjusted = score - (round.totalAdjustment ?: 0)
            differentials[round.id] = (adjusted - rating - pcc) * 113.0 / slope
        }
        return HandicapCalculator.computeIndex(rounds, differentials)?.index
    }
```

- [ ] **Step 2: Add the missing imports**

Add to the top of `NewRoundSetupViewModel.kt`:

```kotlin
import com.example.shottracker.domain.handicap.HandicapCalculator
import kotlinx.coroutines.flow.first
```

- [ ] **Step 3: Verify compile**

Run: `.\gradlew.bat :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

---

### Task 10: Course management — `EditHoleHandicapsDialog` state + methods

**Files:**
- Modify: `app/src/main/java/com/example/shottracker/feature/coursemanagement/CourseManagementViewModel.kt`

- [ ] **Step 1: Add state fields**

In `CourseManagementUiState` add three new fields:

```kotlin
data class CourseManagementUiState(
    val courses: List<Course> = emptyList(),
    val courseToDelete: Course? = null,
    val editingCourse: Course? = null,
    val editingHoles: List<HoleInfo> = emptyList(),
    val editingTees: List<Tee> = emptyList(),
    val teeForm: TeeFormState? = null,
    val editingHandicapsTeeId: Long? = null,
    val handicapEdits: Map<Int, String> = emptyMap(),
    val handicapError: String? = null,
    val isSaving: Boolean = false
)
```

- [ ] **Step 2: Add open / cancel / update / clear methods**

Append to `CourseManagementViewModel` (anywhere after the existing tee methods, before the closing brace):

```kotlin
    // Per-hole handicap editor

    fun openHandicapEditor(teeId: Long) {
        viewModelScope.launch {
            val existing = courseRepository.getHoleHandicaps(teeId)
            val holes = _uiState.value.editingHoles
            val edits = holes.associate { hole ->
                hole.holeNumber to (existing[hole.holeNumber]?.toString().orEmpty())
            }
            _uiState.value = _uiState.value.copy(
                editingHandicapsTeeId = teeId,
                handicapEdits = edits,
                handicapError = null
            )
        }
    }

    fun cancelHandicapEditor() {
        _uiState.value = _uiState.value.copy(
            editingHandicapsTeeId = null,
            handicapEdits = emptyMap(),
            handicapError = null
        )
    }

    fun updateHandicapEntry(holeNumber: Int, value: String) {
        val sanitized = value.filter { it.isDigit() }.take(2)
        _uiState.value = _uiState.value.copy(
            handicapEdits = _uiState.value.handicapEdits + (holeNumber to sanitized),
            handicapError = null
        )
    }

    fun clearAllHandicapEdits() {
        val cleared = _uiState.value.handicapEdits.mapValues { "" }
        _uiState.value = _uiState.value.copy(
            handicapEdits = cleared,
            handicapError = null
        )
    }

    fun saveHandicapEdits() {
        val teeId = _uiState.value.editingHandicapsTeeId ?: return
        val edits = _uiState.value.handicapEdits
        val holeCount = _uiState.value.editingHoles.size

        // Validation
        val nonBlank = edits.filterValues { it.isNotBlank() }
        if (nonBlank.isEmpty()) {
            // All blank → clear all handicaps for the tee
            viewModelScope.launch {
                courseRepository.setHoleHandicaps(teeId, emptyMap())
                _uiState.value = _uiState.value.copy(
                    editingHandicapsTeeId = null,
                    handicapEdits = emptyMap(),
                    handicapError = null
                )
            }
            return
        }
        if (nonBlank.size != edits.size) {
            _uiState.value = _uiState.value.copy(
                handicapError = "Either fill every hole or leave them all blank."
            )
            return
        }

        val parsed = edits.mapValues { it.value.toIntOrNull() }
        if (parsed.any { it.value == null }) {
            _uiState.value = _uiState.value.copy(handicapError = "Values must be integers.")
            return
        }
        val values = parsed.values.filterNotNull()
        if (values.any { it < 1 || it > holeCount }) {
            _uiState.value = _uiState.value.copy(
                handicapError = "Values must be between 1 and $holeCount."
            )
            return
        }
        if (values.toSet().size != values.size) {
            _uiState.value = _uiState.value.copy(handicapError = "Values must be unique.")
            return
        }

        val map = parsed.mapNotNull { (k, v) -> v?.let { k to it } }.toMap()
        viewModelScope.launch {
            courseRepository.setHoleHandicaps(teeId, map)
            _uiState.value = _uiState.value.copy(
                editingHandicapsTeeId = null,
                handicapEdits = emptyMap(),
                handicapError = null
            )
        }
    }
```

- [ ] **Step 3: Verify compile**

Run: `.\gradlew.bat :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

---

### Task 11: Course management — "Set Handicaps" button + `EditHoleHandicapsDialog` UI

**Files:**
- Modify: `app/src/main/java/com/example/shottracker/feature/coursemanagement/CourseManagementScreen.kt`

This task assumes the existing `TeeRow` composable renders tee summary text + action icons/buttons (Edit / Delete). Read the file first to confirm the exact location.

- [ ] **Step 1: Read the file to locate the tee row composable**

Read `CourseManagementScreen.kt`. Find the `TeeRow` composable (or the inline `Row` that renders one tee with edit/delete actions). Note the existing imports.

- [ ] **Step 2: Add the "Set Handicaps" button**

In the tee row, add a new `TextButton` after the existing Edit button and before the Delete button:

```kotlin
TextButton(onClick = { onSetHandicaps(tee.id) }) {
    Text("Set Handicaps")
}
```

Add `onSetHandicaps: (Long) -> Unit` to the tee row composable's parameters and thread it through from the caller (the editing-course content composable). At the call site in the course editor, pass `viewModel::openHandicapEditor`.

- [ ] **Step 3: Add `EditHoleHandicapsDialog` composable**

Append to `CourseManagementScreen.kt`, before any private composable helpers at the bottom:

```kotlin
@Composable
private fun EditHoleHandicapsDialog(
    holes: List<HoleInfo>,
    edits: Map<Int, String>,
    error: String?,
    onChange: (Int, String) -> Unit,
    onClearAll: () -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit,
) {
    val holeCount = holes.size
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Set Hole Handicaps") },
        text = {
            Column {
                Text(
                    text = "Assign a stroke index 1..$holeCount to each hole. Each value must be unique.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp)
                ) {
                    items(holes) { hole ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "Hole ${hole.holeNumber} (Par ${hole.par})",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            OutlinedTextField(
                                value = edits[hole.holeNumber].orEmpty(),
                                onValueChange = { onChange(hole.holeNumber, it) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.width(72.dp),
                                textStyle = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                if (error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onClearAll) { Text("Clear All") }
            }
        },
        confirmButton = { Button(onClick = onSave) { Text("Save") } },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancel") } }
    )
}
```

Add the imports near the top of the file (only those not already present):

```kotlin
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.shottracker.domain.model.HoleInfo
```

- [ ] **Step 4: Mount the dialog**

In the course editor content composable (where the edit-tee dialog is shown), add:

```kotlin
if (state.editingHandicapsTeeId != null) {
    EditHoleHandicapsDialog(
        holes = state.editingHoles,
        edits = state.handicapEdits,
        error = state.handicapError,
        onChange = viewModel::updateHandicapEntry,
        onClearAll = viewModel::clearAllHandicapEdits,
        onCancel = viewModel::cancelHandicapEditor,
        onSave = viewModel::saveHandicapEdits,
    )
}
```

- [ ] **Step 5: Verify compile**

Run: `.\gradlew.bat :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

---

### Task 12: `ActiveRoundViewModel` — load tee handicap map + auto-cap in `saveCurrentHole`

**Files:**
- Modify: `app/src/main/java/com/example/shottracker/feature/round/ActiveRoundViewModel.kt`

- [ ] **Step 1: Add `holeHandicaps` and tee data to UI state**

In `ActiveRoundUiState`, add three fields:

```kotlin
val holeHandicaps: Map<Int, Int> = emptyMap(),
val teeSlope: Int? = null,
val teeRating: Double? = null,
```

- [ ] **Step 2: Load the handicap map after the round is loaded**

In `loadRound()`, after `resolveCourseId(round)`, add:

```kotlin
                loadTeeHandicaps(round.teeId)
```

Add the helper method to the class:

```kotlin
    private suspend fun loadTeeHandicaps(teeId: Long?) {
        if (teeId == null) return
        val tee = courseRepository.getTeeById(teeId)
        val map = courseRepository.getHoleHandicaps(teeId)
        _uiState.value = _uiState.value.copy(
            holeHandicaps = map,
            teeSlope = tee?.slope,
            teeRating = tee?.rating
        )
    }
```

- [ ] **Step 3: Add the auto-cap helper**

Append a private helper to `ActiveRoundViewModel`:

```kotlin
    /**
     * Returns the NDB-capped adjustment for the current hole, or null if no adjustment is needed
     * or if any prerequisite for auto-cap is missing (in which case the caller should leave the
     * existing adjustment unchanged).
     *
     * Prerequisites: round.handicapIndex != null, tee.slope != null, hole has a per-tee handicap,
     * round is 18 holes (9-hole skipped per spec v1).
     */
    private fun computeAutoCap(score: Int): Int? {
        val round = _uiState.value.round ?: return null
        val holeInfo = _uiState.value.currentHoleInfo ?: return null
        val holeNumber = _uiState.value.currentHoleNumber
        val handicapIndex = round.handicapIndex ?: return null
        val slope = _uiState.value.teeSlope ?: return null
        val holeHandicap = _uiState.value.holeHandicaps[holeNumber] ?: return null
        // 9-hole rounds out of scope for v1
        // (no reliable way to know holeCount here; treat 18 as the only supported value)
        val courseHandicap = HandicapCalculator.courseHandicap(handicapIndex, slope)
        val strokes = HandicapCalculator.strokesReceived(holeHandicap, courseHandicap)
        val ndbCap = HandicapCalculator.netDoubleBogey(holeInfo.par, strokes)
        return if (score > ndbCap) score - ndbCap else null
    }
```

Add the import:

```kotlin
import com.example.shottracker.domain.handicap.HandicapCalculator
```

- [ ] **Step 4: Wire auto-cap into `saveCurrentHole`**

Replace `saveCurrentHole` with:

```kotlin
    private suspend fun saveCurrentHole() {
        val holeScore = _uiState.value.currentHoleScore ?: return
        val score = _uiState.value.score
        val putts = _uiState.value.putts
        val penalties = _uiState.value.penalties

        // Auto-cap fires only when all prerequisites are present.
        // If they're missing OR computeAutoCap returns null because the score is at/below NDB,
        // we set adjustment to null (clears any stale adjustment for this hole).
        // Manual chip taps in the scorecard edit dialog will overwrite this on save.
        val autoCappedAdjustment = if (score > 0) computeAutoCap(score) else holeScore.adjustment

        roundRepository.updateHoleScoreStats(
            holeScoreId = holeScore.id,
            score = if (score > 0) score else null,
            putts = if (putts > 0) putts else null,
            penalties = if (penalties > 0) penalties else null,
            adjustment = autoCappedAdjustment,
            fairwayHit = null,
            gir = null
        )
    }
```

- [ ] **Step 5: Verify compile**

Run: `.\gradlew.bat :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

---

### Task 13: `ScorecardViewModel` — load tee handicap map

**Files:**
- Modify: `app/src/main/java/com/example/shottracker/feature/scorecard/ScorecardViewModel.kt`

This task assumes `ScorecardViewModel` already loads a `Round` plus its `holeScores` and exposes a `tee` (or `teeId`) in its UI state. Read the file first.

- [ ] **Step 1: Add `holeHandicaps` to `ScorecardUiState`**

In `ScorecardUiState`, add:

```kotlin
val holeHandicaps: Map<Int, Int> = emptyMap(),
```

- [ ] **Step 2: Load handicaps after the round is loaded**

Find the place in `ScorecardViewModel` where `round.teeId` becomes known (typically inside `loadRound` or the equivalent init flow). Add right after the round is set in state:

```kotlin
            round.teeId?.let { teeId ->
                val map = courseRepository.getHoleHandicaps(teeId)
                _uiState.value = _uiState.value.copy(holeHandicaps = map)
            }
```

If `CourseRepository` is not yet injected in this ViewModel, add it as a constructor parameter and bring its import in.

- [ ] **Step 3: Verify compile**

Run: `.\gradlew.bat :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

---

### Task 14: `ScorecardScreen` — conditional `Hcp` column

**Files:**
- Modify: `app/src/main/java/com/example/shottracker/feature/scorecard/ScorecardScreen.kt`

The file is large; read it first. The plan below adapts the same `showAdj` pattern used by the adjustment feature.

- [ ] **Step 1: Compute `showHcp` and pass it into the table composables**

In the scorecard's main composable, derive:

```kotlin
val showHcp = state.holeHandicaps.isNotEmpty()
```

Pass `showHcp = showHcp` and `holeHandicaps = state.holeHandicaps` into `ScorecardHeader`, `HoleScoreRow` (each call), and `SummaryRow`.

- [ ] **Step 2: Update `ScorecardHeader` to render the `Hcp` column conditionally**

Locate the header row. Add a `showHcp: Boolean` parameter. Inside, between the existing `Hole` column and the `Par` column, insert:

```kotlin
if (showHcp) {
    Text(
        text = "Hcp",
        modifier = Modifier.weight(1f),
        style = MaterialTheme.typography.labelSmall,
        textAlign = TextAlign.Center
    )
}
```

- [ ] **Step 3: Update `HoleScoreRow` to render each hole's handicap value**

Add `showHcp: Boolean` and `holeHandicap: Int?` parameters. Between the `Hole` cell and the `Par` cell, add:

```kotlin
if (showHcp) {
    Text(
        text = holeHandicap?.toString() ?: "-",
        modifier = Modifier.weight(1f),
        style = MaterialTheme.typography.bodySmall,
        textAlign = TextAlign.Center
    )
}
```

At the call site, pass `holeHandicap = state.holeHandicaps[holeScore.holeNumber]`.

- [ ] **Step 4: Update `SummaryRow` to render a blank `Hcp` cell**

In the Front 9 / Back 9 summary rows, add the same conditional column with an empty/blank cell:

```kotlin
if (showHcp) {
    Spacer(modifier = Modifier.weight(1f))
}
```

(There's no meaningful "total" for stroke index — it stays blank.)

- [ ] **Step 5: Verify compile**

Run: `.\gradlew.bat :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

---

### Task 15: `RoundDetailViewModel` + `RoundDetailScreen` — mirror Hcp column

**Files:**
- Modify: `app/src/main/java/com/example/shottracker/feature/history/RoundDetailViewModel.kt`
- Modify: `app/src/main/java/com/example/shottracker/feature/history/RoundDetailScreen.kt`

- [ ] **Step 1: Mirror the ViewModel change**

Apply the same change as Task 13 to `RoundDetailViewModel`: add `holeHandicaps: Map<Int, Int>` to its UI state, load via `courseRepository.getHoleHandicaps(teeId)` after the round is loaded.

- [ ] **Step 2: Mirror the screen change**

Apply Task 14 to `RoundDetailScreen.kt`. Header / row / summary composables in this file mirror the ones in `ScorecardScreen` — same inserts.

- [ ] **Step 3: Verify compile**

Run: `.\gradlew.bat :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Verify build**

Run: `.\gradlew.bat :app:assembleDebug`
Expected: BUILD SUCCESSFUL.

---

### Task 16: `TeeHoleInfoDao` instrumented test

**Files:**
- Create: `app/src/androidTest/java/com/example/shottracker/data/local/dao/TeeHoleInfoDaoTest.kt`

This mirrors `CourseDaoCreateCourseTest`. Read that file first to copy the in-memory database setup if unsure of the pattern.

- [ ] **Step 1: Write the test**

```kotlin
package com.example.shottracker.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.shottracker.data.local.ShotTrackerDatabase
import com.example.shottracker.data.local.entity.CourseEntity
import com.example.shottracker.data.local.entity.HoleInfoEntity
import com.example.shottracker.data.local.entity.TeeEntity
import com.example.shottracker.data.local.entity.TeeHoleInfoEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TeeHoleInfoDaoTest {

    private lateinit var db: ShotTrackerDatabase
    private lateinit var dao: TeeHoleInfoDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ShotTrackerDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.teeHoleInfoDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun replaceForTee_overwritesExistingRows() = runTest {
        val courseId = db.courseDao().insertCourse(CourseEntity(name = "Test", city = null, state = null))
        val holes = (1..18).map { HoleInfoEntity(holeInfoId = 0, courseId = courseId, holeNumber = it, par = 4) }
        db.holeInfoDao().insertHoles(holes)
        val teeId = db.teeDao().insertTee(TeeEntity(teeId = 0, courseId = courseId, name = "Black", color = null, rating = 72.0, slope = 132))

        val storedHoles = db.holeInfoDao().getHolesForCourseSync(courseId)
        val first = storedHoles.take(9).mapIndexed { idx, h ->
            TeeHoleInfoEntity(teeId = teeId, holeInfoId = h.holeInfoId, yardage = null, handicap = idx + 1)
        }
        dao.replaceForTee(teeId, first)
        assertEquals(9, dao.getForTee(teeId).size)

        // Replace with a different set
        val second = storedHoles.take(18).mapIndexed { idx, h ->
            TeeHoleInfoEntity(teeId = teeId, holeInfoId = h.holeInfoId, yardage = null, handicap = idx + 1)
        }
        dao.replaceForTee(teeId, second)
        val rows = dao.getForTee(teeId)
        assertEquals(18, rows.size)
        assertEquals((1..18).toSet(), rows.mapNotNull { it.handicap }.toSet())
    }

    @Test
    fun getHandicapRowsForTee_joinsHoleNumber() = runTest {
        val courseId = db.courseDao().insertCourse(CourseEntity(name = "Test", city = null, state = null))
        val holes = (1..18).map { HoleInfoEntity(holeInfoId = 0, courseId = courseId, holeNumber = it, par = 4) }
        db.holeInfoDao().insertHoles(holes)
        val teeId = db.teeDao().insertTee(TeeEntity(teeId = 0, courseId = courseId, name = "Black", color = null, rating = 72.0, slope = 132))
        val stored = db.holeInfoDao().getHolesForCourseSync(courseId)
        val rows = stored.mapIndexed { idx, h ->
            TeeHoleInfoEntity(teeId = teeId, holeInfoId = h.holeInfoId, yardage = null, handicap = idx + 1)
        }
        dao.replaceForTee(teeId, rows)

        val joined = dao.getHandicapRowsForTee(teeId)
        assertEquals(18, joined.size)
        joined.forEach { row ->
            // Hole at index i has holeNumber i+1, and we assigned handicap = i+1 above.
            assertEquals(row.holeNumber, row.handicap)
        }
    }
}
```

- [ ] **Step 2: Run the test**

Connect a device or emulator. Run: `.\gradlew.bat :app:connectedDebugAndroidTest --tests "com.example.shottracker.data.local.dao.TeeHoleInfoDaoTest"`
Expected: 2 tests pass.

If no device is available, document this as deferred to manual testing (acceptable per the project's existing posture) and proceed.

---

### Task 17: Manual smoke test plan

**Files:** none — manual exercise.

Build + install: `.\gradlew.bat :app:installDebug`

- [ ] **Step 1: Migration smoke test**

If running on a fresh install, skip. If upgrading from a v6 build with existing data:
- Open History. Verify existing rounds load. Their differentials and handicap-index value should be identical to before the upgrade.
- Open Course Management. Existing courses + tees still appear with all their data.

- [ ] **Step 2: Set handicaps on a tee**

- Open Course Management → tap a course → expand into editor.
- Tap `Set Handicaps` on a tee.
- Leave all rows blank → tap Save. Reopen dialog → all rows still blank.
- Fill each row with 1..18 unique → Save. Reopen → values persist.
- Try invalid combinations:
  - Duplicate two values → error "Values must be unique."
  - Leave one blank → error "Either fill every hole or leave them all blank."
  - Enter "25" → error "Values must be between 1 and 18."

- [ ] **Step 3: Hcp column visibility**

- Start a round on a course where the selected tee has handicaps. Open the scorecard.
- Confirm `Hcp` column appears between `Hole` and `Par`. Each hole shows the assigned stroke index.
- End / discard. Start a new round on a course where the tee has NO handicaps → `Hcp` column hidden.

- [ ] **Step 4: Auto-cap during play**

Prerequisites: at least 3 completed rounds in History so a handicap index exists. Pick a tee with handicaps + slope set.

- Start a round.
- On hole 1: tap Shots until score = 10. Open scorecard, open hole-1 edit dialog. Verify `Adj` chip shows a non-zero value (= score - NDB cap).
- Hand-verify: cap = 4 + 2 + strokes-received(holeHandicap=1, courseHandicap=round(handicapIndex × slope / 113)). Adjustment should equal score - cap.
- Long-press Shots to decrement score down to 5 (≤ cap). Reopen the dialog → `Adj` chip shows `-`.

- [ ] **Step 5: Manual override (per-save-only) test**

- On hole 2: tap Shots to push score over NDB. Auto-cap fills the chip.
- Open the edit dialog. Long-press Adj down by 1. Save.
- Open the dialog again → manual value persists.
- Tap Shots once more in the active-round bottom bar → auto-cap recomputes; open the dialog → chip now shows the auto-capped value (manual override clobbered). This is the documented v1 trade-off.

- [ ] **Step 6: No prerequisites → no auto-cap**

- Fresh install (no prior rounds). Start a round, set shots over what would be an NDB. Adj chip stays `-`.
- Manually tap the Adj chip in the dialog → value persists.

- [ ] **Step 7: 9-hole skip**

- Start a 9-hole round on a course where the tee has handicaps + slope, and a handicap index is available.
- Score over NDB → adjustment is NOT auto-filled (skipped per v1).
- Manual chip taps still work.

---

## Self-Review Checklist

After implementing all tasks:

- [ ] **Spec coverage:** every section of `2026-05-28-per-tee-hole-handicaps-design.md` maps to a task above. Schema → T1/T2/T3. DAO → T3. Repository → T4. Calculator → T5/T6/T7. Round-start seeding → T8/T9. UI: course mgmt → T10/T11. UI: auto-cap → T12. UI: Hcp column → T13/T14/T15. Tests → T6/T16. Manual smoke → T17.
- [ ] **Placeholder scan:** no "TBD", no "similar to X", no generic "handle edge cases".
- [ ] **Type consistency:** `HandicapCalculator.courseHandicap` returns `Int`; `strokesReceived` returns `Int`; `holeHandicaps: Map<Int, Int>` everywhere; `handicapIndex: Double?` everywhere. `setHoleHandicaps` parameter name is `holeHandicaps`.
- [ ] **Backwards compatibility:** rounds created pre-v7 have `handicapIndex = null` → auto-cap skipped. Tees with no handicaps configured → `Hcp` column hidden + auto-cap skipped. Manual chip tap still works in all configurations.
