package com.realmirror.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Glass-morphism inspired palette
val DeepBlack = Color(0xFF050508)
val GlassWhite = Color(0xCCFFFFFF)
val GlassBorder = Color(0x33FFFFFF)
val AccentCyan = Color(0xFF00E5FF)
val AccentPurple = Color(0xFFBB86FC)
val RecordRed = Color(0xFFFF1744)
val RecordRedDim = Color(0x99FF1744)
val SurfaceGlass = Color(0x22FFFFFF)
val OnGlass = Color(0xFFFFFFFF)
val TimerYellow = Color(0xFFFFD600)

private val DarkColorScheme = darkColorScheme(
    primary = AccentCyan,
    secondary = AccentPurple,
    tertiary = RecordRed,
    background = DeepBlack,
    surface = SurfaceGlass,
    onPrimary = DeepBlack,
    onSecondary = DeepBlack,
    onBackground = OnGlass,
    onSurface = OnGlass,
)

@Composable
fun RealMirrorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
