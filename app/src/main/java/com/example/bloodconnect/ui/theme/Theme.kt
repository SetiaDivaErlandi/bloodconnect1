package com.example.bloodconnect.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = RedPrimary,
    secondary = GrayMedium,
    tertiary = RedLight,
    background = Black,
    surface = Black,
    onPrimary = White,
    onSecondary = White,
    onTertiary = Black,
    onBackground = White,
    onSurface = White,
)

private val LightColorScheme = lightColorScheme(
    primary = RedPrimary,
    secondary = GrayMedium,
    tertiary = RedLight,
    background = GrayLight,
    surface = White,
    onPrimary = White,
    onSecondary = White,
    onTertiary = RedPrimary,
    onBackground = Black,
    onSurface = Black,
)

@Composable
fun BloodconnectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
