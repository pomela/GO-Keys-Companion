package com.companion.gokeys.ui.theme

import androidx.compose.ui.graphics.Color

val Background = Color(0xFF0D0F14)
val Surface = Color(0xFF171A22)
val SurfaceVariant = Color(0xFF1F2330)
val MutedSurface = Color(0xFF1A1D27)
val Border = Color(0xFF262A36)

val Primary = Color(0xFFEB5752)  // Roland red, 12% lighter (default main)
val PrimaryFg = Color(0xFFFFFFFF)
val OnSurface = Color(0xFFF5F5F7)
val Muted = Color(0xFF8B8F9C)
val Success = Color(0xFF3DDC97)
val Destructive = Color(0xFFEF4444)
val SliderThumb = Color(0xFF4A90D9)  // blue (default accent, index 3)

// Shared palette — same order for both main and accent selectors.
// Index -1 is the "mono" sentinel (theme-adaptive black/white), handled in GoKeysTheme.
val ColorPresets: List<Color> = listOf(
    Color(0xFFEB5752),  // 0 Roland red (default main)
    Color(0xFFFF7A3D),  // 1 orange
    Color(0xFF20B2CC),  // 2 teal
    Color(0xFF4A90D9),  // 3 blue   (default accent)
    Color(0xFFE91E8C),  // 4 pink
    Color(0xFF4CAF50),  // 5 light green
)

val PrimaryPresets: List<Color> = ColorPresets
val ThumbPresets: List<Color> = ColorPresets
