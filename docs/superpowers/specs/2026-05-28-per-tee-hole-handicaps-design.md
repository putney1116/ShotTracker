# Per-Tee Hole Handicaps + NDB Auto-Cap

Allow each tee to define a per-hole stroke index (1–18 / 1–9), then use those handicaps together with the round-time player handicap index to auto-fill the per-hole adjustment up to the Net Double Bogey cap as the player records strokes.

## Motivation

The adjustment feature (see `2026-05-21-hole-score-adjustment-design.md`) currently relies on the player to manually compute and enter their Net Double Bogey deduction on each hole that exceeds the cap. NDB requires:

- The player's course handicap (a function of their handicap index and the tee's slope rating).
- The hole's stroke index (the "handicap" column on a scorecard, 1–18).

Both inputs are inferable: the player already has a handicap index computed in `HistoryViewModel` from prior rounds, and per-hole stroke indices are a standard course attribute. Capturing the stroke indices unlocks fully automatic NDB cap computation during play while preserving manual overrides for any hole where the user disagrees.

## User Flow

1. **Setting handicaps on a tee** (Edit Course screen): each tee row in the tee list gets a `Set Handicaps` text button. Tapping it opens a dialog with one editable cell per hole (1..holeCount). The user enters integers 1..holeCount; the dialog enforces uniqueness across the set and warns on missing values. Save persists the assignment to the tee.
2. **Round start**: at `NewRoundSetupViewModel.startRound`, the current player handicap index (from the existing 20-round computation) is snapshotted onto the new `Round` row. The selected tee, its slope/rating, and the per-hole handicaps are read live during the round (no snapshotting needed — they're stable course data).
3. **During play**: as the user taps Shots / Pen / Putts in the active-round bottom bar, `saveCurrentHole` recomputes the hole's NDB cap and writes the resulting adjustment. The player sees the chip on the scorecard already filled in — no manual action required for the common case.
4. **Manual override**: in the scorecard edit dialog (or RoundDetail edit), tapping the Adj chip changes the value as it does today. Saving the dialog persists the manually-set value without recomputation. The next stroke recorded via the active-round bottom bar will recompute and may override.
5. **Display**: scorecard table gains a conditional `Hcp` column showing each hole's stroke index for the selected tee, between `Hole` and `Par`. Shown only when the round's tee has handicaps configured.

## Architecture

### Data model

**Schema change** (DB version 6 → 7):

```sql
ALTER TABLE tee_hole_info RENAME TO tee_hole_info_old;
CREATE TABLE tee_hole_info (
  teeHoleInfoId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
  teeId INTEGER NOT NULL,
  holeInfoId INTEGER NOT NULL,
  yardage INTEGER,
  handicap INTEGER,
  FOREIGN KEY(teeId) REFERENCES tees(teeId) ON DELETE CASCADE,
  FOREIGN KEY(holeInfoId) REFERENCES hole_info(holeInfoId) ON DELETE CASCADE
);
CREATE INDEX index_tee_hole_info_teeId ON tee_hole_info(teeId);
CREATE INDEX index_tee_hole_info_holeInfoId ON tee_hole_info(holeInfoId);
INSERT INTO tee_hole_info (teeHoleInfoId, teeId, holeInfoId, yardage, handicap)
  SELECT teeHoleInfoId, teeId, holeInfoId, yardage, NULL FROM tee_hole_info_old;
DROP TABLE tee_hole_info_old;

ALTER TABLE rounds ADD COLUMN handicapIndex REAL;
```

The `yardage` column becomes nullable in the new schema. The existing `tee_hole_info` table is currently created by Room but never populated by any code path; converting yardage from NOT NULL to nullable lets us add rows that carry only a handicap (or only a yardage) without lying about the other field.

**Entities**:
- `TeeHoleInfoEntity.yardage: Int?` (was `Int`).
- `TeeHoleInfoEntity.handicap: Int?` — nullable; `null` means "no handicap assigned for this hole on this tee".
- `RoundEntity.handicapIndex: Double?` — snapshotted at round-start; null means "no index was available when this round began".

**Domain models**:
- New `HoleHandicap` value carrier or, simpler, expose `Map<Int, Int>` (holeNumber → handicap) on the loaded course state where each tee provides its own map.
- `Round.handicapIndex: Double? = null`.

**Mappers**: `RoundMapper` round-trips the new field. A new mapper layer for `TeeHoleInfoEntity` (does not yet exist as a domain type — see DAO/Repository below).

**Migration `MIGRATION_6_7`**:

```kotlin
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

Registered in `DatabaseModule.kt`'s `addMigrations(...)` chain.

### DAO

**New `TeeHoleInfoDao`**:

```kotlin
@Dao
interface TeeHoleInfoDao {
    @Query("SELECT * FROM tee_hole_info WHERE teeId = :teeId")
    suspend fun getForTee(teeId: Long): List<TeeHoleInfoEntity>

    @Query("""
        SELECT thi.* FROM tee_hole_info thi
        INNER JOIN hole_info hi ON thi.holeInfoId = hi.holeInfoId
        WHERE thi.teeId = :teeId
        ORDER BY hi.holeNumber ASC
    """)
    suspend fun getForTeeOrderedByHole(teeId: Long): List<TeeHoleInfoEntity>

    @Insert
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
```

The dao is added to `ShotTrackerDatabase` and to `DatabaseModule`'s provider list.

**`RoundDao`**: extend the round insert/update flow to round-trip `handicapIndex`. With Room's `@Insert(entity = RoundEntity::class)` this is automatic; no DAO signature change needed if `RoundEntity` is updated.

### Repository

**`CourseRepository`** gets two new methods:

```kotlin
/** Returns map of holeNumber → stroke index for the given tee. Empty if none set. */
suspend fun getHoleHandicaps(teeId: Long): Map<Int, Int>

/** Replaces all handicap rows for the tee with the supplied map. */
suspend fun setHoleHandicaps(teeId: Long, holeHandicaps: Map<Int, Int>)
```

`getHoleHandicaps` joins `tee_hole_info` against `hole_info` to map handicap rows to hole numbers.

`setHoleHandicaps` uses `replaceForTee`. For each (holeNumber, handicap) in the supplied map, it looks up the matching `holeInfoId` from the course, then inserts the row. Holes not present in the map are dropped (no row).

**`RoundRepository`**: no interface change. `RoundEntity.handicapIndex` rides through existing CRUD.

### Domain extraction: HandicapCalculator

Move handicap-index math out of `HistoryViewModel` into a new file:

`domain/handicap/HandicapCalculator.kt`:

```kotlin
data class HandicapIndexResult(
    val index: Double,
    val countedRoundIds: Set<Long>,
    val formulaDescription: String, // e.g. "Lowest 8 of last 20 differentials"
)

object HandicapCalculator {
    /** Reads completed rounds, returns the WHS handicap index plus metadata.
     *  Null if fewer than 3 rounds are available (the existing rule). */
    fun computeIndex(rounds: List<Round>): HandicapIndexResult?

    /** WHS Course Handicap formula. */
    fun courseHandicap(handicapIndex: Double, slope: Int): Int =
        kotlin.math.round(handicapIndex * slope / 113.0).toInt()

    /** Per-hole strokes received using stroke-index allocation. */
    fun strokesReceived(holeHandicap: Int, courseHandicap: Int): Int {
        val base = Math.floorDiv(courseHandicap, 18)
        val remainder = Math.floorMod(courseHandicap, 18)
        val extra = if (holeHandicap <= remainder) 1 else 0
        return base + extra
    }

    /** Net Double Bogey for a hole given par + strokes received. */
    fun netDoubleBogey(par: Int, strokesReceived: Int): Int = par + 2 + strokesReceived
}
```

`HistoryViewModel` is refactored to call `HandicapCalculator.computeIndex` and then map the result into its existing `HandicapResult` UI state object.

`NewRoundSetupViewModel.startRound` calls `HandicapCalculator.computeIndex` on the user's existing completed rounds and stores the resulting `index` on the new `Round` (or null if no result).

## UI Components

### Course management — Set Handicaps dialog

In `CourseManagementScreen.kt`, each tee row in the tee list adds a `Set Handicaps` `TextButton` next to the existing Edit / Delete actions:

```
[Black Tees · 73.2 / 142 · 7102 yds]   [Edit] [Set Handicaps] [Delete]
```

Tapping it opens `EditHoleHandicapsDialog`. The dialog body is a `LazyColumn` of `holeCount` rows. Each row has:

```
Hole 1 (Par 4)    [_____]    ← OutlinedTextField, numeric, width ~64dp
```

A header row above the list says: `Stroke Index 1..N — each hole must have a unique value.`

State: `Map<Int, Int>` (holeNumber → handicap). The Save button is disabled until:
- Every hole has a non-blank value, OR
- Every hole is blank (which clears all handicaps).
- Values are in `1..holeCount`.
- All values are unique.

The dialog shows a single error string when validation fails (red text below the list). On Save, the VM calls `setHoleHandicaps(teeId, map)` and dismisses.

A `Clear All` `TextButton` at the bottom-left of the dialog clears the map (each row becomes blank), which on Save deletes all handicap rows for the tee.

### Scorecard table — conditional `Hcp` column

The table currently has columns: `Hole | Par | Shots | Pen | Putts | Score | [Adj] | GIR | +/-` (Adj is itself conditional). When the round's selected tee has any handicap rows configured, insert `Hcp` between `Hole` and `Par`:

```
Default:     Hole | Par | ... | GIR | +/-
With hcp:    Hole | Hcp | Par | ... | GIR | +/-
With both:   Hole | Hcp | Par | ... | Adj | GIR | +/-
```

Mirror in both `ScorecardScreen` and `RoundDetailScreen`. Header label `Hcp`. Cell content `holeHandicap?.toString() ?: "-"`.

Same `Modifier.weight(1f)` pattern as `Adj`.

### Active-round screen — Adj chip auto-fill

No new UI on the in-round screen itself. The chip on the scorecard / edit dialog already exists. The change is internal: `ActiveRoundViewModel.saveCurrentHole` now writes the auto-capped `adjustment` value through the existing save path.

## ViewModel state and methods

### CourseManagementViewModel

Add to `CourseManagementUiState`:

```kotlin
val editingHandicapsTeeId: Long? = null
val handicapEdits: Map<Int, String> = emptyMap()   // holeNumber → user input string
val handicapError: String? = null
```

New methods:

```kotlin
fun openHandicapEditor(teeId: Long)        // loads current handicaps via getHoleHandicaps
fun cancelHandicapEditor()
fun updateHandicapEntry(holeNumber: Int, value: String)
fun saveHandicapEdits()                    // validates, calls setHoleHandicaps, dismisses
fun clearAllHandicapEdits()
```

### NewRoundSetupViewModel

`startRound` change (pseudo-flow):

```kotlin
val completedRounds = roundRepository.getCompletedRounds().first()
val indexResult = HandicapCalculator.computeIndex(completedRounds)
val roundId = roundRepository.startRound(
    courseId = course.id,
    teeId = tee.id,
    handicapIndex = indexResult?.index,  // ← new
)
```

The repository's `startRound` (or whatever method creates the row today) takes a new optional `handicapIndex: Double?` parameter.

### ActiveRoundViewModel

`saveCurrentHole` is extended with an NDB auto-cap step that runs before the existing save:

```kotlin
suspend fun saveCurrentHole(...) {
    val round = currentRound ?: return
    val hole = currentHoleInfo ?: return
    val tee = selectedTee ?: return
    val score = (holeScore.score ?: 0)

    val autoCappedAdjustment = computeAutoCap(round, hole, tee, score)
    // computeAutoCap returns null if any prerequisite is missing,
    // OR if score <= NDB cap (no adjustment needed).
    // Otherwise returns score - ndbCap.

    roundRepository.updateHoleScoreStats(
        ...,
        adjustment = autoCappedAdjustment,
        ...
    )
}
```

`computeAutoCap` reads `round.handicapIndex`, `tee.slope`, the hole's per-tee handicap (loaded once into a map on `ActiveRoundUiState` from `CourseRepository.getHoleHandicaps(teeId)`), and the hole's par. If any are missing or if the round is 9 holes, returns the existing `holeScore.adjustment` unchanged.

### ScorecardViewModel + RoundDetailViewModel

No behavior change for save — the existing `saveHoleEdit` continues to pass `editAdjustment` through verbatim. Both load `holeHandicaps: Map<Int, Int>` into their UI state from `CourseRepository.getHoleHandicaps(teeId)` on init / refresh and pass through to the table for rendering.

`RoundDetailViewModel.handicapDifferential` is unaffected (already uses `totalScore - totalAdjustment`).

## Edge cases

| Case | Behavior |
|------|----------|
| Tee has no handicap rows | `Hcp` column hidden in both scorecards. Auto-cap is skipped (treated as "no handicap data"). |
| Round started before user had 3 rounds | `handicapIndex` is null on the round. Auto-cap is skipped. Manual adjustment still works. |
| Tee's slope is null | Auto-cap is skipped. Hcp column still shows. |
| 9-hole round | Auto-cap is skipped (WHS 9-hole index allocation is out of scope for v1). Hcp column still shows for 9 holes. |
| User long-presses score down so it no longer exceeds NDB | Auto-cap writes `adjustment = null`. Chip displays `-`. |
| User manually overrides chip in dialog, then records another stroke in active round | Auto-cap recomputes and may overwrite. Documented trade-off; no schema flag in v1. |
| Existing rounds before migration | `handicapIndex = NULL`. Auto-cap doesn't fire retroactively. Manual adjustment editing in `RoundDetailScreen` still works exactly as today. |
| Existing `tee_hole_info` rows | Migration preserves `teeHoleInfoId`, `teeId`, `holeInfoId`, `yardage` (now nullable). `handicap` is `NULL`. Since no code currently populates this table, this is academic. |
| Course has 18 holes but tee handicap map covers only 9 | Validation in the dialog forces all holes to be set or all blank — partial saves not allowed. Holes with `null` handicap behave as "no data" (auto-cap skipped for those holes only). |
| Course handicap is negative (plus-handicap player) | `Math.floorMod` and `Math.floorDiv` give correct stroke allocation for negative course handicaps. |

## Testing

### Unit tests

- `HandicapCalculatorTest`:
  - `computeIndex` matches existing HistoryViewModel output on a fixture of N rounds (parity check against the current implementation before extraction).
  - `courseHandicap` rounding against three fixture inputs.
  - `strokesReceived` for: zero handicap (all holes 0), exactly 18 (all holes 1), 27 (some holes 2, rest 1), -2 (two specific holes get -1).
  - `netDoubleBogey` parity.
- Optional: a `ScorecardViewModelTest` confirming `saveHoleEdit` passes adjustment through unchanged (does not depend on Android).

### Manual test plan

1. **Set up handicaps on a tee.**
   - Open Edit Course, expand a course, tap `Set Handicaps` on a tee. Dialog opens with 18 blank rows.
   - Try to save with blanks → error.
   - Fill all 18 with 1..18, but duplicate two → error.
   - Fill with 1..18 unique → Save dismisses. Reopen the dialog → values persist.
2. **Hcp column visibility.**
   - Start a round on a course where the selected tee has handicaps. Scorecard shows `Hcp` column with each hole's value.
   - Switch to a tee with no handicaps (separate round) → `Hcp` column hidden.
3. **Auto-cap during play.**
   - Start a round. Confirm a non-zero handicap index in History. Pick a tee with handicaps and slope.
   - Hole 1 par 4, stroke index 1. Suppose course handicap = 20, so strokes received on hole 1 = 2, NDB = 8.
   - Tap Shots 10 times. Open scorecard → Adj chip on hole 1 reads `2` (10 − 8).
   - Long-press Shots to 7. Reopen scorecard → Adj chip reads `-` (7 ≤ 8).
4. **Manual override stickiness.**
   - On hole 2, set shots to a value over NDB so auto-cap fires. Open edit dialog. Long-press Adj chip down by 1. Save. Verify saved value persists across screen rotations.
   - Add one more shot via the active-round bar → auto-cap recomputes and overrides. Per the spec (per-save-only override) this is expected.
5. **No prerequisites → no auto-cap.**
   - Start a round where the user has <3 prior rounds (uninstall / fresh install). Score over NDB → Adj chip stays `-`. Manual chip taps still work.
6. **Migration safety.**
   - Install v6 build, create a course with tees, play a round, complete it. Upgrade to v7 build → courses, tees, rounds all open. No `Hcp` column appears (no handicaps set). Differentials unchanged.

## Out of Scope (v1)

- WHS 9-hole handicap index calculation (auto-cap simply skips 9-hole rounds).
- Per-round snapshot of slope/rating (live read from the tee is acceptable; editing a tee's slope after a round started would change auto-cap; rare enough to ignore).
- A sticky-manual-override flag (`adjustmentManuallyOverridden`) — explicitly rejected in favor of the per-save-only behavior.
- Reading handicap from OSM during course import.
- Auto-allocating handicaps based on yardage / par at course-create time.
- Course-handicap display on the round summary card. (Could be added later; not required for the NDB feature to work.)
- Updating the existing penalty / score `combinedClickable` UX. Unchanged.
