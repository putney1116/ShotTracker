# Per-Course Hole Notes Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a free-text note per hole at a course (persisted via `HoleInfoEntity`), with a compact icon on the in-round screen that opens either a read-only quick-view bubble (note exists) or a full-screen editor (note empty).

**Architecture:** A single nullable `notes: String?` column added to `hole_info` via Room migration 3→4. The existing `CourseRepository.updateHole` covers writes; the existing `getHoleByNumber` covers reads (already populates `currentHoleInfo` in `ActiveRoundViewModel`). UI additions all live in `ActiveRoundScreen.kt` and `ActiveRoundViewModel.kt` — no new files in production code.

**Tech Stack:** Kotlin 2.2.10, Jetpack Compose (Material 3), Room 2.8.4, Hilt DI.

**Spec:** `docs/superpowers/specs/2026-05-21-hole-notes-design.md`

**Repository note:** Project is NOT a git repository. No `git commit` steps. Each task ends with a verification step.

**Testing scope deviation:** The spec called for `ActiveRoundViewModelTest` unit tests, but `ActiveRoundViewModel` depends on a concrete `LocationService` (final class needing `Context`) that can't be cleanly faked without a refactor (extracting an interface). For v1, this plan **skips automated tests** and relies on the manual smoke test in Task 9. Note this trade-off if reviewing — the alternative would be a `LocationService` → `LocationProvider` interface extraction, which adds 4 files of unrelated refactoring.

---

## File Inventory

**Modified files:**

- `app/src/main/java/com/example/shottracker/data/local/entity/HoleInfoEntity.kt` — add `notes: String?` column
- `app/src/main/java/com/example/shottracker/data/local/Migrations.kt` — add `MIGRATION_3_4`
- `app/src/main/java/com/example/shottracker/data/local/ShotTrackerDatabase.kt` — bump `version` to 4
- `app/src/main/java/com/example/shottracker/core/di/DatabaseModule.kt` — register `MIGRATION_3_4`
- `app/src/main/java/com/example/shottracker/domain/model/Course.kt` — add `notes` to `HoleInfo`
- `app/src/main/java/com/example/shottracker/data/mapper/CourseMapper.kt` — round-trip `notes`
- `app/src/main/java/com/example/shottracker/feature/round/ActiveRoundViewModel.kt` — add UI state + methods
- `app/src/main/java/com/example/shottracker/feature/round/ActiveRoundScreen.kt` — icon row + quick view + editor + back handler

No new files. No test files.

**Verification commands (Windows PowerShell):**
- `.\gradlew.bat :app:compileDebugKotlin` — compile check after each backend task
- `.\gradlew.bat :app:assembleDebug` — final build verification

---

### Task 1: Add `notes` column to schema

**Files:**
- Modify: `app/src/main/java/com/example/shottracker/data/local/entity/HoleInfoEntity.kt`
- Modify: `app/src/main/java/com/example/shottracker/data/local/Migrations.kt`
- Modify: `app/src/main/java/com/example/shottracker/data/local/ShotTrackerDatabase.kt`
- Modify: `app/src/main/java/com/example/shottracker/core/di/DatabaseModule.kt`

- [ ] **Step 1: Add `notes` field to `HoleInfoEntity`**

In `HoleInfoEntity.kt`, append a new field at the end of the data class (preserving existing field order and trailing comma style of the file). The full data class becomes:

```kotlin
data class HoleInfoEntity(
    @PrimaryKey(autoGenerate = true)
    val holeInfoId: Long = 0,
    val courseId: Long,
    val holeNumber: Int,
    val par: Int,
    val greenFrontLat: Double?,
    val greenFrontLng: Double?,
    val greenCenterLat: Double?,
    val greenCenterLng: Double?,
    val greenBackLat: Double?,
    val greenBackLng: Double?,
    val notes: String? = null
)
```

- [ ] **Step 2: Add `MIGRATION_3_4` to `Migrations.kt`**

Append to `Migrations.kt` (after the existing `MIGRATION_2_3` block):

```kotlin
/**
 * v3 -> v4: added per-course hole notes
 *   hole_info.notes (nullable TEXT; null = no note)
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE hole_info ADD COLUMN notes TEXT")
    }
}
```

- [ ] **Step 3: Bump database version**

In `ShotTrackerDatabase.kt`, change `version = 3` to `version = 4`. The `@Database(...)` annotation becomes:

```kotlin
@Database(
    entities = [
        CourseEntity::class,
        HoleInfoEntity::class,
        TeeEntity::class,
        TeeHoleInfoEntity::class,
        ClubEntity::class,
        RoundEntity::class,
        HoleScoreEntity::class,
        ShotEntity::class
    ],
    version = 4,
    exportSchema = false
)
```

- [ ] **Step 4: Register the migration in `DatabaseModule.kt`**

Add the import:

```kotlin
import com.example.shottracker.data.local.MIGRATION_3_4
```

Change the existing `.addMigrations(MIGRATION_1_2, MIGRATION_2_3)` call to:

```kotlin
.addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
```

- [ ] **Step 5: Verify compile**

Run: `.\gradlew.bat :app:compileDebugKotlin`

Expected: `BUILD SUCCESSFUL`. KSP will regenerate Room code with the new column.

---

### Task 2: Plumb `notes` through domain model and mapper

**Files:**
- Modify: `app/src/main/java/com/example/shottracker/domain/model/Course.kt`
- Modify: `app/src/main/java/com/example/shottracker/data/mapper/CourseMapper.kt`

- [ ] **Step 1: Add `notes` to the `HoleInfo` domain model**

In `Course.kt`, modify the `HoleInfo` data class. The full class becomes:

```kotlin
data class HoleInfo(
    val id: Long = 0,
    val courseId: Long,
    val holeNumber: Int,
    val par: Int,
    val greenFrontLat: Double? = null,
    val greenFrontLng: Double? = null,
    val greenCenterLat: Double? = null,
    val greenCenterLng: Double? = null,
    val greenBackLat: Double? = null,
    val greenBackLng: Double? = null,
    val notes: String? = null
)
```

- [ ] **Step 2: Round-trip `notes` in `CourseMapper.kt`**

In `CourseMapper.kt`, modify the two `HoleInfo` mapping functions to include `notes`.

Replace `fun HoleInfoEntity.toDomain(): HoleInfo = ...` with:

```kotlin
fun HoleInfoEntity.toDomain(): HoleInfo = HoleInfo(
    id = holeInfoId,
    courseId = courseId,
    holeNumber = holeNumber,
    par = par,
    greenFrontLat = greenFrontLat,
    greenFrontLng = greenFrontLng,
    greenCenterLat = greenCenterLat,
    greenCenterLng = greenCenterLng,
    greenBackLat = greenBackLat,
    greenBackLng = greenBackLng,
    notes = notes
)
```

Replace `fun HoleInfo.toEntity(): HoleInfoEntity = ...` with:

```kotlin
fun HoleInfo.toEntity(): HoleInfoEntity = HoleInfoEntity(
    holeInfoId = id,
    courseId = courseId,
    holeNumber = holeNumber,
    par = par,
    greenFrontLat = greenFrontLat,
    greenFrontLng = greenFrontLng,
    greenCenterLat = greenCenterLat,
    greenCenterLng = greenCenterLng,
    greenBackLat = greenBackLat,
    greenBackLng = greenBackLng,
    notes = notes
)
```

- [ ] **Step 3: Verify compile**

Run: `.\gradlew.bat :app:compileDebugKotlin`

Expected: `BUILD SUCCESSFUL`.

---

### Task 3: Add note UI state to `ActiveRoundUiState`

**Files:**
- Modify: `app/src/main/java/com/example/shottracker/feature/round/ActiveRoundViewModel.kt`

- [ ] **Step 1: Add the four new state fields**

In `ActiveRoundViewModel.kt`, modify the `ActiveRoundUiState` data class. Add four fields at the end:

```kotlin
data class ActiveRoundUiState(
    val round: Round? = null,
    val currentHoleNumber: Int = 1,
    val currentHoleScore: HoleScore? = null,
    val currentHoleInfo: HoleInfo? = null,
    val shots: List<Shot> = emptyList(),
    val clubs: List<Club> = emptyList(),
    val selectedClub: Club? = null,
    val currentLocation: GpsLocation? = null,
    val distanceToGreen: DistanceToGreen = DistanceToGreen.EMPTY,
    val hasLocationPermission: Boolean = false,
    val isLoading: Boolean = true,
    val showClubSelector: Boolean = false,
    val showEndRoundDialog: Boolean = false,
    val putts: Int = 0,
    val penalties: Int = 0,
    val score: Int = 0,
    val tappedLocation: LatLng? = null,
    val distanceToTap: Int? = null,
    val distanceFromTapToGreen: Int? = null,
    val showNoteQuickView: Boolean = false,
    val showNoteEditor: Boolean = false,
    val noteEditorDraft: String = "",
    val showDiscardNoteDialog: Boolean = false
)
```

- [ ] **Step 2: Verify compile**

Run: `.\gradlew.bat :app:compileDebugKotlin`

Expected: `BUILD SUCCESSFUL`.

---

### Task 4: Add note-handling methods to `ActiveRoundViewModel`

**Files:**
- Modify: `app/src/main/java/com/example/shottracker/feature/round/ActiveRoundViewModel.kt`

- [ ] **Step 1: Add the 9 note-handling methods to the ViewModel class**

Add these methods inside `ActiveRoundViewModel`. Place them as a new section after the existing `endRound()` method (anywhere in the class is fine — locate them together for cohesion):

```kotlin
// --- Note handling ---

fun onNoteIconTapped() {
    val notes = _uiState.value.currentHoleInfo?.notes
    if (notes.isNullOrBlank()) {
        _uiState.value = _uiState.value.copy(
            showNoteEditor = true,
            noteEditorDraft = ""
        )
    } else {
        _uiState.value = _uiState.value.copy(showNoteQuickView = true)
    }
}

fun dismissNoteQuickView() {
    _uiState.value = _uiState.value.copy(showNoteQuickView = false)
}

fun openNoteEditorFromQuickView() {
    val existing = _uiState.value.currentHoleInfo?.notes.orEmpty()
    _uiState.value = _uiState.value.copy(
        showNoteQuickView = false,
        showNoteEditor = true,
        noteEditorDraft = existing
    )
}

fun updateNoteDraft(text: String) {
    _uiState.value = _uiState.value.copy(noteEditorDraft = text)
}

fun saveNote() {
    val holeInfo = _uiState.value.currentHoleInfo ?: return
    val trimmed = _uiState.value.noteEditorDraft.trim()
    val newNotes = trimmed.ifBlank { null }

    viewModelScope.launch {
        val updated = holeInfo.copy(notes = newNotes)
        courseRepository.updateHole(updated)
        _uiState.value = _uiState.value.copy(
            currentHoleInfo = updated,
            showNoteEditor = false,
            showDiscardNoteDialog = false,
            noteEditorDraft = ""
        )
    }
}

fun deleteNote() {
    _uiState.value = _uiState.value.copy(noteEditorDraft = "")
    saveNote()
}

fun requestCloseEditor() {
    val draft = _uiState.value.noteEditorDraft.trim()
    val saved = _uiState.value.currentHoleInfo?.notes.orEmpty()
    if (draft == saved) {
        _uiState.value = _uiState.value.copy(
            showNoteEditor = false,
            noteEditorDraft = ""
        )
    } else {
        _uiState.value = _uiState.value.copy(showDiscardNoteDialog = true)
    }
}

fun cancelNoteEdit() {
    _uiState.value = _uiState.value.copy(
        showNoteEditor = false,
        showDiscardNoteDialog = false,
        noteEditorDraft = ""
    )
}

fun dismissDiscardNoteDialog() {
    _uiState.value = _uiState.value.copy(showDiscardNoteDialog = false)
}
```

- [ ] **Step 2: Verify compile**

Run: `.\gradlew.bat :app:compileDebugKotlin`

Expected: `BUILD SUCCESSFUL`.

---

### Task 5: Dismiss note UI on hole navigation

**Files:**
- Modify: `app/src/main/java/com/example/shottracker/feature/round/ActiveRoundViewModel.kt`

The existing `nextHole()` and `previousHole()` methods reset some state but not the new note flags. Update both.

- [ ] **Step 1: Update `nextHole`**

Locate the existing `nextHole()` method (around line 324). Replace the `_uiState.value.copy(...)` call inside the launch block. The full method becomes:

```kotlin
fun nextHole() {
    viewModelScope.launch {
        saveCurrentHole()

        val maxHoles = 18 // Could be dynamic based on course
        val newHoleNumber = (_uiState.value.currentHoleNumber + 1).coerceAtMost(maxHoles)

        // Clear hole info so the camera waits for new green coords before re-centering
        _uiState.value = _uiState.value.copy(
            currentHoleNumber = newHoleNumber,
            currentHoleInfo = null,
            selectedClub = null,
            shots = emptyList(),
            tappedLocation = null,
            distanceToTap = null,
            distanceFromTapToGreen = null,
            showNoteQuickView = false,
            showNoteEditor = false,
            noteEditorDraft = "",
            showDiscardNoteDialog = false
        )

        loadOrCreateCurrentHole()
    }
}
```

- [ ] **Step 2: Update `previousHole`**

Locate `previousHole()` (around line 346). Replace its `copy(...)` call. The full method becomes:

```kotlin
fun previousHole() {
    viewModelScope.launch {
        saveCurrentHole()

        val newHoleNumber = (_uiState.value.currentHoleNumber - 1).coerceAtLeast(1)

        // Clear hole info so the camera waits for new green coords before re-centering
        _uiState.value = _uiState.value.copy(
            currentHoleNumber = newHoleNumber,
            currentHoleInfo = null,
            selectedClub = null,
            tappedLocation = null,
            distanceToTap = null,
            distanceFromTapToGreen = null,
            showNoteQuickView = false,
            showNoteEditor = false,
            noteEditorDraft = "",
            showDiscardNoteDialog = false
        )

        loadOrCreateCurrentHole()
    }
}
```

- [ ] **Step 3: Verify compile**

Run: `.\gradlew.bat :app:compileDebugKotlin`

Expected: `BUILD SUCCESSFUL`.

---

### Task 6: Add the note icon row to `ActiveRoundScreen`

**Files:**
- Modify: `app/src/main/java/com/example/shottracker/feature/round/ActiveRoundScreen.kt`

- [ ] **Step 1: Add imports**

Near the other `androidx.compose.material.icons.*` imports (around line 30), add:

```kotlin
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material.icons.outlined.StickyNote2 as OutlinedStickyNote2
```

(The `as OutlinedStickyNote2` alias avoids the name collision with the filled variant — both are named `StickyNote2` in their respective packages.)

- [ ] **Step 2: Add the icon row to the bottom overlay Column**

Locate the bottom overlay `Column` (around line 559). The existing structure is:

```kotlin
Column(
    modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .navigationBarsPadding()
        .padding(bottom = 8.dp, start = 8.dp, end = 8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
) {
    // Shots, Pen, Putts, Score row
    Row(
        // ... existing row contents ...
    )
}
```

Add a new icon row inside this Column AFTER the existing Shots/Pen/Putts/Score `Row` and BEFORE the closing `}` of the `Column`. The new row sits to the right because of `Modifier.align(Alignment.End)`:

```kotlin
            // Note icon (right-aligned, only shown when we have hole info)
            if (uiState.currentHoleInfo != null) {
                val notes = uiState.currentHoleInfo?.notes
                val hasNote = !notes.isNullOrBlank()
                Box(
                    modifier = Modifier
                        .align(Alignment.End)
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(overlayBackground)
                ) {
                    IconButton(
                        onClick = viewModel::onNoteIconTapped,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = if (hasNote) Icons.Default.StickyNote2
                                          else OutlinedStickyNote2,
                            contentDescription = if (hasNote) "View hole note"
                                                  else "Add hole note",
                            tint = overlayText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
```

The `Modifier.align(Alignment.End)` works inside the bottom `Column` because `Column` provides a `ColumnScope` that defines that modifier on direct children.

- [ ] **Step 3: Verify compile**

Run: `.\gradlew.bat :app:compileDebugKotlin`

Expected: `BUILD SUCCESSFUL`. (You may see lint warnings about unused params if any composables reference things not added yet — that's fine.)

---

### Task 7: Add the quick-view bubble

**Files:**
- Modify: `app/src/main/java/com/example/shottracker/feature/round/ActiveRoundScreen.kt`

- [ ] **Step 1: Add imports for the new icons + scroll modifiers**

Near the other Material 3 imports (around line 39-52), add:

```kotlin
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
```

- [ ] **Step 2: Add the bubble overlay**

The quick-view bubble is shown when `uiState.showNoteQuickView == true`. It overlays the screen and renders just above the bottom overlay area.

Add this block inside the top-level `Box` (the outer `Box(modifier = Modifier.fillMaxSize())` that starts around line 215) — place it at the END of that outer Box, AFTER the existing "Layer 3: Bottom overlay" Column but BEFORE the outer Box's closing brace. Look for the closing `}` of the outer `Box`, then insert this block just before it:

```kotlin
        // Note quick-view bubble (shown when showNoteQuickView == true)
        if (uiState.showNoteQuickView) {
            val notes = uiState.currentHoleInfo?.notes.orEmpty()
            // Full-screen transparent click-catcher behind the bubble
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = viewModel::dismissNoteQuickView
                    )
            )
            // Bubble itself, positioned above the bottom overlay row
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(start = 16.dp, end = 8.dp, bottom = 96.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(overlayBackground)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = viewModel::openNoteEditorFromQuickView,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit note",
                            tint = overlayText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = viewModel::dismissNoteQuickView,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close note",
                            tint = overlayText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Text(
                    text = notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = overlayText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                        .verticalScroll(rememberScrollState())
                )
            }
        }
```

Note: the `bottom = 96.dp` padding lifts the bubble above the bottom overlay row + icon row. Adjust if visual testing shows overlap.

- [ ] **Step 3: Add `heightIn` import**

If not already imported (search the file for `heightIn`), add to the imports near the other `androidx.compose.foundation.layout.*`:

```kotlin
import androidx.compose.foundation.layout.heightIn
```

- [ ] **Step 4: Verify compile**

Run: `.\gradlew.bat :app:compileDebugKotlin`

Expected: `BUILD SUCCESSFUL`.

---

### Task 8: Add the full-screen editor dialog + discard dialog + back handler

**Files:**
- Modify: `app/src/main/java/com/example/shottracker/feature/round/ActiveRoundScreen.kt`

- [ ] **Step 1: Add imports**

Add to the imports section:

```kotlin
import androidx.activity.compose.BackHandler
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
```

(Some of these may already be present; only add the missing ones.)

- [ ] **Step 2: Add a `BackHandler` for the note UI**

Inside the `ActiveRoundScreen` composable, near the top of the function (after the `val uiState by ...` line, around line 104), add:

```kotlin
    // Hardware back handling for note UI
    BackHandler(enabled = uiState.showNoteEditor || uiState.showNoteQuickView) {
        when {
            uiState.showDiscardNoteDialog -> viewModel.dismissDiscardNoteDialog()
            uiState.showNoteEditor -> viewModel.requestCloseEditor()
            uiState.showNoteQuickView -> viewModel.dismissNoteQuickView()
        }
    }
```

Note: When the editor is closed (no note UI showing), this BackHandler is disabled and Android's normal back behavior continues.

- [ ] **Step 3: Add the full-screen editor Dialog**

After the existing "End round confirmation dialog" `if (uiState.showEndRoundDialog) { AlertDialog(...) }` block (around line 637-653), and BEFORE the closing `}` of the `ActiveRoundScreen` composable, add the editor Dialog:

```kotlin
    // Full-screen note editor
    if (uiState.showNoteEditor) {
        val focusRequester = remember { FocusRequester() }
        val savedNotes = uiState.currentHoleInfo?.notes.orEmpty()
        val draft = uiState.noteEditorDraft
        val isDirty = draft.trim() != savedNotes
        val hasSavedNote = savedNotes.isNotEmpty()

        Dialog(
            onDismissRequest = viewModel::requestCloseEditor,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Top bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = viewModel::requestCloseEditor) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                        Text(
                            text = "Hole ${uiState.currentHoleNumber} notes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        TextButton(
                            onClick = viewModel::deleteNote,
                            enabled = hasSavedNote
                        ) {
                            Text(
                                "Delete",
                                color = if (hasSavedNote) MaterialTheme.colorScheme.error
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Button(
                            onClick = viewModel::saveNote,
                            enabled = isDirty
                        ) {
                            Text("Save")
                        }
                    }
                    // Body: multi-line text field
                    OutlinedTextField(
                        value = draft,
                        onValueChange = viewModel::updateNoteDraft,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .focusRequester(focusRequester),
                        placeholder = { Text("Notes for hole ${uiState.currentHoleNumber}…") },
                        singleLine = false
                    )
                }
            }
        }
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
    }
```

- [ ] **Step 4: Add the discard-changes confirmation dialog**

Immediately after the editor block (still inside `ActiveRoundScreen`, still before the closing `}`):

```kotlin
    // Discard-changes confirmation dialog
    if (uiState.showDiscardNoteDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDiscardNoteDialog,
            title = { Text("Discard changes?") },
            text = { Text("Your unsaved changes will be lost.") },
            confirmButton = {
                Button(onClick = viewModel::cancelNoteEdit) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDiscardNoteDialog) {
                    Text("Cancel")
                }
            }
        )
    }
```

- [ ] **Step 5: Run final assembleDebug**

Run: `.\gradlew.bat :app:assembleDebug`

Expected: `BUILD SUCCESSFUL`.

If there are any "Unresolved reference" errors at this point, scan the imports list at the top of `ActiveRoundScreen.kt` and ensure every import added by Tasks 6, 7, and 8 is present.

---

### Task 9: Manual smoke test

**Files:** None (user runs on device or emulator).

This step cannot be dispatched to a subagent — it requires interaction with a real device.

- [ ] **Step 1: Install and launch the app**

Run: `.\gradlew.bat :app:installDebug`

Then launch ShotTracker on your device or emulator.

- [ ] **Step 2: First-time outlined icon**

Open or start a round at a course where the holes have no notes yet. On Hole 1, verify the note icon in the bottom-right (below the Shots/Pen/Putts/Score row) is rendered with the **outline** sticky-note glyph.

- [ ] **Step 3: Add a note via editor**

Tap the outlined icon. Verify the full-screen editor opens with an empty `OutlinedTextField` and the keyboard pops up automatically. Type "Hidden bunker behind green" and tap **Save**. Verify the editor closes and the icon switches to the **filled** sticky-note glyph.

- [ ] **Step 4: View the note via quick-view**

Tap the filled icon. Verify the dark translucent quick-view bubble appears just above the bottom overlay row, showing your note text. Verify the bubble has an Edit (pencil) icon and a Close (X) icon at the top right.

- [ ] **Step 5: Tap-outside dismiss**

Tap anywhere on the map area (not on the bubble itself). Verify the bubble dismisses.

- [ ] **Step 6: Edit from quick-view, then test the discard flow**

Tap the icon → bubble appears → tap the pencil icon → editor reopens with your note text in the field. Modify the text (append " — aim left"). Tap the **✕** (close icon, top-left of editor). Verify a "Discard changes?" AlertDialog appears.

- [ ] **Step 7: Cancel the discard, then save**

In the discard dialog, tap **Cancel**. Verify you return to the editor with your edits intact. Tap **Save**. Verify the editor closes and the note now reflects the new text (tap the icon again to confirm).

- [ ] **Step 8: Delete the note**

Tap the filled icon → tap pencil → in the editor, tap **Delete**. Verify the editor closes, the icon reverts to the **outline** variant, and tapping it again opens an empty editor (not the quick-view).

- [ ] **Step 9: Hole navigation dismisses note UI**

Add a note to Hole 1 (use the editor, save). Open the quick-view bubble. While the bubble is showing, tap the right arrow in the top hole nav bar to go to Hole 2. Verify the bubble dismisses automatically and Hole 2's icon reflects Hole 2's note status (likely outlined).

Repeat with the editor open: open the editor on Hole 2, type some text, hit the right arrow to advance to Hole 3. Verify the editor dismisses without a discard prompt (consistent with "hole nav silently discards" per spec).

- [ ] **Step 10: Persistence across rounds**

End the current round. Start a NEW round on the same course. Navigate to Hole 1. Verify the note you wrote in Step 3 (or 7) is still there — icon is filled, quick view shows the note.

- [ ] **Step 11: Hardware back behavior**

Open the editor. Tap hardware back (or gesture). Verify:
- If the editor draft is unchanged from saved: editor closes silently.
- If the editor draft is dirty: discard dialog appears.

Open the quick view. Tap hardware back. Verify the bubble dismisses.

If any step above fails, stop and investigate before considering the feature complete.

---

## Self-Review Notes

This plan was self-reviewed against the spec at `docs/superpowers/specs/2026-05-21-hole-notes-design.md`:

1. **Spec coverage:** All four major spec sections (Data model, ViewModel state/methods, UI components, Edge cases) are implemented by Tasks 1–8. Manual testing in Task 9 covers the spec's manual test plan.
2. **Testing deviation:** The spec called for `ActiveRoundViewModelTest` unit tests. This plan deviates and skips them due to `LocationService` being a final concrete class that can't be cleanly faked without a refactor. The deviation is called out in the plan header. Risk: VM regressions go uncaught by CI. Mitigation: Task 9 manual test plan exercises all key paths.
3. **Type consistency:** The methods exposed by the VM in Task 4 (`onNoteIconTapped`, `dismissNoteQuickView`, `openNoteEditorFromQuickView`, `updateNoteDraft`, `saveNote`, `deleteNote`, `requestCloseEditor`, `cancelNoteEdit`, `dismissDiscardNoteDialog`) are all referenced consistently by the screen code in Tasks 6–8.
4. **Migration safety:** Existing rows get `notes = NULL` per SQLite default for nullable TEXT columns. No data migration needed.
5. **Icon name collision:** Both `Icons.Filled.StickyNote2` and `Icons.Outlined.StickyNote2` exist with the same simple name. Task 6's import uses `as OutlinedStickyNote2` to disambiguate, then references both unambiguously.
