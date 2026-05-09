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
    val primary = PrimaryPresets.getOrElse(preferences.primaryPresetIndex) { Primary }
    val thumb = ThumbPresets.getOrElse(preferences.sliderThumbPresetIndex) { SliderThumb }
    val scheme = if (isDark) buildDarkScheme(primary) else buildLightScheme(primary)

    CompositionLocalProvider(LocalSliderThumb provides thumb) {
        MaterialTheme(colorScheme = scheme, typography = AppTypography, content = content)
    }
}
