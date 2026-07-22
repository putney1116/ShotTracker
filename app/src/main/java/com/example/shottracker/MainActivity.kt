package com.example.shottracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.shottracker.core.dnd.DndController
import com.example.shottracker.core.prefs.AppPreferences
import com.example.shottracker.domain.repository.RoundRepository
import com.example.shottracker.navigation.ShotTrackerNavHost
import com.example.shottracker.ui.theme.ShotTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var dndController: DndController
    @Inject lateinit var appPreferences: AppPreferences
    @Inject lateinit var roundRepository: RoundRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Safety net: if we left DND on (e.g. process died mid-round), restore it once the
        // round is no longer active; otherwise re-assert it while the round continues.
        if (appPreferences.dndManagedByApp) {
            lifecycleScope.launch {
                if (roundRepository.getActiveRoundSync() == null) {
                    dndController.restore()
                } else {
                    dndController.enableForRound()
                }
            }
        }

        setContent {
            ShotTrackerTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ShotTrackerNavHost(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
