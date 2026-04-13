package com.goatsoccer.manager.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GoatSoccerColorScheme = lightColorScheme(
    primary            = GreenPrimary,
    onPrimary          = Color.White,
    primaryContainer   = GreenLight,
    onPrimaryContainer = Color.White,
    secondary          = GreenDark,
    onSecondary        = Color.White,
    secondaryContainer = GreenLight,
    background         = AppBackground,
    onBackground       = TextPrimary,
    surface            = AppSurface,
    onSurface          = TextPrimary,
    surfaceVariant     = AppBackground,
    onSurfaceVariant   = TextSecondary,
    error              = AppError,
    onError            = Color.White
)

@Composable
fun GoatSoccerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GoatSoccerColorScheme,
        content     = content
    )
}
