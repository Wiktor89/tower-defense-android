package ru.games.platform.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF2D6A4F),
    onPrimary = Color.White,
    secondary = Color(0xFFD4A373),
    background = Color(0xFFF7F3EA),
    surface = Color.White,
    onSurface = Color(0xFF1E293B),
    error = Color(0xFFB91C1C),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF52B788),
    onPrimary = Color(0xFF052E16),
    secondary = Color(0xFFE9C46A),
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF1F5F9),
)

@Composable
fun GamesTheme(dark: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        content = content,
    )
}
