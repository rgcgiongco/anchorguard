package com.anchorguard.client.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class AnchorColorScheme(
    val primary: Color = AnchorPrimary,
    val primaryDark: Color = AnchorPrimaryDark,
    val secondary: Color = AnchorSecondary,
    val danger: Color = AnchorDanger,
    val warning: Color = AnchorWarning,
    val surface: Color = AnchorSurfaceLight,
    val surfaceAlt: Color = AnchorSurface,
    val surfaceVariant: Color = AnchorSurfaceVariant,
    val onPrimary: Color = AnchorOnPrimary,
    val onSurface: Color = AnchorOnSurface,
    val onSurfaceVariant: Color = AnchorOnSurfaceVariant,
    val outline: Color = AnchorOutline,
    val success: Color = AnchorSuccess,
    val error: Color = AnchorError,
    val onError: Color = AnchorOnError,
    val onSecondary: Color = AnchorOnSecondary
)

val AnchorColors = AnchorColorScheme()

val AnchorShapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(16.dp)
)

object AnchorElevation {
    val level0: Dp = 0.dp
    val level1: Dp = 1.dp
    val level2: Dp = 3.dp
    val level3: Dp = 6.dp
    val level4: Dp = 8.dp
}

private val LightColorScheme = lightColorScheme(
    primary = AnchorPrimary,
    onPrimary = AnchorOnPrimary,
    primaryContainer = AnchorSurfaceVariant,
    onPrimaryContainer = AnchorPrimaryDark,
    secondary = AnchorSecondary,
    onSecondary = AnchorOnSecondary,
    secondaryContainer = Color(0xFFDDF3E8),
    onSecondaryContainer = Color(0xFF1E4A32),
    tertiary = AnchorWarning,
    onTertiary = Color(0xFF2B1E07),
    background = AnchorSurface,
    onBackground = AnchorOnSurface,
    surface = AnchorSurfaceLight,
    onSurface = AnchorOnSurface,
    surfaceVariant = AnchorSurfaceVariant,
    onSurfaceVariant = AnchorOnSurfaceVariant,
    outline = AnchorOutline,
    error = AnchorError,
    onError = AnchorOnError
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF7FC0DA),
    onPrimary = Color(0xFF082634),
    primaryContainer = AnchorPrimaryDark,
    onPrimaryContainer = Color(0xFFD7EAF3),
    secondary = Color(0xFF86D7AA),
    onSecondary = Color(0xFF0B2B1A),
    secondaryContainer = Color(0xFF24543A),
    onSecondaryContainer = Color(0xFFDDF3E8),
    tertiary = Color(0xFFECC47A),
    onTertiary = Color(0xFF2B1E07),
    background = AnchorDarkBackground,
    onBackground = AnchorDarkOnSurface,
    surface = AnchorDarkSurface,
    onSurface = AnchorDarkOnSurface,
    surfaceVariant = Color(0xFF24323D),
    onSurfaceVariant = Color(0xFFB7C6D2),
    outline = Color(0xFF6E8594),
    error = Color(0xFFE48A86),
    onError = Color(0xFF3A0A08)
)

@Composable
fun AnchorGuardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
        typography = AnchorTypography,
        shapes = AnchorShapes,
        content = content
    )
}
