# Per-Course Hole Notes

Add the ability to write a free-text note for each hole at a course. Notes are stored on the course (not the round), so they persist across every round played at that course. During a round, a compact icon on the in-round screen opens a read-only view of the note; a pencil opens a full-screen editor.

## Motivation

Golfers accumulate course knowledge over time ("hidden bunker behind the green on 7", "aim left of the flag on 12"). Today this lives in the player's head; the app should let them capture it once and surface it the next time they play the same course.

## User Flow

1. From the in-round screen, the user sees a small note icon in the bottom-right corner, below the existing Shots/Pen/Putts/Score row.
2. **If the current hole has no note**, the icon is outlined. Tapping it opens a full-screen editor with an empty text field; type and tap Save to commit.
3. **If the current hole has a note**, the icon is filled. Tapping it pops up a translucent read-only bubble showing the note text. The bubble has a pencil to open the editor, and an X (or tap-outside) to dismiss.
4. The full-screen editor supports Save, Delete, and Close (Close prompts to discard unsaved changes if any).
5. Navigating to a different hole (prev/next) auto-dismisses any open note UI.

## Architecture

Single nullable column on `HoleInfoEntity`, room-migrated, mapped through domain to ViewModel state. No new tables, no new repository methods.

### Data model

**Database schema change** (version 3 → 4):

```sql
ALTER TABLE hole_info ADD COLUMN notes TEXT
```

**Entity** (`HoleInfoEntity.kt`): add `val notes: String? = null` at the end of the data class.

**Domain model** (`HoleInfo.kt`): add `val notes: String? = null`.

**Mapper** (`CourseMapper.kt`): round-trip `notes` in both `toDomain()` and `toEntity()`.

**Migration** (`Migrations.kt`):

```kotlin
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE hole_info ADD COLUMN notes TEXT")
    }
}
```

Wire into `DatabaseModule.kt`'s `addMigrations(...)` call.

**Repository**: no new methods. The existing `CourseRepository.updateHole(hole: HoleInfo)` covers writes. `getHoleByNumber` (already used to populate `currentHoleInfo`) covers reads — the new `notes` field flows through for free.

### ViewModel state additions

On `ActiveRoundUiState`:

```kotlin
val showNoteQuickView: Boolean = false,
val showNoteEditor: Boolean = false,
val noteEditorDraft: String = "",
val showDiscardNoteDialog: Boolean = false,
```

### ViewModel methods

```kotlin
fun onNoteIconTapped()
fun dismissNoteQuickView()
fun openNoteEditorFromQuickView()
fun updateNoteDraft(text: String)
fun saveNote()
fun deleteNote()
fun requestCloseEditor()    // prompts discard dialog if draft != saved
fun cancelNoteEdit()        // confirms discard, closes editor + dialog
fun dismissDiscardNoteDialog()
```

**Branching logic**:

- `onNoteIconTapped`: if `currentHoleInfo?.notes.isNullOrBlank()`, open editor with empty draft (`showNoteEditor = true`, `noteEditorDraft = ""`). Else open quick view (`showNoteQuickView = true`).
- `openNoteEditorFromQuickView`: close quick view, open editor with `noteEditorDraft = currentHoleInfo!!.notes!!`.
- `saveNote`: calls `courseRepository.updateHole(currentHoleInfo!!.copy(notes = draft.trim().ifBlank { null }))`, refreshes `currentHoleInfo`, then closes editor and resets draft.
- `deleteNote`: equivalent to `saveNote()` with `noteEditorDraft = ""` — stores null.
- `requestCloseEditor`: if `draft.trim() != (currentHoleInfo?.notes ?: "")`, set `showDiscardNoteDialog = true`. Else close editor immediately.
- `previousHole` / `nextHole` (existing methods): after the hole transition, set `showNoteQuickView = false`, `showNoteEditor = false`, `noteEditorDraft = ""`, `showDiscardNoteDialog = false`.

## UI Components

### Icon row (always visible while in round, hidden if `currentHoleInfo == null`)

A new compact row inside the existing bottom-overlay `Column` in `ActiveRoundScreen.kt`, placed below the Shots/Pen/Putts/Score row. The row contains a single 40dp circular IconButton, right-aligned via `Modifier.align(Alignment.End)`. Background: `overlayBackground` (`Color.Black.copy(alpha = 0.65f)`), `RoundedCornerShape(12.dp)`.

Icon glyph:
- Empty note: `Icons.Outlined.StickyNote2`
- Non-empty note: `Icons.Filled.StickyNote2`

Both are available via `material-icons-extended` (already on the classpath).

### Quick-view bubble (shown when `showNoteQuickView == true`)

A translucent panel positioned above the bottom overlay, anchored to the right edge with horizontal padding to match the rest of the overlays. Background and shape match the existing overlays.

- Content: read-only `Text` rendering `currentHoleInfo!!.notes`, `bodyMedium`, `overlayText` color.
- Top-right corner: two small icon buttons:
  - `Icons.Default.Edit` (pencil) → calls `openNoteEditorFromQuickView`
  - `Icons.Default.Close` (X) → calls `dismissNoteQuickView`
- Max height: 40% of the screen; if content exceeds, wrap in `verticalScroll(rememberScrollState())`.
- Tap-outside dismiss: a full-screen transparent `Box` placed behind the bubble (in z-order), with `clickable(onClick = vm::dismissNoteQuickView)`, without ripple.

### Full-screen editor (shown when `showNoteEditor == true`)

A `Dialog` with `properties = DialogProperties(usePlatformDefaultWidth = false)` and content using `Modifier.fillMaxSize()`. The surface is a normal opaque `Surface(color = MaterialTheme.colorScheme.surface)` — NOT translucent. The map is fully covered for focused editing.

Layout:

```
┌─────────────────────────────────────────────────┐
│ [✕]   Hole N notes              [Delete] [Save] │   ← top app bar
├─────────────────────────────────────────────────┤
│                                                  │
│  [OutlinedTextField, multi-line, fillMaxSize]    │
│                                                  │
└─────────────────────────────────────────────────┘
```

- Top app bar: a `Row` (custom layout, not Material 3 `TopAppBar` — avoids the experimental opt-in and gives precise control over the Delete/Save layout) with:
  - Close (✕) IconButton → `viewModel::requestCloseEditor`
  - Title "Hole ${currentHoleNumber} notes" centered
  - Delete TextButton (red text via `MaterialTheme.colorScheme.error`) → `viewModel::deleteNote`. Enabled only if the saved note (`currentHoleInfo?.notes`) is non-empty.
  - Save Button → `viewModel::saveNote`. Enabled only if `draft.trim() != (currentHoleInfo?.notes ?: "")`.
- Body: a single `OutlinedTextField`:
  - `value = state.noteEditorDraft`
  - `onValueChange = viewModel::updateNoteDraft`
  - `singleLine = false`
  - `modifier = Modifier.fillMaxSize().padding(16.dp)`
  - `placeholder = { Text("Notes for hole ${currentHoleNumber}…") }`
  - Auto-focused via `FocusRequester.requestFocus()` in a `LaunchedEffect(Unit)`.

### Discard dialog (shown when `showDiscardNoteDialog == true`)

Standard `AlertDialog` overlaid on the editor:
- Title: "Discard changes?"
- Body: "Your unsaved changes will be lost."
- Confirm button: "Discard" → `viewModel::cancelNoteEdit`
- Dismiss button: "Cancel" → `viewModel::dismissDiscardNoteDialog`

### Hardware back behavior

A `BackHandler` is added to `ActiveRoundScreen` (or the editor Dialog) so that hardware back on:

- Discard dialog → dismisses dialog (no-op via Dialog default).
- Editor open → calls `requestCloseEditor` (so prompts discard if dirty).
- Quick view open → calls `dismissNoteQuickView`.
- None of the above open → falls through to existing behavior (no `BackHandler` engagement).

## Edge cases

| Case | Behavior |
|------|----------|
| `currentHoleInfo == null` | Note icon row hidden. Same as how distance-to-green is hidden. |
| Whitespace-only input | Trimmed and stored as `null`. Icon reverts to outline. |
| Hole change while editor / quick view / discard dialog open | All note UI dismissed silently; draft discarded. |
| Long notes | No character limit. Quick-view bubble scrolls vertically; editor body fills available space. |
| Migration on existing rows | New `notes` column is nullable with no default; existing rows get `NULL`. No data migration needed. |
| Concurrent rounds at same course | Last-write-wins via `updateHole`. Acceptable for v1. |
| `currentHoleInfo.notes` changes externally while editor open | Editor draft is the source of truth while open; refresh happens after save. |

## Testing

### Unit tests — `ActiveRoundViewModelTest`

The project currently has no ViewModel tests for `ActiveRoundViewModel`. The Manual Course Creation feature added kotlinx-coroutines-test, Turbine, and a `FakeCourseRepository` pattern; reuse that infrastructure.

If `FakeCourseRepository` isn't already accessible to a new test file in this package (it lives in `app/src/test/java/com/example/shottracker/feature/createcourse/`), either:
- Move it to a shared test package (`app/src/test/java/com/example/shottracker/testing/`), updating its package declaration and any imports, OR
- Create a new fake within `app/src/test/java/com/example/shottracker/feature/round/`.

Either choice is acceptable; the implementation plan should pick one. Default to creating a fresh fake in `feature/round/` for v1 to avoid touching the existing one.

Additional fake needed: `FakeRoundRepository` (the existing fake only stubs `CourseRepository`). It needs only the methods `ActiveRoundViewModel` calls, returning sensible defaults.

Tests to write:

- `onNoteIconTapped_notesNull_opensEditorWithEmptyDraft`
- `onNoteIconTapped_notesBlank_opensEditorWithEmptyDraft`
- `onNoteIconTapped_notesPresent_opensQuickView`
- `openNoteEditorFromQuickView_loadsCurrentNotesIntoDraft`
- `updateNoteDraft_updatesState`
- `saveNote_blankDraft_savesNullAndClosesEditor` — assert fake repo received `updateHole(holeInfo.copy(notes = null))`, then `showNoteEditor = false`.
- `saveNote_nonBlankDraft_savesTrimmedTextAndClosesEditor` — leading/trailing whitespace trimmed before save.
- `saveNote_persistsToHoleInfoAndRefreshes` — after save, `currentHoleInfo.notes` reflects the new value.
- `deleteNote_savesNull` — equivalent to `saveNote("")`.
- `requestCloseEditor_draftMatchesSaved_closesImmediately`
- `requestCloseEditor_draftDiffersFromSaved_showsDiscardDialog`
- `cancelNoteEdit_closesEditorAndDialog_discardsDraft`
- `previousHole_dismissesAllNoteUi` — set up with editor open, call `previousHole()`, assert all three note flags reset.
- `nextHole_dismissesAllNoteUi` — analogous.

### No Compose UI tests in v1.

The map, quick-view bubble, and full-screen editor are visual; manual testing covers them.

### Manual test plan (run on device before merging)

1. Open a round on a course with no notes anywhere. Verify the note icon in the bottom-right is **outlined**.
2. Tap the icon. Full-screen editor opens with an empty TextField; soft keyboard pops up automatically.
3. Type "Hidden bunker behind green" and tap **Save**. Editor closes. Icon switches to the **filled** variant.
4. Tap the icon again. Quick-view bubble appears with the note text.
5. Tap the pencil icon. Editor reopens with the saved text in the TextField.
6. Edit the text (append " — aim left"). Tap the **✕** (close). "Discard changes?" dialog appears.
7. Tap **Cancel** → returns to editor with edits intact. Tap **Save** → editor closes with new text persisted.
8. Tap icon → quick view shows updated text. Tap pencil. Tap **Delete** → editor closes. Icon reverts to **outlined**. Quick view does not reappear.
9. Navigate Next/Prev hole while editor or quick view is open — they should dismiss automatically with no warning.
10. End the round. Start a new round on the same course. The notes you wrote on each hole are still there.

## Out of Scope (v1)

- Multiple notes per hole (only single note supported).
- Rich text / formatting / photos / attachments.
- Per-round journal-style notes (different from per-course notes — the user explicitly chose per-course).
- Searching or listing all notes outside an active round (a "course notes" view in CourseManagement is a natural follow-up but not required for v1).
- Sharing or exporting notes.
- Character limit enforcement.
- Conflict resolution for concurrent edits across devices.
