package com.example.shottracker.core.prefs

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Small SharedPreferences-backed store for app-level settings that don't belong in Room.
 * Currently holds the auto-DND opt-in plus the state needed to restore the user's prior
 * Do-Not-Disturb filter after a round.
 */
@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("shottracker_prefs", Context.MODE_PRIVATE)

    /** Remembered opt-in: silence notifications (enable DND) while a round is active. */
    var silenceDuringRound: Boolean
        get() = prefs.getBoolean(KEY_SILENCE, false)
        set(value) = prefs.edit().putBoolean(KEY_SILENCE, value).apply()

    /** True while the app is the one holding DND on (so we know to restore it). */
    var dndManagedByApp: Boolean
        get() = prefs.getBoolean(KEY_MANAGED, false)
        set(value) = prefs.edit().putBoolean(KEY_MANAGED, value).apply()

    /**
     * Absolute epoch-millis deadline at which DND should auto-restore (anchored to round start).
     * 0 = no pending timeout. Stored so it survives app close/reopen and is never reset.
     */
    var dndTimeoutAt: Long
        get() = prefs.getLong(KEY_TIMEOUT_AT, 0L)
        set(value) = prefs.edit().putLong(KEY_TIMEOUT_AT, value).apply()

    private companion object {
        const val KEY_SILENCE = "silence_during_round"
        const val KEY_MANAGED = "dnd_managed_by_app"
        const val KEY_TIMEOUT_AT = "dnd_timeout_at"
    }
}
