package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.model.AppThemeMode
import com.example.model.DarkModePreference

fun getThemeColorScheme(
    themeMode: AppThemeMode,
    darkMode: DarkModePreference,
    systemDark: Boolean
): ColorScheme {
    val isDark = when (darkMode) {
        DarkModePreference.SYSTEM -> systemDark
        DarkModePreference.LIGHT -> false
        DarkModePreference.DARK, DarkModePreference.AMOLED -> true
    }
    val isAmoled = darkMode == DarkModePreference.AMOLED

    val baseBackground = when {
        isAmoled -> SurfaceAmoled
        isDark -> SurfaceDark
        else -> SurfaceLight
    }

    val baseSurface = when {
        isAmoled -> Color(0xFF0A0A0A)
        isDark -> CardDark
        else -> CardLight
    }

    return when (themeMode) {
        AppThemeMode.EMERALD -> if (isDark) {
            darkColorScheme(
                primary = EmeraldPrimary,
                onPrimary = EmeraldOnPrimary,
                primaryContainer = EmeraldOnPrimaryContainer,
                onPrimaryContainer = EmeraldPrimaryContainer,
                secondary = EmeraldSecondary,
                tertiary = EmeraldTertiary,
                background = baseBackground,
                surface = baseSurface,
                onBackground = TextPrimaryDark,
                onSurface = TextPrimaryDark
            )
        } else {
            lightColorScheme(
                primary = EmeraldPrimary,
                onPrimary = EmeraldOnPrimary,
                primaryContainer = EmeraldPrimaryContainer,
                onPrimaryContainer = EmeraldOnPrimaryContainer,
                secondary = EmeraldSecondary,
                tertiary = EmeraldTertiary,
                background = baseBackground,
                surface = baseSurface,
                onBackground = TextPrimaryLight,
                onSurface = TextPrimaryLight
            )
        }

        AppThemeMode.OCEAN -> if (isDark) {
            darkColorScheme(
                primary = OceanPrimary,
                onPrimary = OceanOnPrimary,
                primaryContainer = OceanOnPrimaryContainer,
                onPrimaryContainer = OceanPrimaryContainer,
                secondary = OceanSecondary,
                tertiary = OceanTertiary,
                background = baseBackground,
                surface = baseSurface,
                onBackground = TextPrimaryDark,
                onSurface = TextPrimaryDark
            )
        } else {
            lightColorScheme(
                primary = OceanPrimary,
                onPrimary = OceanOnPrimary,
                primaryContainer = OceanPrimaryContainer,
                onPrimaryContainer = OceanOnPrimaryContainer,
                secondary = OceanSecondary,
                tertiary = OceanTertiary,
                background = baseBackground,
                surface = baseSurface,
                onBackground = TextPrimaryLight,
                onSurface = TextPrimaryLight
            )
        }

        AppThemeMode.CYBER_VIOLET -> if (isDark) {
            darkColorScheme(
                primary = VioletPrimary,
                onPrimary = VioletOnPrimary,
                primaryContainer = VioletOnPrimaryContainer,
                onPrimaryContainer = VioletPrimaryContainer,
                secondary = VioletSecondary,
                tertiary = VioletTertiary,
                background = baseBackground,
                surface = baseSurface,
                onBackground = TextPrimaryDark,
                onSurface = TextPrimaryDark
            )
        } else {
            lightColorScheme(
                primary = VioletPrimary,
                onPrimary = VioletOnPrimary,
                primaryContainer = VioletPrimaryContainer,
                onPrimaryContainer = VioletOnPrimaryContainer,
                secondary = VioletSecondary,
                tertiary = VioletTertiary,
                background = baseBackground,
                surface = baseSurface,
                onBackground = TextPrimaryLight,
                onSurface = TextPrimaryLight
            )
        }

        AppThemeMode.SUNSET -> if (isDark) {
            darkColorScheme(
                primary = SunsetPrimary,
                onPrimary = SunsetOnPrimary,
                primaryContainer = SunsetOnPrimaryContainer,
                onPrimaryContainer = SunsetPrimaryContainer,
                secondary = SunsetSecondary,
                tertiary = SunsetTertiary,
                background = baseBackground,
                surface = baseSurface,
                onBackground = TextPrimaryDark,
                onSurface = TextPrimaryDark
            )
        } else {
            lightColorScheme(
                primary = SunsetPrimary,
                onPrimary = SunsetOnPrimary,
                primaryContainer = SunsetPrimaryContainer,
                onPrimaryContainer = SunsetOnPrimaryContainer,
                secondary = SunsetSecondary,
                tertiary = SunsetTertiary,
                background = baseBackground,
                surface = baseSurface,
                onBackground = TextPrimaryLight,
                onSurface = TextPrimaryLight
            )
        }

        AppThemeMode.OBSIDIAN -> if (isDark) {
            darkColorScheme(
                primary = ObsidianSecondary,
                onPrimary = ObsidianOnPrimary,
                primaryContainer = ObsidianOnPrimaryContainer,
                onPrimaryContainer = ObsidianPrimaryContainer,
                secondary = ObsidianPrimary,
                tertiary = ObsidianTertiary,
                background = baseBackground,
                surface = baseSurface,
                onBackground = TextPrimaryDark,
                onSurface = TextPrimaryDark
            )
        } else {
            lightColorScheme(
                primary = ObsidianPrimary,
                onPrimary = ObsidianOnPrimary,
                primaryContainer = ObsidianPrimaryContainer,
                onPrimaryContainer = ObsidianOnPrimaryContainer,
                secondary = ObsidianSecondary,
                tertiary = ObsidianTertiary,
                background = baseBackground,
                surface = baseSurface,
                onBackground = TextPrimaryLight,
                onSurface = TextPrimaryLight
            )
        }
    }
}

@Composable
fun GoalTrackerTheme(
    themeMode: AppThemeMode = AppThemeMode.EMERALD,
    darkMode: DarkModePreference = DarkModePreference.SYSTEM,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val colorScheme = getThemeColorScheme(themeMode, darkMode, systemDark)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
