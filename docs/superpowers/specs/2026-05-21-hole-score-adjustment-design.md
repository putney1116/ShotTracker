# Hole Score Adjustment (ESC / Net Double Bogey)

Add a per-hole adjustment value so the player can record a stroke deduction when their actual score exceeds the handicap-rules maximum for that hole. Adjustments lower the round's effective score for handicap purposes — the differential and handicap index both shift accordingly.

## Motivation

USGA / WHS handicap rules cap the maximum score per hole at *Net Double Bogey* (par + 2 + handicap strokes received) for handicap calculation purposes. When a player records a true hole score of, say, 8 on a par 4 where their net-double-bogey cap is 6, the handicap differential should be computed using 6, not 8. Today the app records the raw score only and uses it for differential math, slightly inflating the handicap index.

This feature adds a manual adjustment input per hole. The user enters the difference between their actual score and the handicap-rules maximum (or any other adjustment they choose). The adjustment flows into the round-level total, the on-screen "Adj Score" displays, and the handicap differential formula.

## User Flow

1. While playing, open the scorecard from the in-round screen. Tap any hole row to open the existing edit dialog.
2. The dialog now has an extra control at the bottom-left labeled `Adj: 0`. Tap it to bump the adjustment up one. Long-press to bump it down. Save the dialog as usual.
3. If the adjustment is greater than 0, the dialog's header summary line now also shows `Adj Score: K` where K = `(shots+pen+putts) − adjustment`. The top scorecard summary card adds an `Adj Score` item showing the total minus total adjustment. The hole list table inserts an `Adj` column between `Score` and `GIR`; each hole's Adj cell shows `score − adjustment` (the adjusted score for that hole).
4. After the round is completed, the History page's round card shows an extra line `Adjusted Score: X` between the date and the differential. The differential value itself reflects the adjusted score.
5. The same edit-hole dialog and table behavior are available in `RoundDetailScreen` (the history edit-mode scorecard), so adjustments can be added or modified after the round has been played.

## Architecture

Two new nullable INTEGER columns (one on `hole_scores`, one on `rounds`), plus mirrored updates across the ScorecardScreen / RoundDetailScreen pair which today already duplicate their layout code.

### Data model

**Schema change** (DB version 4 → 5):

```sql
ALTER TABLE hole_scores ADD COLUMN adjustment INTEGER
ALTER TABLE rounds ADD COLUMN totalAdjustment INTEGER
```

**Entities**:
- `HoleScoreEntity.adjustment: Int?` — nullable; `null` and `0` are equivalent (no adjustment).
- `RoundEntity.totalAdjustment: Int?` — nullable; aggregates `SUM(adjustment)` across the round's holes.

**Domain models**:
- `HoleScore.adjustment: Int? = null`
- `HoleScore.adjustedScore: Int?` — derived getter: `score?.let { it - (adjustment ?: 0) }`.
- `Round.totalAdjustment: Int? = null`

**Mappers**: `RoundMapper` and `HoleScoreMapper` round-trip the new fields.

**Migration `MIGRATION_4_5`**:

```kotlin
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE hole_scores ADD COLUMN adjustment INTEGER")
        db.execSQL("ALTER TABLE rounds ADD COLUMN totalAdjustment INTEGER")
    }
}
```

Registered in `DatabaseModule.kt`'s `addMigrations(...)` chain.

### DAOs

**`HoleScoreDao`**:
- Extend `updateHoleScoreStats` UPDATE query to also set `adjustment`. Signature becomes:
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
- New aggregate:
  ```kotlin
  @Query("SELECT SUM(adjustment) FROM hole_scores WHERE roundId = :roundId AND adjustment IS NOT NULL")
  suspend fun getTotalAdjustment(roundId: Long): Int?
  ```

**`RoundDao`**:
- Extend `updateRoundStats` UPDATE query to set `totalAdjustment`:
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

### Repository

**`RoundRepository`** interface change (one method gets one new parameter, one new method added):

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
 * totalPenalties, totalAdjustment, holesPlayed) from the current hole_scores rows.
 * Called after edits to a completed round so the History card reflects the new totals.
 */
suspend fun recalculateRoundTotals(roundId: Long)
```

**`RoundRepositoryImpl`**:
- `updateHoleScoreStats` passes the new `adjustment` parameter through to the DAO.
- `updateRoundStatus` reads `getTotalAdjustment` and includes it in the `updateRoundStats` call (mirrors the existing `totalPenalties` flow).
- `recalculateRoundTotals` runs the same aggregation as `updateRoundStatus` (totals + holesPlayed) without changing the round status. Used by `RoundDetailViewModel.saveHoleEdit` so editing an adjustment on a completed round updates the cached Round totals immediately.

## UI Components

### Edit-hole dialog (both `ScorecardScreen` and `RoundDetailScreen`)

Both files duplicate `EditHoleDialog`. The same change is mirrored in both. The dialog body's structure becomes:

```
┌──────────────────────────────────────────┐
│ Par: 4   Score: 6   +/-: +2  [Adj Score: 4]  │ ← Adj Score appears only when adj > 0
├──────────────────────────────────────────┤
│ [OutlinedTextField] Shots                │
│ [OutlinedTextField] Penalties            │
│ [OutlinedTextField] Putts                │
├──────────────────────────────────────────┤
│ ╭─────────╮                              │ ← bottom-left
│ │ Adj: 2  │                              │   tap → +1, long-press → −1
│ ╰─────────╯                              │
└──────────────────────────────────────────┘
   [Cancel]                       [Save]
```

The Adj chip is a `Row` styled like a Material TextButton using `Modifier.combinedClickable(onClick, onLongClick)` (matches the `StatCounter` composable added in the long-press-decrement feature). Tap increments; long-press decrements; clamped to `0..10`.

When `editAdjustment > 0`, the header summary row appends `Adj Score: K` after the existing `+/-: X` cell, where K = `(editShots ?: 0) + (editPutts ?: 0) + (editPenalties ?: 0) − editAdjustment`.

### Scorecard table — conditional `Adj` column

Currently the table has 8 columns: `Hole | Par | Shots | Pen | Putts | Score | GIR | +/-`. When `holeScores.any { (it.adjustment ?: 0) > 0 }`, the table reflows to 9 columns by inserting `Adj` between `Score` and `GIR`:

```
Default:  Hole | Par | Shots | Pen | Putts | Score | GIR | +/-
Adjusted: Hole | Par | Shots | Pen | Putts | Score | Adj | GIR | +/-
```

The screen computes a single `showAdj: Boolean` and passes it to `ScorecardHeader`, `HoleScoreRow`, and `SummaryRow`. Each composable conditionally renders the extra cell (still using `Modifier.weight(1f)` so the table stays evenly spaced regardless of column count).

**Adj cell content per row**: `holeScore.adjustedScore?.toString() ?: "-"` (the adjusted score for that hole, not the deduction value).

**Adj cell content in summary row** (Front 9 / Back 9): sum of `adjustedScore` over the included holes.

Header label: `"Adj"`.

### Summary card at top of scorecard

Currently shows 7 items: `Par | Shots | Pen | Putts | Score | GIR | +/-`. When `totalAdjustment > 0`, an `Adj Score` item is inserted between `Score` and `GIR`:

```
Default:  Par  Shots  Pen  Putts  Score  GIR  +/-
Adjusted: Par  Shots  Pen  Putts  Score  Adj Score  GIR  +/-
```

`Arrangement.SpaceEvenly` on the surrounding Row absorbs the new item without manual width work.

`Adj Score` value = `totalScore − totalAdjustment`.

### History card on `HistoryScreen`

`RoundHistoryCard` currently shows (top-down inside the left column): course name → date → optional "In Progress" → optional Differential → optional PCC.

Add a new line **between the date (and any "In Progress" indicator) and Differential**, conditional on `(round.totalAdjustment ?: 0) > 0`:

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

## ViewModel state and methods

Both `ScorecardUiState` and `RoundDetailUiState` get one new edit field:

```kotlin
val editAdjustment: Int? = null
```

Both `ScorecardViewModel` and `RoundDetailViewModel` get parallel additions:

```kotlin
// In startEditingHole:
editAdjustment = holeScore.adjustment

// New methods:
fun incrementEditAdjustment() {
    val current = _uiState.value.editAdjustment ?: 0
    _uiState.value = _uiState.value.copy(editAdjustment = (current + 1).coerceIn(0, 10))
}
fun decrementEditAdjustment() {
    val current = _uiState.value.editAdjustment ?: 0
    if (current <= 0) return
    _uiState.value = _uiState.value.copy(editAdjustment = current - 1)
}

// In saveHoleEdit:
roundRepository.updateHoleScoreStats(
    holeScoreId = editingHole.id,
    score = ...,
    putts = ...,
    penalties = ...,
    adjustment = _uiState.value.editAdjustment,  // ← new
    fairwayHit = editingHole.fairwayHit,
    gir = editingHole.greenInRegulation
)
// In RoundDetailViewModel only, follow with:
roundRepository.recalculateRoundTotals(roundId)
loadRound()

// In cancelEdit / cancelHoleEdit:
editAdjustment = null  // ← include in the reset
```

Both VMs also get derived getters:

```kotlin
val totalAdjustment: Int
    get() = _uiState.value.holeScores.sumOf { it.adjustment ?: 0 }

val adjustedScore: Int
    get() = totalScore - totalAdjustment
```

`RoundDetailViewModel.handicapDifferential` switches its score input:

```kotlin
val handicapDifferential: Double?
    get() {
        val tee = _uiState.value.tee ?: return null
        val rating = tee.rating ?: return null
        val slope = tee.slope ?: return null
        val score = totalScore
        if (score == 0) return null
        val pcc = _uiState.value.round?.pcc ?: 0
        val adjusted = score - totalAdjustment   // ← was just `score` before
        return (adjusted - rating - pcc) * 113.0 / slope
    }
```

`HistoryViewModel.computeHandicapIndex` (and its differential-per-round loop) similarly switches to:

```kotlin
val adjusted = (round.totalScore ?: 0) - (round.totalAdjustment ?: 0)
differentials[round.id] = (adjusted - rating - pcc) * 113.0 / slope
```

## Edge cases

| Case | Behavior |
|------|----------|
| `adjustment` is null | Treated as 0 throughout. No display anywhere. |
| `adjustment > score` (would yield negative adjusted score) | Allowed; the adjusted score can go negative. Realistically the UI clamps adjustment 0..10 and score 1..20, so reaching this requires user input. Display the value as-is; no clamping at render. |
| `score` is null but adjustment > 0 | Possible if user taps Adj before entering a score. `adjustedScore = null`. UI shows "-" in the Adj column. Summary panel uses totalScore=0 minus totalAdjustment = negative adj total — but `totalAdjustment > 0` will trigger "Adj Score: -2" etc. This is intentional; the user gets a visible reminder that the score is incomplete. |
| Editing adjustment on completed round | `RoundDetailViewModel.saveHoleEdit` calls `recalculateRoundTotals` so the cached `Round.totalAdjustment` and `totalScore` update, and the History card and handicap index reflect the new value next time they load. |
| Existing rows after migration | `adjustment` and `totalAdjustment` columns added as `NULL` to every existing row. No data loss; no differential changes. |
| Active round in-progress | The Round row's `totalAdjustment` stays stale (only updated on completion via `updateRoundStatus`), but the scorecard view computes `totalAdjustment` live from `holeScores`, so the in-round summary stays accurate. |

## Testing

### No automated tests

Per the accepted trade-off documented in the hole-notes spec and the existing project state: `ScorecardViewModel` and `RoundDetailViewModel` currently have no unit tests, and `ActiveRoundViewModel`'s coupling to `LocationService` makes faking that pipeline unattractive. This feature follows the same path — manual testing only.

### Manual test plan

Run on a device after `:app:installDebug`:

1. **Live round adjustment flow:**
   - Start a round, play to hole 1, set score=8 / pen=0 / putts=2.
   - Open scorecard from the in-round screen, tap hole 1 row.
   - Edit dialog shows Adj: 0 at bottom-left. Tap it 3 times → label reads `Adj: 3`. Header line now also shows `Adj Score: 5`.
   - Long-press → `Adj: 2`. Tap once → `Adj: 3`. Save.
   - Back on the scorecard: top summary now shows `Adj Score: (totalScore − 3)` between Score and GIR.
   - The hole-list table now has an `Adj` column (between Score and GIR). Hole 1's Adj cell reads `5` (score 8 − adj 3).
2. **Hide on zero:**
   - Tap hole 1 again, long-press Adj to 0. Save.
   - Top summary loses the Adj Score item, table loses the Adj column. All holes display fine in the default 8-column layout.
3. **Round completion + History card:**
   - Set a non-zero adjustment on hole 1, complete the round.
   - Open History → the round card shows `Adjusted Score: X` between the date and Differential. The Differential value is *lower* than it was before the adjustment was applied (verify against a quick hand calc: `(adjusted − rating − pcc) × 113 / slope`).
4. **History edit mode:**
   - Tap the round to enter `RoundDetailScreen`. Tap edit icon. Tap any hole. The same Adj button appears in the dialog with the saved value.
   - Increment Adj, save. Pop back to History → the `Adjusted Score:` line and the Differential value both update without an app restart.
5. **Handicap index:**
   - Verify the displayed handicap index on the History page changes when adjustments are added to rounds (because differentials shift).
6. **Migration safety:**
   - If running over an existing v4 install with real data: rounds open without error; rounds with no adjustments show no Adj column, no Adj Score line in History, and identical differentials to before the upgrade.

## Out of Scope (v1)

- Auto-computing the adjustment from the user's handicap and the hole's stroke index (would require player handicap input + course handicap allocation table).
- Per-tee or per-course default maximums.
- Showing the adjustment value (the deduction) anywhere directly — UI only shows the adjusted *score*. The deduction is implicit (raw score minus adjusted score, both visible).
- Modifying the +/- to-par calculation. The +/- column continues to use the raw score.
- A Front 9 / Back 9 "Adj Score" subtotal line — the existing summary rows sum the new Adj column the same way they sum others.
- Adjustment on the active-round bottom bar (Shots / Pen / Putts have long-press decrement; adjustment lives only in the scorecard edit dialog per the user's spec).
