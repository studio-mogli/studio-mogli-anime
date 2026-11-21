package com.studiomogli.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val NetflixBlack = Color(0xFF141414)
val NetflixSurface = Color(0xFF1F1F1F)
val NetflixRed = Color(0xFFE50914)
val TrustGreen = Color(0xFF22C55E)
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFB3B3B3)

private val DarkColorPalette = darkColorScheme(
    primary = NetflixRed,
    background = NetflixBlack,
    surface = NetflixSurface,
    onPrimary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun StudioMogliTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorPalette,
        content = content
    )
}
