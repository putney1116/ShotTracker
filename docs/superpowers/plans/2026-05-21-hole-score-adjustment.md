# Hole Score Adjustment Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a per-hole `adjustment` value (stored on `hole_scores`) and an aggregated `totalAdjustment` on `rounds`, with edit-dialog control, conditional scorecard column and summary item, history card line, and adjusted-score-based handicap differential.

**Architecture:** Two nullable INTEGER columns added via Room migration 4 → 5. Domain models pick up the new fields. The scorecard edit dialog gains a `combinedClickable` "Adj: N" chip (tap = +1, long-press = −1, clamped 0..10). Scorecard table and top summary panel render extra Adj cells conditionally based on `holeScores.any { (it.adjustment ?: 0) > 0 }`. History card and handicap differential calculations switch to `score − totalAdjustment`. ScorecardScreen and RoundDetailScreen contain mirrored duplicated code — every UI change is applied in BOTH files for parity.

**Tech Stack:** Kotlin 2.2.10, Jetpack Compose (Material 3), Room 2.8.4, Hilt DI.

**Spec:** `docs/superpowers/specs/2026-05-21-hole-score-adjustment-design.md`

**Repository note:** Project is NOT a git repository. No `git commit` steps. Each task ends with a verification step (compile or assemble).

**Testing scope deviation:** No automated tests added. `ScorecardViewModel`, `RoundDetailViewModel`, and `HistoryViewModel` currently have no unit tests; the test infrastructure needed to add them (fakes for `RoundRepository`, `CourseRepository`) is unbuilt and the `ActiveRoundViewModel.LocationService` coupling already led the previous feature down the same path. Manual smoke testing in the final task covers the paths.

---

## File Inventory

**Modified files:**

- `app/src/main/java/com/example/shottracker/data/local/entity/HoleScoreEntity.kt` — add `adjustment: Int?`
- `app/src/main/java/com/example/shottracker/data/local/entity/RoundEntity.kt` — add `totalAdjustment: Int?`
- `app/src/main/java/com/example/shottracker/data/local/Migrations.kt` — add `MIGRATION_4_5`
- `app/src/main/java/com/example/shottracker/data/local/ShotTrackerDatabase.kt` — bump `version` to 5
- `app/src/main/java/com/example/shottracker/core/di/DatabaseModule.kt` — register `MIGRATION_4_5`
- `app/src/main/java/com/example/shottracker/domain/model/HoleScore.kt` — add `adjustment: Int?` + `adjustedScore` getter
- `app/src/main/java/com/example/shottracker/domain/model/Round.kt` — add `totalAdjustment: Int?`
- `app/src/main/java/com/example/shottracker/data/mapper/RoundMapper.kt` — round-trip new fields
- `app/src/main/java/com/example/shottracker/data/local/dao/HoleScoreDao.kt` — extend `updateHoleScoreStats`, add `getTotalAdjustment`
- `app/src/main/java/com/example/shottracker/data/local/dao/RoundDao.kt` — extend `updateRoundStats`
- `app/src/main/java/com/example/shottracker/domain/repository/RoundRepository.kt` — extend `updateHoleScoreStats`, add `recalculateRoundTotals`
- `app/src/main/java/com/example/shottracker/data/repository/RoundRepositoryImpl.kt` — implement the new interface
- `app/src/main/java/com/example/shottracker/feature/scorecard/ScorecardViewModel.kt` — add edit-adjustment state + methods + getters
- `app/src/main/java/com/example/shottracker/feature/history/RoundDetailViewModel.kt` — same as above + handicap differential change + `recalculateRoundTotals` call
- `app/src/main/java/com/example/shottracker/feature/round/ActiveRoundViewModel.kt` — preserve existing adjustment in `saveCurrentHole`
- `app/src/main/java/com/example/shottracker/feature/history/HistoryViewModel.kt` — differential formula uses adjusted score
- `app/src/main/java/com/example/shottracker/feature/scorecard/ScorecardScreen.kt` — Adj chip in dialog, conditional Adj column + summary item
- `app/src/main/java/com/example/shottracker/feature/history/RoundDetailScreen.kt` — mirror identical changes
- `app/src/main/java/com/example/shottracker/feature/history/HistoryScreen.kt` — "Adjusted Score:" line in round card

No new files. No test files.

**Verification commands (Windows PowerShell):**
- `.\gradlew.bat :app:compileDebugKotlin` — fast compile check
- `.\gradlew.bat :app:assembleDebug` — full APK build

---

### Task 1: Add `adjustment` and `totalAdjustment` columns to schema

**Files:**
- Modify: `app/src/main/java/com/example/shottracker/data/local/entity/HoleScoreEntity.kt`
- Modify: `app/src/main/java/com/example/shottracker/data/local/entity/RoundEntity.kt`
- Modify: `app/src/main/java/com/example/shottracker/data/local/Migrations.kt`
- Modify: `app/src/main/java/com/example/shottracker/data/local/ShotTrackerDatabase.kt`
- Modify: `app/src/main/java/com/example/shottracker/core/di/DatabaseModule.kt`

- [ ] **Step 1: Add `adjustment` field to `HoleScoreEntity`**

In `HoleScoreEntity.kt`, append a new field at the end of the data class. The full data class becomes:

```kotlin
data class HoleScoreEntity(
    @PrimaryKey(autoGenerate = true)
    val holeScoreId: Long = 0,
    val roundId: Long,
    val holeNumber: Int,
    val par: Int,
    val score: Int?,
    val putts: Int?,
    val penalties: Int?,
    val fairwayHit: Boolean?,
    val greenInRegulation: Boolean?,
    val adjustment: Int? = null
)
```

- [ ] **Step 2: Add `totalAdjustment` field to `RoundEntity`**

In `RoundEntity.kt`, append `totalAdjustment: Int?` after `pcc`. The full data class becomes:

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
    val totalAdjustment: Int? = null
)
```

- [ ] **Step 3: Add `MIGRATION_4_5` to `Migrations.kt`**

Append to `Migrations.kt` (after the existing `MIGRATION_3_4` block):

```kotlin
/**
 * v4 -> v5: added per-hole score adjustment for handicap (Net Double Bogey / ESC).
 *   hole_scores.adjustment (nullable INTEGER; null/0 = no adjustment)
 *   rounds.totalAdjustment (nullable INTEGER; aggregate over the round's holes)
 */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE hole_scores ADD COLUMN adjustment INTEGER")
        db.execSQL("ALTER TABLE rounds ADD COLUMN totalAdjustment INTEGER")
    }
}
```

- [ ] **Step 4: Bump database version**

In `ShotTrackerDatabase.kt`, change `version = 4` to `version = 5`. The `@Database(...)` annotation block ends with `version = 5,` and `exportSchema = false`.

- [ ] **Step 5: Register the migration in `DatabaseModule.kt`**

Add the import alongside the other migration imports:

```kotlin
import com.example.shottracker.data.local.MIGRATION_4_5
```

Change the existing `.addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)` call to:

```kotlin
.addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
```

- [ ] **Step 6: Verify compile**

Run: `.\gradlew.bat :app:compileDebugKotlin`

Expected: `BUILD SUCCESSFUL`. KSP regenerates Room code with the new columns and the existing DAOs may now have unused-parameter warnings (acceptable until Task 3 updates them).

---

### Task 2: Plumb `adjustment` through domain models and mapper

**Files:**
- Modify: `app/src/main/java/com/example/shottracker/domain/model/HoleScore.kt`
- Modify: `app/src/main/java/com/example/shottracker/domain/model/Round.kt`
- Modify: `app/src/main/java/com/example/shottracker/data/mapper/RoundMapper.kt`

- [ ] **Step 1: Add `adjustment` to `HoleScore` domain model with derived `adjustedScore`**

In `HoleScore.kt`, add `adjustment: Int? = null` and an `adjustedScore` getter. The full data class becomes:

```kotlin
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
```

- [ ] **Step 2: Add `totalAdjustment` to `Round` domain model**

In `Round.kt`, add `val totalAdjustment: Int? = null` after `pcc`. The full data class becomes:

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
    val holeScores: List<HoleScore> = emptyList()
) {
    val startDateTime: LocalDateTime
        get() = LocalDateTime.ofInstant(startTime, ZoneId.systemDefault())

    val endDateTime: LocalDateTime?
        get() = endTime?.let { LocalDateTime.ofInstant(it, ZoneId.systemDefault()) }

    val isActive: Boolean
        get() = status == RoundStatus.IN_PROGRESS

    val scoreToPar: Int?
        get() {
            val score = totalScore ?: return null
            val par = holeScores.filter { it.score != null }.sumOf { it.par }
            return if (par > 0) score - par else null
        }
}
```

- [ ] **Step 3: Round-trip `adjustment` and `totalAdjustment` in `RoundMapper.kt`**

Replace `fun RoundEntity.toDomain(...)`, `fun Round.toEntity()`, `fun HoleScoreEntity.toDomain(...)`, and `fun HoleScore.toEntity()` with the following (Shot mappers untouched):

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
    totalAdjustment = totalAdjustment
)

fun HoleScoreEntity.toDomain(shots: List<Shot> = emptyList()): HoleScore = HoleScore(
    id = holeScoreId,
    roundId = roundId,
    holeNumber = holeNumber,
    par = par,
    score = score,
    putts = putts,
    penalties = penalties,
    fairwayHit = fairwayHit,
    greenInRegulation = greenInRegulation,
    adjustment = adjustment,
    shots = shots
)

fun HoleScore.toEntity(): HoleScoreEntity = HoleScoreEntity(
    holeScoreId = id,
    roundId = roundId,
    holeNumber = holeNumber,
    par = par,
    score = score,
    putts = putts,
    penalties = penalties,
    fairwayHit = fairwayHit,
    greenInRegulation = greenInRegulation,
    adjustment = adjustment
)
```

- [ ] **Step 4: Verify compile**

Run: `.\gradlew.bat :app:compileDebugKotlin`

Expected: `BUILD SUCCESSFUL`.

---

### Task 3: Extend DAOs for adjustment

**Files:**
- Modify: `app/src/main/java/com/example/shottracker/data/local/dao/HoleScoreDao.kt`
- Modify: `app/src/main/java/com/example/shottracker/data/local/dao/RoundDao.kt`

- [ ] **Step 1: Extend `HoleScoreDao.updateHoleScoreStats` and add `getTotalAdjustment`**

In `HoleScoreDao.kt`, replace the existing `updateHoleScoreStats` declaration:

```kotlin
@Query("UPDATE hole_scores SET score = :score, putts = :putts, penalties = :penalties, fairwayHit = :fairwayHit, greenInRegulation = :gir WHERE holeScoreId = :holeScoreId")
suspend fun updateHoleScoreStats(holeScoreId: Long, score: Int?, putts: Int?, penalties: Int?, fairwayHit: Boolean?, gir: Boolean?)
```

with this expanded signature (now includes `adjustment`):

```kotlin
@Query("UPDATE hole_scores SET score = :score, putts = :putts, penalties = :penalties, adjustment = :adjustment, fairwayHit = :fairwayHit, greenInRegulation = :gir WHERE holeScoreId = :holeScoreId")
suspend fun updateHoleScoreStats(
    holeScoreId: Long,
    score: Int?,
    putts: Int?,
    penalties: Int?,
    adjustment: Int?,
    fairwayHit: Boolean?,
    gir: Boolean?
)
```

Then append a new query method below `getTotalPenalties` (so all the totals stay grouped together):

```kotlin
@Query("SELECT SUM(adjustment) FROM hole_scores WHERE roundId = :roundId AND adjustment IS NOT NULL")
suspend fun getTotalAdjustment(roundId: Long): Int?
```

- [ ] **Step 2: Extend `RoundDao.updateRoundStats`**

In `RoundDao.kt`, replace the existing `updateRoundStats` declaration:

```kotlin
@Query("UPDATE rounds SET holesPlayed = :holesPlayed, totalScore = :totalScore, totalPutts = :totalPutts, totalPenalties = :totalPenalties WHERE roundId = :roundId")
suspend fun updateRoundStats(roundId: Long, holesPlayed: Int, totalScore: Int?, totalPutts: Int?, totalPenalties: Int?)
```

with the expanded version:

```kotlin
@Query("UPDATE rounds SET holesPlayed = :holesPlayed, totalScore = :totalScore, totalPutts = :totalPutts, totalPenalties = :totalPenalties, totalAdjustment = :totalAdjustment WHERE roundId = :roundId")
suspend fun updateRoundStats(
    roundId: Long,
    holesPlayed: Int,
    totalScore: Int?,
    totalPutts: Int?,
    totalPenalties: Int?,
    totalAdjustment: Int?
)
```

- [ ] **Step 3: Verify compile**

Run: `.\gradlew.bat :app:compileDebugKotlin`

Expected: compile errors at every CALLER of `updateHoleScoreStats` and `updateRoundStats` because they're now missing the new required parameters. Specifically:
- `RoundRepositoryImpl.kt` (Tasks 4 will fix)
- Existing tests, if any, that call these methods

This is the expected red state. The next task makes the callers compile.

---

### Task 4: Extend repository for adjustment + `recalculateRoundTotals`

**Files:**
- Modify: `app/src/main/java/com/example/shottracker/domain/repository/RoundRepository.kt`
- Modify: `app/src/main/java/com/example/shottracker/data/repository/RoundRepositoryImpl.kt`

- [ ] **Step 1: Update `RoundRepository` interface**

In `RoundRepository.kt`, replace the `updateHoleScoreStats` interface method:

```kotlin
suspend fun updateHoleScoreStats(holeScoreId: Long, score: Int?, putts: Int?, penalties: Int?, fairwayHit: Boolean?, gir: Boolean?)
```

with the expanded version, and add `recalculateRoundTotals` immediately below:

```kotlin
suspend fun updateHoleScoreStats(
    holeScoreId: Long,
    score: Int?,
    putts: Int?,
    penalties: Int?,
    adjustment: Int?,
    fairwayHit: Boolean?,
    gir: Boolean?
)

/**
 * Recalculates and persists the round's aggregate stats (totalScore, totalPutts,
 * totalPenalties, totalAdjustment, holesPlayed) from current hole_scores rows.
 * Used after edits to a completed round so the History card reflects the new totals.
 */
suspend fun recalculateRoundTotals(roundId: Long)
```

- [ ] **Step 2: Update `RoundRepositoryImpl.updateHoleScoreStats`**

In `RoundRepositoryImpl.kt`, locate the existing `updateHoleScoreStats` override and replace it with:

```kotlin
override suspend fun updateHoleScoreStats(
    holeScoreId: Long,
    score: Int?,
    putts: Int?,
    penalties: Int?,
    adjustment: Int?,
    fairwayHit: Boolean?,
    gir: Boolean?
) {
    holeScoreDao.updateHoleScoreStats(holeScoreId, score, putts, penalties, adjustment, fairwayHit, gir)
}
```

- [ ] **Step 3: Update `RoundRepositoryImpl.updateRoundStatus`**

Locate `updateRoundStatus` (around line 107). Replace its body to include the totalAdjustment aggregation:

```kotlin
override suspend fun updateRoundStatus(roundId: Long, status: RoundStatus) {
    val endTime = if (status == RoundStatus.COMPLETED || status == RoundStatus.ABANDONED) {
        Instant.now().toEpochMilli()
    } else null
    roundDao.updateRoundStatus(roundId, status.name, endTime)

    // Update stats
    val totalScore = holeScoreDao.getTotalScore(roundId)
    val totalPutts = holeScoreDao.getTotalPutts(roundId)
    val totalPenalties = holeScoreDao.getTotalPenalties(roundId)
    val totalAdjustment = holeScoreDao.getTotalAdjustment(roundId)
    val holesPlayed = holeScoreDao.getCompletedHoleCount(roundId)
    roundDao.updateRoundStats(roundId, holesPlayed, totalScore, totalPutts, totalPenalties, totalAdjustment)
}
```

- [ ] **Step 4: Implement `recalculateRoundTotals` in `RoundRepositoryImpl`**

Add this method anywhere in `RoundRepositoryImpl.kt` (a good spot is right after `updateRoundStatus`):

```kotlin
override suspend fun recalculateRoundTotals(roundId: Long) {
    val totalScore = holeScoreDao.getTotalScore(roundId)
    val totalPutts = holeScoreDao.getTotalPutts(roundId)
    val totalPenalties = holeScoreDao.getTotalPenalties(roundId)
    val totalAdjustment = holeScoreDao.getTotalAdjustment(roundId)
    val holesPlayed = holeScoreDao.getCompletedHoleCount(roundId)
    roundDao.updateRoundStats(roundId, holesPlayed, totalScore, totalPutts, totalPenalties, totalAdjustment)
}
```

- [ ] **Step 5: Verify compile**

Run: `.\gradlew.bat :app:compileDebugKotlin`

Expected: ViewModel call sites of `updateHoleScoreStats` are still broken (missing the `adjustment` parameter). Specifically `ScorecardViewModel.kt`, `RoundDetailViewModel.kt`, and `ActiveRoundViewModel.kt`. The next tasks fix each.

---

### Task 5: Update `ActiveRoundViewModel.saveCurrentHole`

**Files:**
- Modify: `app/src/main/java/com/example/shottracker/feature/round/ActiveRoundViewModel.kt`

This VM doesn't expose adjustment editing (per the spec — adjustment lives only in the scorecard edit dialog). But it must NOT overwrite an existing adjustment to null when it auto-saves the current hole on every shot/putt/penalty change.

- [ ] **Step 1: Pass existing adjustment through `saveCurrentHole`**

Locate `private suspend fun saveCurrentHole()` (around line 417). Replace it with:

```kotlin
private suspend fun saveCurrentHole() {
    val holeScore = _uiState.value.currentHoleScore ?: return
    val score = _uiState.value.score
    val putts = _uiState.value.putts
    val penalties = _uiState.value.penalties

    roundRepository.updateHoleScoreStats(
        holeScoreId = holeScore.id,
        score = if (score > 0) score else null,
        putts = if (putts > 0) putts else null,
        penalties = if (penalties > 0) penalties else null,
        adjustment = holeScore.adjustment,
        fairwayHit = null,
        gir = null
    )
}
```

The `adjustment = holeScore.adjustment` line is the only addition — preserves whatever the user set in the scorecard.

- [ ] **Step 2: Verify compile**

Run: `.\gradlew.bat :app:compileDebugKotlin`

Expected: still broken on `ScorecardViewModel` and `RoundDetailViewModel`. Tasks 6 and 7 fix those.

---

### Task 6: Update `ScorecardViewModel` with adjustment edit state

**Files:**
- Modify: `app/src/main/java/com/example/shottracker/feature/scorecard/ScorecardViewModel.kt`

- [ ] **Step 1: Add `editAdjustment` to `ScorecardUiState`**

Replace the existing `ScorecardUiState` data class with:

```kotlin
data class ScorecardUiState(
    val round: Round? = null,
    val holeScores: List<HoleScore> = emptyList(),
    val editingHole: HoleScore? = null,
    val editShots: Int? = null,
    val editPutts: Int? = null,
    val editPenalties: Int? = null,
    val editAdjustment: Int? = null,
    val isLoading: Boolean = true
)
```

- [ ] **Step 2: Initialize `editAdjustment` in `startEditingHole`**

Replace `startEditingHole`:

```kotlin
fun startEditingHole(holeScore: HoleScore) {
    val shots = holeScore.score?.let { it - (holeScore.putts ?: 0) - (holeScore.penalties ?: 0) }
    _uiState.value = _uiState.value.copy(
        editingHole = holeScore,
        editShots = shots,
        editPutts = holeScore.putts,
        editPenalties = holeScore.penalties,
        editAdjustment = holeScore.adjustment
    )
}
```

- [ ] **Step 3: Add `incrementEditAdjustment` and `decrementEditAdjustment`**

Add these methods to the class (a good location is right after `onEditPenaltiesChanged`):

```kotlin
fun incrementEditAdjustment() {
    val current = _uiState.value.editAdjustment ?: 0
    _uiState.value = _uiState.value.copy(
        editAdjustment = (current + 1).coerceIn(0, 10)
    )
}

fun decrementEditAdjustment() {
    val current = _uiState.value.editAdjustment ?: 0
    if (current <= 0) return
    _uiState.value = _uiState.value.copy(editAdjustment = current - 1)
}
```

- [ ] **Step 4: Update `saveHoleEdit` to pass `adjustment`**

Replace `saveHoleEdit`:

```kotlin
fun saveHoleEdit() {
    viewModelScope.launch {
        val editingHole = _uiState.value.editingHole ?: return@launch

        val shots = _uiState.value.editShots ?: 0
        val putts = _uiState.value.editPutts ?: 0
        val penalties = _uiState.value.editPenalties ?: 0
        val computedScore = shots + putts + penalties
        val adjustment = _uiState.value.editAdjustment?.takeIf { it > 0 }

        roundRepository.updateHoleScoreStats(
            holeScoreId = editingHole.id,
            score = if (computedScore > 0) computedScore else null,
            putts = _uiState.value.editPutts,
            penalties = _uiState.value.editPenalties,
            adjustment = adjustment,
            fairwayHit = editingHole.fairwayHit,
            gir = editingHole.greenInRegulation
        )

        cancelEdit()
    }
}
```

- [ ] **Step 5: Update `cancelEdit` to reset `editAdjustment`**

Replace `cancelEdit`:

```kotlin
fun cancelEdit() {
    _uiState.value = _uiState.value.copy(
        editingHole = null,
        editShots = null,
        editPutts = null,
        editPenalties = null,
        editAdjustment = null
    )
}
```

- [ ] **Step 6: Add derived getters `totalAdjustment` and `adjustedScore`**

Add these getters after the existing `scoreToPar` getter at the bottom of the class:

```kotlin
val totalAdjustment: Int
    get() = _uiState.value.holeScores.sumOf { it.adjustment ?: 0 }

val adjustedScore: Int
    get() = totalScore - totalAdjustment
```

- [ ] **Step 7: Verify compile**

Run: `.\gradlew.bat :app:compileDebugKotlin`

Expected: still broken on `RoundDetailViewModel`. Task 7 fixes it.

---

### Task 7: Update `RoundDetailViewModel` mirror + handicap differential + `recalculateRoundTotals`

**Files:**
- Modify: `app/src/main/java/com/example/shottracker/feature/history/RoundDetailViewModel.kt`

- [ ] **Step 1: Add `editAdjustment` to `RoundDetailUiState`**

Replace the data class:

```kotlin
data class RoundDetailUiState(
    val round: Round? = null,
    val holeScores: List<HoleScore> = emptyList(),
    val tee: Tee? = null,
    val availableTees: List<Tee> = emptyList(),
    val isEditing: Boolean = false,
    val editingHole: HoleScore? = null,
    val editShots: Int? = null,
    val editPutts: Int? = null,
    val editPenalties: Int? = null,
    val editAdjustment: Int? = null,
    val isLoading: Boolean = true
)
```

- [ ] **Step 2: Initialize `editAdjustment` in `startEditingHole`**

Replace `startEditingHole`:

```kotlin
fun startEditingHole(holeScore: HoleScore) {
    val shots = holeScore.score?.let { it - (holeScore.putts ?: 0) - (holeScore.penalties ?: 0) }
    _uiState.value = _uiState.value.copy(
        editingHole = holeScore,
        editShots = shots,
        editPutts = holeScore.putts,
        editPenalties = holeScore.penalties,
        editAdjustment = holeScore.adjustment
    )
}
```

- [ ] **Step 3: Add `incrementEditAdjustment` and `decrementEditAdjustment`**

Add these methods (a good location is right after `onEditPenaltiesChanged`):

```kotlin
fun incrementEditAdjustment() {
    val current = _uiState.value.editAdjustment ?: 0
    _uiState.value = _uiState.value.copy(
        editAdjustment = (current + 1).coerceIn(0, 10)
    )
}

fun decrementEditAdjustment() {
    val current = _uiState.value.editAdjustment ?: 0
    if (current <= 0) return
    _uiState.value = _uiState.value.copy(editAdjustment = current - 1)
}
```

- [ ] **Step 4: Update `saveHoleEdit` to pass `adjustment` and recalc round totals**

Replace `saveHoleEdit`:

```kotlin
fun saveHoleEdit() {
    viewModelScope.launch {
        val editingHole = _uiState.value.editingHole ?: return@launch
        val shots = _uiState.value.editShots ?: 0
        val putts = _uiState.value.editPutts ?: 0
        val penalties = _uiState.value.editPenalties ?: 0
        val computedScore = shots + putts + penalties
        val adjustment = _uiState.value.editAdjustment?.takeIf { it > 0 }

        roundRepository.updateHoleScoreStats(
            holeScoreId = editingHole.id,
            score = if (computedScore > 0) computedScore else null,
            putts = _uiState.value.editPutts,
            penalties = _uiState.value.editPenalties,
            adjustment = adjustment,
            fairwayHit = editingHole.fairwayHit,
            gir = editingHole.greenInRegulation
        )
        roundRepository.recalculateRoundTotals(roundId)
        cancelHoleEdit()
        loadRound()
    }
}
```

- [ ] **Step 5: Update `cancelHoleEdit` to reset `editAdjustment`**

Replace `cancelHoleEdit`:

```kotlin
fun cancelHoleEdit() {
    _uiState.value = _uiState.value.copy(
        editingHole = null,
        editShots = null,
        editPutts = null,
        editPenalties = null,
        editAdjustment = null
    )
}
```

- [ ] **Step 6: Update `exitEditMode` to reset `editAdjustment`**

Replace `exitEditMode`:

```kotlin
fun exitEditMode() {
    _uiState.value = _uiState.value.copy(
        isEditing = false,
        editingHole = null,
        editShots = null,
        editPutts = null,
        editPenalties = null,
        editAdjustment = null
    )
}
```

- [ ] **Step 7: Add `totalAdjustment` and `adjustedScore` getters; update `handicapDifferential`**

After the existing `scoreToPar` getter and BEFORE `handicapDifferential`, add:

```kotlin
val totalAdjustment: Int
    get() = _uiState.value.holeScores.sumOf { it.adjustment ?: 0 }

val adjustedScore: Int
    get() = totalScore - totalAdjustment
```

Then replace `handicapDifferential` to use the adjusted score:

```kotlin
/**
 * Handicap differential = (adjustedScore - course rating - PCC) * 113 / slope
 * Uses the adjusted score (totalScore − totalAdjustment) per USGA / WHS rules.
 * Returns null if score, rating, or slope are missing.
 */
val handicapDifferential: Double?
    get() {
        val tee = _uiState.value.tee ?: return null
        val rating = tee.rating ?: return null
        val slope = tee.slope ?: return null
        val score = totalScore
        if (score == 0) return null
        val pcc = _uiState.value.round?.pcc ?: 0
        val adjusted = score - totalAdjustment
        return (adjusted - rating - pcc) * 113.0 / slope
    }
```

- [ ] **Step 8: Verify compile**

Run: `.\gradlew.bat :app:compileDebugKotlin`

Expected: `BUILD SUCCESSFUL`.

---

### Task 8: Update `HistoryViewModel` differential formula

**Files:**
- Modify: `app/src/main/java/com/example/shottracker/feature/history/HistoryViewModel.kt`

- [ ] **Step 1: Replace the differential loop in `loadRounds`**

Locate `loadRounds()`. Inside the `for (round in rounds)` loop, replace the differential calculation:

```kotlin
private fun loadRounds() {
    viewModelScope.launch {
        roundRepository.getAllRounds().collect { rounds ->
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
            val (index, eligibleCount) = computeHandicapIndex(rounds, differentials)
            _uiState.value = _uiState.value.copy(
                rounds = rounds,
                differentialsById = differentials,
                handicapIndex = index,
                handicapEligibleRoundCount = eligibleCount,
                isLoading = false
            )
        }
    }
}
```

The only change is the new `val adjusted = ...` line and the formula now uses `adjusted` instead of `score`.

- [ ] **Step 2: Verify compile**

Run: `.\gradlew.bat :app:compileDebugKotlin`

Expected: `BUILD SUCCESSFUL`.

---

### Task 9: Edit-hole dialog Adj chip (BOTH ScorecardScreen and RoundDetailScreen)

**Files:**
- Modify: `app/src/main/java/com/example/shottracker/feature/scorecard/ScorecardScreen.kt`
- Modify: `app/src/main/java/com/example/shottracker/feature/history/RoundDetailScreen.kt`

The two screens duplicate `EditHoleDialog` with near-identical bodies. This task makes the same change to both files. Apply each step to BOTH files.

- [ ] **Step 1: Add imports to both files**

Add these imports near the existing `androidx.compose.foundation.*` imports (top of each file):

```kotlin
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.size
```

(`shape.RoundedCornerShape` and `layout.size` may already be present in one or both files — only add if missing.)

- [ ] **Step 2: Update the `EditHoleDialog` signature**

In `ScorecardScreen.kt`, locate `@Composable private fun EditHoleDialog(...)`. Replace its parameter list and body with the version below. The dialog now takes `editAdjustment` plus `onAdjustmentIncrement` / `onAdjustmentDecrement` callbacks, renders the Adj chip at the bottom-left, and shows the "Adj Score" line in the header when adjustment > 0.

```kotlin
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EditHoleDialog(
    holeScore: HoleScore,
    editShots: Int?,
    editPutts: Int?,
    editPenalties: Int?,
    editAdjustment: Int?,
    onShotsChanged: (Int?) -> Unit,
    onPuttsChanged: (Int?) -> Unit,
    onPenaltiesChanged: (Int?) -> Unit,
    onAdjustmentIncrement: () -> Unit,
    onAdjustmentDecrement: () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val computedScore = (editShots ?: 0) + (editPutts ?: 0) + (editPenalties ?: 0)
    val adjustment = editAdjustment ?: 0
    val adjustedScore = computedScore - adjustment

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Edit Hole ${holeScore.holeNumber}") },
        text = {
            Column {
                val scoreToPar = computedScore - holeScore.par
                val scoreToParText = when {
                    computedScore == 0 -> "-"
                    scoreToPar >= 0 -> "+$scoreToPar"
                    else -> "$scoreToPar"
                }
                Row {
                    Text("Par: ${holeScore.par}")
                    Spacer(modifier = Modifier.width(24.dp))
                    Text("Score: $computedScore")
                    Spacer(modifier = Modifier.width(24.dp))
                    Text("+/-: $scoreToParText")
                    if (adjustment > 0) {
                        Spacer(modifier = Modifier.width(24.dp))
                        Text("Adj Score: $adjustedScore")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = editShots?.toString() ?: "",
                    onValueChange = { onShotsChanged(it.toIntOrNull()) },
                    label = { Text("Shots") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = editPenalties?.toString() ?: "",
                    onValueChange = { onPenaltiesChanged(it.toIntOrNull()) },
                    label = { Text("Penalties") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = editPutts?.toString() ?: "",
                    onValueChange = { onPuttsChanged(it.toIntOrNull()) },
                    label = { Text("Putts") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom-left: Adj chip. Tap = +1, long-press = -1.
                Row(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .combinedClickable(
                                onClick = onAdjustmentIncrement,
                                onLongClick = onAdjustmentDecrement
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Adj: $adjustment",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onSave) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        }
    )
}
```

Note: the file may already import `androidx.compose.ui.draw.clip` for use elsewhere; if not, add:

```kotlin
import androidx.compose.ui.draw.clip
```

- [ ] **Step 3: Apply the same dialog rewrite to `RoundDetailScreen.kt`**

In `RoundDetailScreen.kt`, locate the `@Composable private fun EditHoleDialog(...)`. Replace its parameter list and body with the EXACT same code as Step 2 (the only difference between the two files' versions is which file they live in — bodies must be byte-identical for parity).

- [ ] **Step 4: Update both screens' dialog invocations to pass the new callbacks**

In `ScorecardScreen.kt`, locate the dialog invocation in the `Scaffold` body (around line 118):

```kotlin
uiState.editingHole?.let { holeScore ->
    EditHoleDialog(
        holeScore = holeScore,
        editShots = uiState.editShots,
        editPutts = uiState.editPutts,
        editPenalties = uiState.editPenalties,
        onShotsChanged = viewModel::onEditShotsChanged,
        onPuttsChanged = viewModel::onEditPuttsChanged,
        onPenaltiesChanged = viewModel::onEditPenaltiesChanged,
        onSave = viewModel::saveHoleEdit,
        onCancel = viewModel::cancelEdit
    )
}
```

Replace with:

```kotlin
uiState.editingHole?.let { holeScore ->
    EditHoleDialog(
        holeScore = holeScore,
        editShots = uiState.editShots,
        editPutts = uiState.editPutts,
        editPenalties = uiState.editPenalties,
        editAdjustment = uiState.editAdjustment,
        onShotsChanged = viewModel::onEditShotsChanged,
        onPuttsChanged = viewModel::onEditPuttsChanged,
        onPenaltiesChanged = viewModel::onEditPenaltiesChanged,
        onAdjustmentIncrement = viewModel::incrementEditAdjustment,
        onAdjustmentDecrement = viewModel::decrementEditAdjustment,
        onSave = viewModel::saveHoleEdit,
        onCancel = viewModel::cancelEdit
    )
}
```

In `RoundDetailScreen.kt`, locate its dialog invocation (around line 277). Replace with the same shape (callbacks point at the RoundDetailViewModel methods — same names exist on both VMs):

```kotlin
uiState.editingHole?.let { holeScore ->
    EditHoleDialog(
        holeScore = holeScore,
        editShots = uiState.editShots,
        editPutts = uiState.editPutts,
        editPenalties = uiState.editPenalties,
        editAdjustment = uiState.editAdjustment,
        onShotsChanged = viewModel::onEditShotsChanged,
        onPuttsChanged = viewModel::onEditPuttsChanged,
        onPenaltiesChanged = viewModel::onEditPenaltiesChanged,
        onAdjustmentIncrement = viewModel::incrementEditAdjustment,
        onAdjustmentDecrement = viewModel::decrementEditAdjustment,
        onSave = viewModel::saveHoleEdit,
        onCancel = viewModel::cancelHoleEdit
    )
}
```

(Note `cancelEdit` vs `cancelHoleEdit` — the two VMs use different names for the same thing.)

- [ ] **Step 5: Verify compile**

Run: `.\gradlew.bat :app:compileDebugKotlin`

Expected: `BUILD SUCCESSFUL`.

---

### Task 10: Conditional Adj column + Adj Score summary item (BOTH ScorecardScreen and RoundDetailScreen)

**Files:**
- Modify: `app/src/main/java/com/example/shottracker/feature/scorecard/ScorecardScreen.kt`
- Modify: `app/src/main/java/com/example/shottracker/feature/history/RoundDetailScreen.kt`

Both screens have identical `ScorecardHeader`, `HoleScoreRow`, `SummaryRow`, and `SummaryCard` composables. Apply each step to BOTH files.

- [ ] **Step 1: Compute `showAdj` at the top of each screen's main `Composable` body**

In `ScorecardScreen.kt`, inside the `ScorecardScreen` composable after `val uiState by viewModel.uiState.collectAsStateWithLifecycle()`, add:

```kotlin
val showAdj = uiState.holeScores.any { (it.adjustment ?: 0) > 0 }
```

In `RoundDetailScreen.kt`, do the same — add the same line at the top of the `RoundDetailScreen` composable function, right after the existing `val uiState by ...` line.

- [ ] **Step 2: Update `ScorecardHeader` in both files to take `showAdj` and render conditionally**

In `ScorecardScreen.kt`, replace `ScorecardHeader`:

```kotlin
@Composable
private fun ScorecardHeader(showAdj: Boolean) {
    val labels = buildList {
        add("Hole"); add("Par"); add("Shots"); add("Pen"); add("Putts"); add("Score")
        if (showAdj) add("Adj")
        add("GIR"); add("+/-")
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        labels.forEach { label ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }
}
```

In `RoundDetailScreen.kt`, replace its `ScorecardHeader` with the identical body.

- [ ] **Step 3: Update `HoleScoreRow` in both files to take `showAdj` and render the Adj cell**

In `ScorecardScreen.kt`, replace `HoleScoreRow`:

```kotlin
@Composable
private fun HoleScoreRow(
    holeScore: HoleScore,
    showAdj: Boolean,
    onClick: () -> Unit
) {
    val scoreToPar = holeScore.scoreToPar
    val scoreColor = when {
        scoreToPar == null -> MaterialTheme.colorScheme.onSurface
        scoreToPar < 0 -> Color(0xFF4CAF50)
        scoreToPar == 0 -> MaterialTheme.colorScheme.onSurface
        else -> Color(0xFFF44336)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val shots = holeScore.score?.let { it - (holeScore.putts ?: 0) - (holeScore.penalties ?: 0) }
        Text(
            text = "${holeScore.holeNumber}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Text(
            text = "${holeScore.par}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Text(
            text = shots?.toString() ?: "-",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        val penaltyColor = if ((holeScore.penalties ?: 0) >= 1) Color(0xFFF44336) else MaterialTheme.colorScheme.onSurface
        Text(
            text = holeScore.penaltiesDisplay,
            style = MaterialTheme.typography.bodyLarge,
            color = penaltyColor,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        val puttsValue = if (holeScore.score != null) (holeScore.putts ?: 0) else holeScore.putts
        val puttsColor = when (puttsValue) {
            0, 1 -> Color(0xFF4CAF50)
            in 3..Int.MAX_VALUE -> Color(0xFFF44336)
            else -> MaterialTheme.colorScheme.onSurface
        }
        Text(
            text = puttsValue?.toString() ?: "-",
            style = MaterialTheme.typography.bodyLarge,
            color = puttsColor,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Text(
            text = holeScore.scoreDisplay,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = scoreColor,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        if (showAdj) {
            Text(
                text = holeScore.adjustedScore?.toString() ?: "-",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = scoreColor,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
        Text(
            text = if (holeScore.isGir == true) "✓" else "-",
            style = MaterialTheme.typography.bodyLarge,
            color = if (holeScore.isGir == true) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Text(
            text = scoreToPar?.let { if (it >= 0) "+$it" else "$it" } ?: "-",
            style = MaterialTheme.typography.bodyMedium,
            color = scoreColor,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
    }
}
```

In `RoundDetailScreen.kt`, `HoleScoreRow` takes an optional `onClick` (nullable). Replace it with the structurally identical version below — the difference from the ScorecardScreen version is just the nullable click handler:

```kotlin
@Composable
private fun HoleScoreRow(
    holeScore: HoleScore,
    showAdj: Boolean,
    onClick: (() -> Unit)? = null
) {
    val scoreToPar = holeScore.scoreToPar
    val scoreColor = when {
        scoreToPar == null -> MaterialTheme.colorScheme.onSurface
        scoreToPar < 0 -> Color(0xFF4CAF50)
        scoreToPar == 0 -> MaterialTheme.colorScheme.onSurface
        else -> Color(0xFFF44336)
    }

    val rowModifier = Modifier
        .fillMaxWidth()
        .let { if (onClick != null) it.clickable(onClick = onClick) else it }
        .padding(horizontal = 16.dp, vertical = 12.dp)

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val shots = holeScore.score?.let { it - (holeScore.putts ?: 0) - (holeScore.penalties ?: 0) }
        Text(
            text = "${holeScore.holeNumber}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Text(
            text = "${holeScore.par}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Text(
            text = shots?.toString() ?: "-",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        val penaltyColor = if ((holeScore.penalties ?: 0) >= 1) Color(0xFFF44336) else MaterialTheme.colorScheme.onSurface
        Text(
            text = holeScore.penaltiesDisplay,
            style = MaterialTheme.typography.bodyLarge,
            color = penaltyColor,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        val puttsValue = if (holeScore.score != null) (holeScore.putts ?: 0) else holeScore.putts
        val puttsColor = when (puttsValue) {
            0, 1 -> Color(0xFF4CAF50)
            in 3..Int.MAX_VALUE -> Color(0xFFF44336)
            else -> MaterialTheme.colorScheme.onSurface
        }
        Text(
            text = puttsValue?.toString() ?: "-",
            style = MaterialTheme.typography.bodyLarge,
            color = puttsColor,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Text(
            text = holeScore.scoreDisplay,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = scoreColor,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        if (showAdj) {
            Text(
                text = holeScore.adjustedScore?.toString() ?: "-",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = scoreColor,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
        Text(
            text = if (holeScore.isGir == true) "✓" else "-",
            style = MaterialTheme.typography.bodyLarge,
            color = if (holeScore.isGir == true) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Text(
            text = scoreToPar?.let { if (it >= 0) "+$it" else "$it" } ?: "-",
            style = MaterialTheme.typography.bodyMedium,
            color = scoreColor,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
    }
}
```

- [ ] **Step 4: Update `SummaryRow` in both files to take `showAdj` and render the Adj cell**

In `ScorecardScreen.kt`, replace `SummaryRow`:

```kotlin
@Composable
private fun SummaryRow(
    label: String,
    holes: List<HoleScore>,
    showAdj: Boolean
) {
    val totalPar = holes.sumOf { it.par }
    val totalShots = holes.sumOf { (it.score ?: 0) - (it.putts ?: 0) - (it.penalties ?: 0) }
    val totalPenalties = holes.sumOf { it.penalties ?: 0 }
    val totalPutts = holes.sumOf { it.putts ?: 0 }
    val totalScore = holes.sumOf { it.score ?: 0 }
    val totalAdjustedScore = holes.sumOf { it.adjustedScore ?: 0 }
    val totalGir = holes.count { it.isGir == true }
    val scored = holes.filter { it.score != null }
    val scoreToPar = scored.sumOf { (it.score ?: 0) - it.par }
    val scoreToParText = if (scored.isEmpty()) "-" else if (scoreToPar >= 0) "+$scoreToPar" else "$scoreToPar"
    val scoreColor = when {
        scored.isEmpty() -> MaterialTheme.colorScheme.onSurface
        scoreToPar < 0 -> Color(0xFF4CAF50)
        scoreToPar == 0 -> MaterialTheme.colorScheme.onSurface
        else -> Color(0xFFF44336)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Text(
            text = "$totalPar",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Text(
            text = "$totalShots",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Text(
            text = "$totalPenalties",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Text(
            text = "$totalPutts",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Text(
            text = "$totalScore",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = scoreColor,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        if (showAdj) {
            Text(
                text = "$totalAdjustedScore",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = scoreColor,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
        Text(
            text = "$totalGir",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Text(
            text = scoreToParText,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = scoreColor,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
    }
}
```

In `RoundDetailScreen.kt`, replace `SummaryRow` with the identical body.

- [ ] **Step 5: Update `SummaryCard` in both files**

In `ScorecardScreen.kt`, replace `SummaryCard`:

```kotlin
@Composable
private fun SummaryCard(
    totalPar: Int,
    totalPutts: Int,
    totalPenalties: Int,
    totalShots: Int,
    totalScore: Int,
    totalAdjustment: Int,
    totalGir: Int,
    scoreToPar: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SummaryItem("Par", totalPar.toString())
            SummaryItem("Shots", totalShots.toString())
            SummaryItem("Pen", totalPenalties.toString())
            SummaryItem("Putts", totalPutts.toString())
            SummaryItem("Score", totalScore.toString())
            if (totalAdjustment > 0) {
                SummaryItem("Adj Score", (totalScore - totalAdjustment).toString())
            }
            SummaryItem("GIR", totalGir.toString())
            SummaryItem(
                "+/-",
                if (scoreToPar >= 0) "+$scoreToPar" else "$scoreToPar"
            )
        }
    }
}
```

In `RoundDetailScreen.kt`, replace its `SummaryCard` with the identical body.

- [ ] **Step 6: Update the `SummaryCard` invocations in both screens**

In `ScorecardScreen.kt`, locate the `SummaryCard(...)` call (around line 74). Replace with:

```kotlin
SummaryCard(
    totalPar = viewModel.totalPar,
    totalPutts = viewModel.totalPutts,
    totalPenalties = viewModel.totalPenalties,
    totalShots = viewModel.totalScore - viewModel.totalPutts - viewModel.totalPenalties,
    totalScore = viewModel.totalScore,
    totalAdjustment = viewModel.totalAdjustment,
    totalGir = viewModel.totalGir,
    scoreToPar = viewModel.scoreToPar
)
```

In `RoundDetailScreen.kt`, locate the `SummaryCard(...)` call (around line 207). Replace with:

```kotlin
SummaryCard(
    totalPar = viewModel.totalPar,
    totalShots = viewModel.totalShots,
    totalPenalties = viewModel.totalPenalties,
    totalPutts = viewModel.totalPutts,
    totalScore = viewModel.totalScore,
    totalAdjustment = viewModel.totalAdjustment,
    totalGir = viewModel.totalGir,
    scoreToPar = viewModel.scoreToPar
)
```

- [ ] **Step 7: Update invocations of `ScorecardHeader`, `HoleScoreRow`, `SummaryRow` to pass `showAdj`**

In `ScorecardScreen.kt`:
- `ScorecardHeader()` (around line 85) → `ScorecardHeader(showAdj = showAdj)`
- `HoleScoreRow(holeScore = holeScore, onClick = { viewModel.startEditingHole(holeScore) })` (around line 94) → `HoleScoreRow(holeScore = holeScore, showAdj = showAdj, onClick = { viewModel.startEditingHole(holeScore) })`
- `SummaryRow(label = "Front 9", holes = ...)` (around line 102) → `SummaryRow(label = "Front 9", holes = ..., showAdj = showAdj)`
- `SummaryRow(label = "Back 9", holes = ...)` (around line 108) → `SummaryRow(label = "Back 9", holes = ..., showAdj = showAdj)`

In `RoundDetailScreen.kt`:
- `ScorecardHeader()` (around line 218) → `ScorecardHeader(showAdj = showAdj)`
- `HoleScoreRow(holeScore = holeScore, onClick = if (uiState.isEditing) { ... } else null)` (around line 224) → `HoleScoreRow(holeScore = holeScore, showAdj = showAdj, onClick = if (uiState.isEditing) { ... } else null)`
- `SummaryRow(label = "Front 9", holes = ...)` (around line 234) → `SummaryRow(label = "Front 9", holes = ..., showAdj = showAdj)`
- `SummaryRow(label = "Back 9", holes = ...)` (around line 240) → `SummaryRow(label = "Back 9", holes = ..., showAdj = showAdj)`

- [ ] **Step 8: Verify compile**

Run: `.\gradlew.bat :app:compileDebugKotlin`

Expected: `BUILD SUCCESSFUL`.

---

### Task 11: History card "Adjusted Score" line

**Files:**
- Modify: `app/src/main/java/com/example/shottracker/feature/history/HistoryScreen.kt`

- [ ] **Step 1: Insert the conditional Adjusted Score line in `RoundHistoryCard`**

Locate the date `Text(...)` (around line 223-227) — it formats `round.startDateTime`. Just AFTER the date Text (and after the optional "In Progress" block at lines 228-235) and BEFORE the `differential?.let { ... }` block (around lines 236-243), add a new conditional block.

The block goes between line 235 (the closing `}` of the In Progress block) and line 236 (the start of `differential?.let`). Here's the exact insertion:

```kotlin
                if ((round.totalAdjustment ?: 0) > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Adjusted Score: ${(round.totalScore ?: 0) - round.totalAdjustment!!}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
```

The result inside `RoundHistoryCard`'s left Column flows: courseName → date → optional "In Progress" → optional Adjusted Score → optional Differential → optional PCC.

- [ ] **Step 2: Verify assemble**

Run: `.\gradlew.bat :app:assembleDebug`

Expected: `BUILD SUCCESSFUL`.

---

### Task 12: Manual smoke test

**Files:** None (user runs on device or emulator).

This step cannot be dispatched to a subagent — it requires interaction with a real device.

- [ ] **Step 1: Install and launch the app**

Run: `.\gradlew.bat :app:installDebug`

Then launch ShotTracker on your device or emulator.

- [ ] **Step 2: Apply adjustment from live-round scorecard**

Start a round at a course with a tee that has rating + slope (so a differential can be computed). Play to hole 1, set score=8, pen=0, putts=2. Open the scorecard from the in-round screen. Tap hole 1's row to open the edit dialog.

Verify:
- The dialog now has an "Adj: 0" chip at the bottom-left.
- Tap it 3 times → label reads "Adj: 3". The header summary line also shows "Adj Score: 5".
- Long-press the chip → label reads "Adj: 2".
- Tap once → "Adj: 3". Tap Save.

Back on the scorecard:
- Top summary card now has an "Adj Score" item between Score and GIR, value = (totalScore − 3).
- The hole-list table has an "Adj" column (between Score and GIR). Hole 1's row shows the adjusted score (5).

- [ ] **Step 3: Hide on zero**

Tap hole 1 again, long-press Adj down to 0, tap Save.

Verify:
- Top summary card loses the "Adj Score" item.
- Table loses the "Adj" column. Layout is back to the default 8 columns.

- [ ] **Step 4: Round completion → History card**

Apply a non-zero adjustment on hole 1 (use the dialog), end the round.

Open History. Verify:
- The round's card now shows "Adjusted Score: X" between the date and the Differential.
- The Differential value reads LOWER than it would have without the adjustment. Spot-check by computing `(adjustedScore − rating − pcc) × 113 / slope` and confirming the value matches what's shown.
- The handicap index (top of History) reflects the new differential (will only change if you have ≥ 3 eligible rounds).

- [ ] **Step 5: History edit mode**

Tap the completed round to open `RoundDetailScreen`. Tap the pencil icon to enter edit mode. Tap any hole row.

Verify:
- The dialog has the same "Adj: N" chip with the saved value pre-populated.
- Tap to increment, long-press to decrement.
- Save. Pop back to History.
- The "Adjusted Score:" line on the round card and the Differential value both update without restarting the app.

- [ ] **Step 6: Migration safety on existing data**

If the device already has v4 ShotTracker data installed (from a previous install): the `installDebug` command upgrades in place. Confirm:
- The app opens to the History screen without crashing.
- All pre-existing rounds open in RoundDetail without errors.
- Differentials for pre-existing rounds (which have `totalAdjustment = NULL`, treated as 0) match exactly what they did before the upgrade.

- [ ] **Step 7: Adjustment preserved across active-round saves**

Apply an adjustment to hole 1 in the scorecard view of an active round, save. Return to the in-round screen. Tap "Shots" once to record a new shot.

Verify (re-open the scorecard):
- Hole 1's adjustment is STILL the value you set. The active-round auto-save did not wipe it.

If any step above fails, stop and investigate before considering the feature complete.

---

## Self-Review Notes

This plan was self-reviewed against the spec at `docs/superpowers/specs/2026-05-21-hole-score-adjustment-design.md`.

1. **Spec coverage:** Every spec section maps to one or more tasks. Schema (Task 1), domain/mapper (Task 2), DAO (Task 3), repository (Task 4), active-round preservation (Task 5), ScorecardViewModel (Task 6), RoundDetailViewModel + differential (Task 7), HistoryViewModel (Task 8), edit dialog (Task 9), scorecard table + summary card (Task 10), History card (Task 11), manual test (Task 12).
2. **No automated tests:** Accepted plan deviation (no VM tests, mirrors the hole-notes feature). Documented in the plan header and the spec's testing section.
3. **Mirror parity:** Tasks 9 and 10 explicitly require identical edits in BOTH `ScorecardScreen.kt` and `RoundDetailScreen.kt`. The two files duplicate composables today; this plan does not refactor the duplication (out of scope), it just requires mirroring.
4. **Type consistency:** Method names match across VM tasks (`incrementEditAdjustment`, `decrementEditAdjustment`, `totalAdjustment`, `adjustedScore` are identical in both VMs). The DAO `updateHoleScoreStats` signature is updated everywhere it's referenced (Tasks 3 → 4 → 5/6/7).
5. **Adjustment-vs-null:** Task 6/7 store `adjustment = editAdjustment?.takeIf { it > 0 }` — so a saved adjustment of 0 becomes NULL in the database. This matches the spec's "null and 0 are equivalent" rule. The History card's `(round.totalAdjustment ?: 0) > 0` check handles both.
