package com.abir.kotlinposapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val PosDarkColorScheme = darkColorScheme(
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

private val PosLightColorScheme = lightColorScheme(
    primary = PosGreenDark,
    onPrimary = PosOnError,
    primaryContainer = PosLightPrimaryContainer,
    onPrimaryContainer = PosLightOnPrimaryContainer,
    secondary = PosGreenDark,
    onSecondary = PosOnError,
    background = PosLightBackground,
    onBackground = PosLightOnBackground,
    surface = PosLightSurface,
    onSurface = PosLightOnBackground,
    surfaceVariant = PosLightSurfaceVariant,
    onSurfaceVariant = PosLightOnSurfaceVariant,
    error = PosError,
    onError = PosOnError
)

@Composable
fun KotlinPOSAppTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) PosDarkColorScheme else PosLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
