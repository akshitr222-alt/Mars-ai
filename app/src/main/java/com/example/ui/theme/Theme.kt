package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AccentBlue,
    onPrimary = Color.White,
    secondary = AccentSilver,
    onSecondary = Color.Black,
    background = AmoledBlack,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = CardSurface,
    onSurfaceVariant = TextSecondary,
    outline = TextTertiary,
    error = ErrorRed,
    onError = Color.White
)

private val AmoledColorScheme = darkColorScheme(
    primary = AccentBlue,
    onPrimary = Color.White,
    secondary = AccentSilver,
    onSecondary = Color.Black,
    background = AmoledBlack,
    onBackground = TextPrimary,
    surface = AmoledBlack,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = TextSecondary,
    outline = TextTertiary,
    error = ErrorRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = AccentBlue,
    onPrimary = Color.White,
    secondary = Color(0xFF1C1C1E),
    onSecondary = Color.White,
    background = Color(0xFFF2F2F7),
    onBackground = Color(0xFF1C1C1E),
    surface = Color.White,
    onSurface = Color(0xFF1C1C1E),
    surfaceVariant = Color(0xFFE5E5EA),
    onSurfaceVariant = Color(0xFF8E8E93),
    outline = Color(0xFFC7C7CC),
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun SoleAiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    isAmoled: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        isAmoled && darkTheme -> AmoledColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
