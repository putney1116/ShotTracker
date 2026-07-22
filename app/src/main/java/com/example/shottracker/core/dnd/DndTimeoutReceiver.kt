package com.example.shottracker.core.dnd

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Fires when the DND auto-off safety timer elapses (the user enabled DND for a round but never
 * ended it). Restores the pre-round interruption filter. Runs even if the app process is dead,
 * because it's triggered by an AlarmManager broadcast.
 */
@AndroidEntryPoint
class DndTimeoutReceiver : BroadcastReceiver() {

    @Inject lateinit var dndController: DndController

    override fun onReceive(context: Context, intent: Intent) {
        dndController.restore()
    }
}
