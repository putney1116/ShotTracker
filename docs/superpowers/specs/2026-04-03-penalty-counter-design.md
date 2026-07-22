# Penalty Counter Feature

Add a penalty stroke counter to the active round bottom bar and scorecard, mirroring the existing putts pattern.

## Behavior

- Tapping the Penalty button increments both `penalties` and `score` by 1 (penalty strokes count toward total score)
- Penalties are persisted per hole and aggregated per round
- Scorecard displays penalties per hole and allows editing
- Destructive DB migration (no need to preserve existing data)

## Changes by Layer

### Database (Room)

- **HoleScoreEntity**: add `penalties: Int?` column
- **HoleScoreDao.updateHoleScoreStats()**: add `penalties` parameter to UPDATE query
- **HoleScoreDao**: add `getTotalPenalties(roundId)` — `SELECT SUM(penalties)`
- **RoundEntity**: add `totalPenalties: Int?` column
- **RoundDao.updateRoundStats()**: add `totalPenalties` parameter
- Bump DB version, destructive fallback migration

### Domain Models

- **HoleScore**: add `penalties: Int? = null`, `penaltiesDisplay: String` (same pattern as `puttsDisplay`)
- **Round**: add `totalPenalties: Int? = null`

### Mapper

- **RoundMapper**: map `penalties` field in `HoleScoreEntity.toDomain()` and `HoleScore.toEntity()`. Map `totalPenalties` in Round mappings.

### Repository

- **RoundRepository interface**: add `penalties` param to `updateHoleScoreStats()`
- **RoundRepositoryImpl**: pass `penalties` through to DAO. Include `getTotalPenalties()` in `updateRoundStatus()`.

### ActiveRound (ViewModel + Screen)

- **ActiveRoundUiState**: add `penalties: Int = 0`
- **ActiveRoundViewModel**:
  - `onPenaltiesChanged(penalties: Int)` — coerce 0..10, update score: `(currentScore - currentPenalties) + newPenalties`
  - `incrementPenalties()` — calls `onPenaltiesChanged(current + 1)`
  - `loadOrCreateCurrentHole()` — load `penalties` from HoleScore
  - `saveCurrentHole()` — persist `penalties` to DB
- **ActiveRoundScreen bottom bar**: add "Penalty" TextButton between Shots and Score, showing count

### Scorecard (ViewModel + Screen)

- **ScorecardScreen**: add "Pen" column header in table, display `penaltiesDisplay` per hole row
- **ScorecardUiState**: add `editPenalties: Int?`
- **ScorecardViewModel**:
  - `startEditingHole()` — load `penalties`
  - `onEditPenaltiesChanged(penalties: Int?)` — coerce 0..10
  - `saveHoleEdit()` — include `penalties` in `updateHoleScoreStats()`
- Edit dialog: add penalties number field
