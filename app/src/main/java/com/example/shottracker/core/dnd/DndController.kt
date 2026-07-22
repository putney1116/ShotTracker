package com.example.shottracker.core.dnd

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import com.example.shottracker.core.prefs.AppPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enables priority-only Do-Not-Disturb for the duration of an active round and restores the
 * user's prior interruption filter afterward. All NotificationManager calls are guarded so a
 * missing/revoked "Do Not Disturb access" permission can never crash the app.
 */
@Singleton
class DndController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: AppPreferences,
) {
    private val nm: NotificationManager
        get() = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val alarmManager: AlarmManager
        get() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun hasPermission(): Boolean =
        try { nm.isNotificationPolicyAccessGranted } catch (e: Exception) { false }

    /** Enable priority-only DND for the round. No-op without permission. */
    fun enableForRound() {
        if (!hasPermission()) return
        try {
            if (prefs.dndManagedByApp) {
                // Already managing (e.g. app relaunched mid-round). Keep the ORIGINAL deadline
                // anchored to round start — never reset it.
                val deadline = prefs.dndTimeoutAt
                if (deadline in 1..System.currentTimeMillis()) {
                    // Deadline already elapsed while the app was gone — restore now.
                    restore()
                    return
                }
                nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
                // Re-arm the same deadline (recovers the alarm if it was lost, e.g. after reboot).
                if (deadline > 0L) scheduleTimeoutAt(deadline)
                return
            }
            // First time taking over: set the 6h deadline once and turn DND on.
            prefs.dndManagedByApp = true
            val triggerAt = System.currentTimeMillis() + TIMEOUT_MILLIS
            prefs.dndTimeoutAt = triggerAt
            nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
            scheduleTimeoutAt(triggerAt)
            Log.d("DndController", "enableForRound: DND on (priority), deadline=$triggerAt")
        } catch (e: SecurityException) {
            Log.w("DndController", "enableForRound failed; clearing managed flag", e)
            prefs.dndManagedByApp = false
        }
    }

    /**
     * Turn DND off when a round ends. We only ever enable priority-only DND ourselves, so the
     * correct end state is always INTERRUPTION_FILTER_ALL (no DND). We deliberately do NOT replay
     * a previously-saved filter value — that proved unreliable (a stale/unknown value could
     * silently fail to disable DND, leaving it stuck on). The managed flag is cleared only after
     * the filter change actually succeeds, so a transient failure is retried by the next restore
     * (e.g. on the next app launch via the MainActivity safety net) instead of desyncing.
     */
    fun restore() {
        cancelTimeout()
        prefs.dndTimeoutAt = 0L
        if (!prefs.dndManagedByApp) return
        if (!hasPermission()) {
            // Can't change the filter without access; drop the flag so we don't think we own it.
            prefs.dndManagedByApp = false
            return
        }
        try {
            nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            prefs.dndManagedByApp = false
            Log.d("DndController", "restore: DND turned off")
        } catch (e: SecurityException) {
            // Leave dndManagedByApp = true so a later restore retries rather than desyncing.
            Log.w("DndController", "restore failed; will retry on next restore", e)
        }
    }

    fun notificationPolicyAccessIntent(): Intent =
        Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)

    // --- Auto-off safety timer ---------------------------------------------------------------

    /**
     * Schedule a fallback alarm at the absolute [triggerAt] time that restores DND, so DND can't
     * get stuck on if the user forgets to end the round. The deadline is anchored to round start
     * (6h) and reused as-is on relaunch — it is never pushed forward. Inexact + allow-while-idle:
     * fires even after the app is closed/killed and during Doze, without exact-alarm permission.
     * Same PendingIntent, so re-arming simply replaces the prior alarm at the same time.
     */
    private fun scheduleTimeoutAt(triggerAt: Long) {
        try {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, timeoutPendingIntent())
        } catch (e: Exception) {
            Log.w("DndController", "scheduleTimeoutAt failed", e)
        }
    }

    private fun cancelTimeout() {
        try {
            alarmManager.cancel(timeoutPendingIntent())
        } catch (e: Exception) {
            Log.w("DndController", "cancelTimeout failed", e)
        }
    }

    private fun timeoutPendingIntent(): PendingIntent {
        val intent = Intent(context, DndTimeoutReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            TIMEOUT_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private companion object {
        const val TIMEOUT_REQUEST_CODE = 5471
        val TIMEOUT_MILLIS: Long = TimeUnit.HOURS.toMillis(6)
    }
}
