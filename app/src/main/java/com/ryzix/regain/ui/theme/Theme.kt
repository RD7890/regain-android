package com.ryzix.regain.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val RegainColorScheme = darkColorScheme(
    primary = RegainRed,
    onPrimary = TextPrimary,
    primaryContainer = RegainRedContainer,
    onPrimaryContainer = TextPrimary,
    secondary = RegainRed,
    onSecondary = TextPrimary,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = CardDark,
    onSurfaceVariant = TextSecondary,
    outline = DividerColor,
    error = RegainRed,
    onError = TextPrimary
)

@Composable
fun RegainTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = RegainColorScheme,
        typography = RegainTypography,
        content = content
    )
}
