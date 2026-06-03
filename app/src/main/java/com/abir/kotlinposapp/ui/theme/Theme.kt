package com.abir.kotlinposapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val PosColorScheme = darkColorScheme(
    primary = PosGreen,
    onPrimary = PosOnGreen,
    primaryContainer = PosGreenDark,
    onPrimaryContainer = PosOnBackground,
    secondary = PosGreenDark,
    onSecondary = PosOnGreen,
    background = PosBackground,
    onBackground = PosOnBackground,
    surface = PosSurface,
    onSurface = PosOnBackground,
    surfaceVariant = PosSurfaceVariant,
    onSurfaceVariant = PosOnSurfaceVariant,
    error = PosError,
    onError = PosOnError
)

@Composable
fun KotlinPOSAppTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PosColorScheme,
        typography = Typography,
        content = content
    )
}
