package com.example.shottracker.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = GolfGreenDark,
    secondary = GolfGreenVariantDark,
    tertiary = FairwayGreenDark,
    primaryContainer = Color(0xFF1B5E20),
    secondaryContainer = Color(0xFF2E7D32),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black
)

private val LightColorScheme = lightColorScheme(
    primary = GolfGreen,
    secondary = GolfGreenVariant,
    tertiary = FairwayGreen,
    primaryContainer = Color(0xFFC8E6C9),
    secondaryContainer = Color(0xFFDCEDC8),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    background = Color(0xFFFAFAFA),
    surface = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F)
)

@Composable
fun ShotTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled to use golf theme colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
