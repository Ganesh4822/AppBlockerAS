package com.example.appblockerv3.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = White,
    secondary = White,
    tertiary = White
)

private val LightColorScheme = lightColorScheme(
    primary = White,
    secondary = White,
    tertiary = White
)

@Composable
fun AppBlockerV3Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    content: @Composable () -> Unit
) {
    val colors = if (!darkTheme) {
        LightColorScheme
    } else {
        DarkColorScheme
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}