package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CleanGreenPrimaryDark,
    onPrimary = CleanGreenOnPrimaryDark,
    primaryContainer = CleanGreenContainerDark,
    onPrimaryContainer = CleanGreenOnContainerDark,
    secondary = CleanEmeraldSecondaryDark,
    onSecondary = CleanEmeraldOnSecondaryDark,
    secondaryContainer = CleanEmeraldContainerDark,
    onSecondaryContainer = CleanEmeraldOnContainerDark,
    background = CleanBackgroundDark,
    onBackground = CleanOnBackgroundDark,
    surface = CleanSurfaceDark,
    onSurface = CleanOnSurfaceDark
)

private val LightColorScheme = lightColorScheme(
    primary = CleanGreenPrimary,
    onPrimary = CleanGreenOnPrimary,
    primaryContainer = CleanGreenContainer,
    onPrimaryContainer = CleanGreenOnContainer,
    secondary = CleanEmeraldSecondary,
    onSecondary = CleanEmeraldOnSecondary,
    secondaryContainer = CleanEmeraldContainer,
    onSecondaryContainer = CleanEmeraldOnContainer,
    tertiary = CleanBlueTertiary,
    onTertiary = CleanBlueOnTertiary,
    tertiaryContainer = CleanBlueContainer,
    onTertiaryContainer = CleanBlueOnContainer,
    background = CleanBackgroundLight,
    onBackground = CleanOnBackgroundLight,
    surface = CleanSurfaceLight,
    onSurface = CleanOnSurfaceLight,
    surfaceVariant = CleanSurfaceVariantLight,
    onSurfaceVariant = CleanOnSurfaceVariantLight,
    outline = CleanOutlineLight
)

@Composable
fun CleanTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep brand green consistent
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = CleanTrackTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
