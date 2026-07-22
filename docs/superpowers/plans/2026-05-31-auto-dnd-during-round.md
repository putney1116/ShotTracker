# Auto DND During Active Round — Implementation Plan

> REQUIRED SUB-SKILL: superpowers:executing-plans. Steps use checkbox syntax.

**Goal:** Priority-only Do-Not-Disturb auto-enables during an active round when a remembered opt-in toggle (New Round setup) is on, and restores prior state on round end/discard.

**Spec:** `docs/superpowers/specs/2026-05-31-auto-dnd-during-round-design.md`

**Repo note:** Not a git repo. No commit steps. Verify each task with `:app:compileDebugKotlin`.

**Verification:** `.\gradlew.bat :app:compileDebugKotlin` (fast), `.\gradlew.bat :app:installDebug` (device).

---

## File inventory

**New:**
- `app/src/main/java/com/example/shottracker/core/prefs/AppPreferences.kt`
- `app/src/main/java/com/example/shottracker/core/dnd/DndController.kt`

**Modified:**
- `app/src/main/AndroidManifest.xml` — add `ACCESS_NOTIFICATION_POLICY`
- `app/src/main/java/com/example/shottracker/feature/round/NewRoundSetupViewModel.kt` — expose toggle state + DND enable on start
- `app/src/main/java/com/example/shottracker/feature/round/NewRoundSetupScreen.kt` — Switch row + permission dialog
- `app/src/main/java/com/example/shottracker/feature/round/ActiveRoundViewModel.kt` — restore on end/discard (and enable on load as belt-and-suspenders)
- `app/src/main/java/com/example/shottracker/MainActivity.kt` — relaunch safety-net restore

---

### Task 1: AppPreferences

- [ ] Create `AppPreferences.kt`:

```kotlin
package com.example.shottracker.core.prefs

import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("shottracker_prefs", Context.MODE_PRIVATE)

    var silenceDuringRound: Boolean
        get() = prefs.getBoolean(KEY_SILENCE, false)
        set(value) = prefs.edit().putBoolean(KEY_SILENCE, value).apply()

    var dndManagedByApp: Boolean
        get() = prefs.getBoolean(KEY_MANAGED, false)
        set(value) = prefs.edit().putBoolean(KEY_MANAGED, value).apply()

    var previousFilter: Int
        get() = prefs.getInt(KEY_PREV_FILTER, NotificationManager.INTERRUPTION_FILTER_ALL)
        set(value) = prefs.edit().putInt(KEY_PREV_FILTER, value).apply()

    private companion object {
        const val KEY_SILENCE = "silence_during_round"
        const val KEY_MANAGED = "dnd_managed_by_app"
        const val KEY_PREV_FILTER = "dnd_previous_filter"
    }
}
```

- [ ] `.\gradlew.bat :app:compileDebugKotlin` → BUILD SUCCESSFUL.

---

### Task 2: DndController

- [ ] Create `DndController.kt`:

```kotlin
package com.example.shottracker.core.dnd

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import com.example.shottracker.core.prefs.AppPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DndController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: AppPreferences,
) {
    private val nm: NotificationManager
        get() = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun hasPermission(): Boolean =
        try { nm.isNotificationPolicyAccessGranted } catch (e: Exception) { false }

    /** Enable priority-only DND for the duration of a round. No-op without permission. */
    fun enableForRound() {
        if (!hasPermission()) return
        try {
            if (prefs.dndManagedByApp) {
                // Already managing (e.g. relaunch mid-round) — re-assert without clobbering saved filter.
                nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
                return
            }
            prefs.previousFilter = nm.currentInterruptionFilter
            prefs.dndManagedByApp = true
            nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
        } catch (e: SecurityException) {
            Log.w("DndController", "enableForRound failed; clearing managed flag", e)
            prefs.dndManagedByApp = false
        }
    }

    /** Restore the pre-round interruption filter if we set it. No-op otherwise. */
    fun restore() {
        if (!prefs.dndManagedByApp) return
        try {
            nm.setInterruptionFilter(prefs.previousFilter)
        } catch (e: SecurityException) {
            Log.w("DndController", "restore failed", e)
        } finally {
            prefs.dndManagedByApp = false
        }
    }

    fun notificationPolicyAccessIntent(): Intent =
        Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
}
```

- [ ] `.\gradlew.bat :app:compileDebugKotlin` → BUILD SUCCESSFUL.

---

### Task 3: Manifest permission

- [ ] In `AndroidManifest.xml`, after the INTERNET permission, add:

```xml
    <!-- Toggle Do Not Disturb during an active round -->
    <uses-permission android:name="android.permission.ACCESS_NOTIFICATION_POLICY" />
```

- [ ] `.\gradlew.bat :app:compileDebugKotlin` → BUILD SUCCESSFUL.

---

### Task 4: NewRoundSetupViewModel — toggle state + enable on start

- [ ] Inject `AppPreferences` and `DndController` into the constructor.
- [ ] Add to `NewRoundSetupUiState`: `val silenceDuringRound: Boolean = false` and `val needsDndPermission: Boolean = false`.
- [ ] In `init`, seed `silenceDuringRound` from `prefs.silenceDuringRound`.
- [ ] Add:

```kotlin
fun onSilenceDuringRoundChanged(enabled: Boolean) {
    prefs.silenceDuringRound = enabled
    val needsPerm = enabled && !dndController.hasPermission()
    _uiState.value = _uiState.value.copy(silenceDuringRound = enabled, needsDndPermission = needsPerm)
}

fun refreshDndPermission() {
    val needsPerm = _uiState.value.silenceDuringRound && !dndController.hasPermission()
    _uiState.value = _uiState.value.copy(needsDndPermission = needsPerm)
}

fun dismissDndPermissionPrompt() {
    _uiState.value = _uiState.value.copy(needsDndPermission = false)
}

fun dndSettingsIntent() = dndController.notificationPolicyAccessIntent()
```

- [ ] In `startRound`, after the round is created (just before `onRoundStarted(roundId)`):

```kotlin
if (prefs.silenceDuringRound) dndController.enableForRound()
```

- [ ] `.\gradlew.bat :app:compileDebugKotlin` → BUILD SUCCESSFUL.

---

### Task 5: NewRoundSetupScreen — Switch + permission dialog

- [ ] Add a `Switch` row above the Start Round button:

```kotlin
Spacer(modifier = Modifier.height(16.dp))
Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically
) {
    Column(modifier = Modifier.weight(1f)) {
        Text("Silence notifications during round", style = MaterialTheme.typography.bodyLarge)
        Text(
            "Turns on Do Not Disturb (priority only) while you play",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    Switch(
        checked = uiState.silenceDuringRound,
        onCheckedChange = viewModel::onSilenceDuringRoundChanged
    )
}
```

- [ ] Re-check permission on resume:

```kotlin
LifecycleResumeEffect(Unit) {
    viewModel.refreshDndPermission()
    onPauseOrDispose {}
}
```

- [ ] Permission rationale dialog (shown when `uiState.needsDndPermission`):

```kotlin
if (uiState.needsDndPermission) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = viewModel::dismissDndPermissionPrompt,
        title = { Text("Allow Do Not Disturb access") },
        text = { Text("To silence notifications during your round, ShotTracker needs Do Not Disturb access. Open settings to grant it.") },
        confirmButton = {
            Button(onClick = {
                context.startActivity(viewModel.dndSettingsIntent())
                viewModel.dismissDndPermissionPrompt()
            }) { Text("Open settings") }
        },
        dismissButton = {
            TextButton(onClick = viewModel::dismissDndPermissionPrompt) { Text("Not now") }
        }
    )
}
```

- [ ] Add imports: `Switch`, `AlertDialog`, `TextButton`, `LocalContext`, `LifecycleResumeEffect`, `Row`, `Column`, `Alignment` as needed.
- [ ] `.\gradlew.bat :app:compileDebugKotlin` → BUILD SUCCESSFUL.

---

### Task 6: ActiveRoundViewModel — restore on end/discard, enable on load

- [ ] Inject `DndController` and `AppPreferences`.
- [ ] In `loadRound()` after the round is set: `if (prefs.silenceDuringRound) dndController.enableForRound()` (belt-and-suspenders; re-asserts on resume, no-ops if already managed).
- [ ] In `endRound`: call `dndController.restore()` before `onComplete()`.
- [ ] In `discardRound`: call `dndController.restore()` before `onComplete()`.
- [ ] `.\gradlew.bat :app:compileDebugKotlin` → BUILD SUCCESSFUL.

---

### Task 7: MainActivity — relaunch safety net

- [ ] Make `MainActivity` inject `DndController`, `AppPreferences`, and `RoundRepository` (field injection via `@Inject lateinit var`, allowed in `@AndroidEntryPoint` activity).
- [ ] In `onCreate`, before `setContent`, launch a coroutine: if `prefs.dndManagedByApp` and `roundRepository.getActiveRoundSync() == null` → `dndController.restore()`. Use `lifecycleScope.launch`.

```kotlin
if (appPreferences.dndManagedByApp) {
    lifecycleScope.launch {
        if (roundRepository.getActiveRoundSync() == null) {
            dndController.restore()
        } else {
            dndController.enableForRound() // round still active — re-assert
        }
    }
}
```

- [ ] `.\gradlew.bat :app:compileDebugKotlin` → BUILD SUCCESSFUL.

---

### Task 8: Verify build + manual test

- [ ] `.\gradlew.bat :app:assembleDebug` → BUILD SUCCESSFUL.
- [ ] `.\gradlew.bat :app:installDebug` (device connected).
- [ ] Run manual test plan from the spec.
