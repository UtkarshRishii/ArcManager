package com.arcmanager.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ArcManagerColorScheme = darkColorScheme(
    primary = PrimaryViolet,
    onPrimary = TextOnPrimary,
    primaryContainer = PrimaryVioletSubtle,
    onPrimaryContainer = PrimaryVioletLight,
    secondary = SecondaryBlue,
    onSecondary = TextOnPrimary,
    secondaryContainer = StatusInfoSubtle,
    onSecondaryContainer = SecondaryBlueLight,
    tertiary = StatusSuccess,
    onTertiary = TextOnPrimary,
    tertiaryContainer = StatusSuccessSubtle,
    onTertiaryContainer = StatusSuccess,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    surfaceContainerHighest = DarkSurfaceCard,
    surfaceContainerHigh = DarkSurfaceElevated,
    surfaceContainerLow = DarkSurface,
    surfaceContainer = DarkSurfaceElevated,
    outline = BorderDefault,
    outlineVariant = BorderSubtle,
    error = StatusDanger,
    onError = TextOnPrimary,
    errorContainer = StatusDangerSubtle,
    onErrorContainer = StatusDanger,
    inverseSurface = TextPrimary,
    inverseOnSurface = DarkBackground,
    inversePrimary = PrimaryVioletDark,
    scrim = DarkBackground,
)

@Composable
fun ArcManagerTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DarkBackground.toArgb()
            window.navigationBarColor = DarkBackground.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = ArcManagerColorScheme,
        typography = ArcManagerTypography,
        shapes = ArcManagerShapes,
        content = content
    )
}
