# Auto Do-Not-Disturb During Active Round

Automatically enable Android Do-Not-Disturb (priority-only) while a golf round is active, controlled by a remembered opt-in toggle on the New Round setup screen. DND turns on when the round starts and restores the previous state when the round ends or is discarded.

## Motivation

Distractions (call ringtones, notifications) during a round are annoying, especially when audio routes through a Bluetooth speaker. The user wants the app to silence interruptions automatically while playing, scoped specifically to an active round — not merely "while the app is open."

## User Flow

1. On the New Round setup screen there's a switch: **"Silence notifications during round (Do Not Disturb)"**. Its state is remembered between rounds.
2. First time the user turns it on without "Do Not Disturb access" granted, a rationale dialog appears with a button that opens the system DND-access settings screen. The toggle stays visually on; it only takes effect once access is granted (re-checked on screen resume).
3. When a round starts (toggle on + permission granted), the app enables **priority-only** DND.
4. DND stays on for the entire active round — even if the app is backgrounded or the screen locks.
5. When the round is ended or discarded, the app restores whatever interruption filter was in effect before.

## Decisions (from brainstorming)

- **Control:** opt-in toggle on New Round setup, remembered (persisted preference). No separate settings screen.
- **DND level:** priority-only (`INTERRUPTION_FILTER_PRIORITY`) — lets through whatever the user configured as priority (favorite contacts, repeat callers, alarms).
- **Scope:** on at round start, off at round end/discard. Stays on across app backgrounding / screen lock.
- **Architecture:** ViewModel-driven. `ActiveRoundViewModel` owns the round lifecycle and drives a `DndController`.

## Architecture

### Components

1. **`DndController`** — `core/dnd/DndController.kt`. Hilt `@Singleton`, constructor-injected with `@ApplicationContext context`. Wraps `NotificationManager`.
   - `hasPermission(): Boolean` → `notificationManager.isNotificationPolicyAccessGranted`.
   - `enableForRound()`:
     - If no permission → no-op (return).
     - If `AppPreferences.dndManagedByApp` is already true → re-assert priority-only (handles relaunch mid-round) and return.
     - Else: save `notificationManager.currentInterruptionFilter` into `AppPreferences.previousFilter`, set `dndManagedByApp = true`, then `setInterruptionFilter(INTERRUPTION_FILTER_PRIORITY)`.
   - `restore()`:
     - If `dndManagedByApp` is false → no-op.
     - Else: `setInterruptionFilter(AppPreferences.previousFilter)` (default `INTERRUPTION_FILTER_ALL` if unset), then clear `dndManagedByApp`.
   - Every `NotificationManager` mutate/read call is wrapped in try/catch; on `SecurityException` (access revoked) it clears `dndManagedByApp` and no-ops.
   - Opens settings: helper `notificationPolicyAccessIntent(): Intent` returning `Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)`.

2. **`AppPreferences`** — `core/prefs/AppPreferences.kt`. Hilt `@Singleton`, SharedPreferences-backed (file `"shottracker_prefs"`). No new dependency.
   - `var silenceDuringRound: Boolean` (default `false`).
   - `var dndManagedByApp: Boolean` (default `false`).
   - `var previousFilter: Int` (default `NotificationManager.INTERRUPTION_FILTER_ALL` = 1).

3. **UI + wiring:**
   - New Round setup: a Material3 `Switch` row bound to `silenceDuringRound`, plus the permission rationale dialog.
   - `ActiveRoundViewModel`: calls `DndController.enableForRound()` after a round is loaded/started; calls `DndController.restore()` in `endRound` and `discardRound`.
   - App-launch safety net (in `MainActivity` or `ShotTrackerApplication`): if `dndManagedByApp == true` and there is no in-progress round, call `restore()`.

### Data flow

```
NewRoundSetupScreen (Switch)
        │ toggle → AppPreferences.silenceDuringRound
        ▼
NewRoundSetupViewModel.startRound() → creates round, navigates
        ▼
ActiveRoundViewModel.loadRound()/init
        │ if silenceDuringRound && DndController.hasPermission()
        ▼
DndController.enableForRound()  ── saves prev filter, sets PRIORITY
        ⋮ (round in progress; DND persists across backgrounding)
ActiveRoundViewModel.endRound()/discardRound()
        ▼
DndController.restore()  ── restores prev filter
```

### Permission

- Manifest: add `<uses-permission android:name="android.permission.ACCESS_NOTIFICATION_POLICY" />`.
- This permission is not auto-granted; the user must enable "Do Not Disturb access" for the app in system settings. The rationale dialog drives them there via `ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS`.

## Edge cases & error handling

| Case | Behavior |
|------|----------|
| Permission never granted | Toggle persists but `enableForRound()` no-ops. No crash, no nag beyond the first rationale. |
| Permission revoked mid-round | Set/restore calls throw `SecurityException`; controller catches, clears `dndManagedByApp`, no-ops. |
| Toggle off while a round already running | No effect on the in-progress round; only affects next round start. |
| User already had DND on before round | Saved as `previousFilter` and restored to it — we never turn off a user's own DND. |
| App killed mid-round | DND stays on (round still active). On relaunch: if `dndManagedByApp` and no active round → restore; if active round still exists → re-assert priority-only. |
| Resume round from home | Same path as start: enable if toggle on + permission. |
| Resume round, toggle since turned off | Honors current preference (won't enable). Acceptable. |

## Testing

### Unit tests
- `AppPreferences`: write/read round-trip for all three fields (using a real `SharedPreferences` from an instrumented context, or a fake). Lightweight.
- `DndController` decision logic: extract the pure decision into testable form where practical; fake the `NotificationManager` interaction behind a small seam so we can assert:
  - `enableForRound()` with permission + unmanaged → saves current filter, sets PRIORITY, sets managed flag.
  - `enableForRound()` already managed → re-asserts PRIORITY, does not overwrite saved filter.
  - `restore()` managed → sets saved filter, clears flag.
  - `restore()` unmanaged → no-op.
  - No permission → `enableForRound()` no-op.

### Not auto-tested (manual)
The real system DND state, the permission-access intent, and `ActiveRoundViewModel` wiring (LocationService coupling) follow the project's existing manual-test posture.

### Manual test plan
1. Grant DND access. Start a round with toggle ON → phone shows DND active; an incoming call is silenced.
2. End the round → DND returns to prior state.
3. Start a round with toggle OFF → no DND.
4. Turn toggle on without access → rationale dialog → opens settings.
5. Revoke access mid-round → no crash.
6. Kill app mid-round, relaunch, end round → DND restored (not stuck on).
7. Manually enable DND before a round, play with toggle on, end round → DND remains on (restored to user's prior state).

## Out of scope (v1)
- Mid-round DND on/off control on the active-round screen.
- A general app settings screen.
- Per-round (vs remembered) toggle choice.
- Total-silence or alarms-only DND levels (priority-only only).
- Auto-restoring DND if the user manually changes the system filter during a round.
