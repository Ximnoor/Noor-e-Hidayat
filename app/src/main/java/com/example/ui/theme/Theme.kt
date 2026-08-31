package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AppDarkColorScheme = darkColorScheme(
    background = DarkGreenBg,
    surface = EmeraldSurface,
    surfaceVariant = EmeraldSurfaceDark,
    primary = GoldAccent,
    onBackground = TextLight,
    onSurface = TextLight,
    onPrimary = DarkGreenBg
)

private val AppLightColorScheme = lightColorScheme(
    background = Color(0xFFF5F2ED),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE5E2DD),
    primary = Color(0xFF0E4B3E),
    onBackground = Color(0xFF06261D),
    onSurface = Color(0xFF06261D),
    onPrimary = Color(0xFFFFFFFF)
)

@Composable
fun NoorEHidayatTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) AppDarkColorScheme else AppLightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.value.toInt()
            window.navigationBarColor = if (darkTheme) NavBackground.value.toInt() else colorScheme.surface.value.toInt()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
