package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6D28D9),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE4FA),
    onPrimaryContainer = Color(0xFF2E1065),
    secondary = PulseTealSuccess,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F7F0),
    onSecondaryContainer = Color(0xFF003826),
    tertiary = PulseElectricViolet,
    onTertiary = Color.White,
    background = PulseLavenderBg,
    onBackground = PulseOnSurfaceLight,
    surface = PulseSurfaceLight,
    onSurface = PulseOnSurfaceLight,
    surfaceVariant = PulseSurfaceVariantLight,
    onSurfaceVariant = PulseOnSurfaceMutedLight,
    outline = PulseOutlineLight,
    error = PulseCrimsonFail,
    onError = Color.White,
    errorContainer = Color(0xFFFEE4E2),
    onErrorContainer = Color(0xFF7A140E)
)

private val DarkColorScheme = darkColorScheme(
    primary = PulseViolet,
    onPrimary = Color.White,
    primaryContainer = PulseSoftViolet,
    onPrimaryContainer = Color(0xFFE9D5FF),
    secondary = PulseTealSuccess,
    onSecondary = Color(0xFF002418),
    secondaryContainer = PulseTealSoft,
    onSecondaryContainer = Color(0xFFA7F3D0),
    tertiary = PulseElectricViolet,
    onTertiary = Color.White,
    background = PulseDarkBg,
    onBackground = PulseOnSurfaceDark,
    surface = PulseDarkSurface,
    onSurface = PulseOnSurfaceDark,
    surfaceVariant = PulseDarkCard,
    onSurfaceVariant = PulseOnSurfaceMutedDark,
    outline = PulseOutlineDark,
    error = PulseCrimsonFail,
    onError = Color.White,
    errorContainer = PulseCrimsonSoft,
    onErrorContainer = Color(0xFFFECACA)
)

val PulsePayShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun PulsePayTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PulsePayTypography,
        shapes = PulsePayShapes,
        content = content
    )
}
