package com.companion.gokeys.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import com.companion.gokeys.data.PreferencesConfig
import com.companion.gokeys.data.ThemeMode

val LocalSliderThumb = compositionLocalOf { SliderThumb }

fun PreferencesConfig.mainColor(): Color {
    if (mainCustomHex.length == 6) {
        val r = mainCustomHex.substring(0, 2).toIntOrNull(16)
        val g = mainCustomHex.substring(2, 4).toIntOrNull(16)
        val b = mainCustomHex.substring(4, 6).toIntOrNull(16)
        if (r != null && g != null && b != null) return Color(r, g, b)
    }
    return PrimaryPresets.getOrElse(mainPresetIndex) { Primary }
}

fun PreferencesConfig.accentColor(): Color {
    if (accentCustomHex.length == 6) {
        val r = accentCustomHex.substring(0, 2).toIntOrNull(16)
        val g = accentCustomHex.substring(2, 4).toIntOrNull(16)
        val b = accentCustomHex.substring(4, 6).toIntOrNull(16)
        if (r != null && g != null && b != null) return Color(r, g, b)
    }
    return ThumbPresets.getOrElse(accentPresetIndex) { SliderThumb }
}

private fun buildDarkScheme(primary: Color) = darkColorScheme(
    primary = primary,
    onPrimary = PrimaryFg,
    background = Background,
    onBackground = OnSurface,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurface,
    error = Destructive,
    onError = OnSurface,
    outline = Border,
)

private fun buildLightScheme(primary: Color) = lightColorScheme(
    primary = primary,
    onPrimary = Color.White,
    background = Color(0xFFF2F3F7),
    onBackground = Color(0xFF1A1A2E),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1A2E),
    surfaceVariant = Color(0xFFEAEBF0),
    onSurfaceVariant = Color(0xFF44445A),
    error = Destructive,
    onError = Color.White,
    outline = Color(0xFFCCCCDD),
)

@Composable
fun GoKeysTheme(
    preferences: PreferencesConfig = PreferencesConfig(),
    content: @Composable () -> Unit,
) {
    val isDark = when (preferences.themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val primary = preferences.mainColor()
    val thumb = preferences.accentColor()
    val scheme = if (isDark) buildDarkScheme(primary) else buildLightScheme(primary)

    CompositionLocalProvider(LocalSliderThumb provides thumb) {
        MaterialTheme(colorScheme = scheme, typography = AppTypography, content = content)
    }
}
