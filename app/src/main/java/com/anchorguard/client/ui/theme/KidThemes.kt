package com.anchorguard.client.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Kid-friendly theme options for the AnchorGuard wizard entry screen.
 * Each theme provides a distinct primary/secondary/surface color palette.
 */
enum class KidTheme(
    val id: String,
    val displayName: String,
    val description: String
) {
    OCEAN_BLUE("ocean_blue", "Ocean Blue", "Cool blues for little explorers"),
    SUNSET_PINK("sunset_pink", "Sunset Pink", "Warm pinks for dreamy adventurers"),
    FOREST_GREEN("forest_green", "Forest Green", "Natural greens for nature lovers"),
    SPACE_PURPLE("space_purple", "Space Purple", "Cosmic purples for star gazers"),
    RAINBOW_BRIGHT("rainbow_bright", "Rainbow Bright", "Colorful fun for everyone")
}

// Color values are defined in Color.kt (same package).

private fun getPrimaryColor(theme: KidTheme): Color = when (theme) {
    KidTheme.OCEAN_BLUE -> OceanPrimary
    KidTheme.SUNSET_PINK -> SunsetPrimary
    KidTheme.FOREST_GREEN -> ForestPrimary
    KidTheme.SPACE_PURPLE -> SpacePrimary
    KidTheme.RAINBOW_BRIGHT -> RainbowPrimary
}

private fun getPrimaryDarkColor(theme: KidTheme): Color = when (theme) {
    KidTheme.OCEAN_BLUE -> OceanPrimaryDark
    KidTheme.SUNSET_PINK -> SunsetPrimaryDark
    KidTheme.FOREST_GREEN -> ForestPrimaryDark
    KidTheme.SPACE_PURPLE -> SpacePrimaryDark
    KidTheme.RAINBOW_BRIGHT -> RainbowPrimaryDark
}

private fun getSecondaryColor(theme: KidTheme): Color = when (theme) {
    KidTheme.OCEAN_BLUE -> OceanSecondary
    KidTheme.SUNSET_PINK -> SunsetSecondary
    KidTheme.FOREST_GREEN -> ForestSecondary
    KidTheme.SPACE_PURPLE -> SpaceSecondary
    KidTheme.RAINBOW_BRIGHT -> RainbowSecondary
}

private fun getSurfaceColor(theme: KidTheme): Color = when (theme) {
    KidTheme.OCEAN_BLUE -> OceanSurface
    KidTheme.SUNSET_PINK -> SunsetSurface
    KidTheme.FOREST_GREEN -> ForestSurface
    KidTheme.SPACE_PURPLE -> SpaceSurface
    KidTheme.RAINBOW_BRIGHT -> RainbowSurface
}

private fun getSurfaceVariantColor(theme: KidTheme): Color = when (theme) {
    KidTheme.OCEAN_BLUE -> OceanSurfaceVariant
    KidTheme.SUNSET_PINK -> SunsetSurfaceVariant
    KidTheme.FOREST_GREEN -> ForestSurfaceVariant
    KidTheme.SPACE_PURPLE -> SpaceSurfaceVariant
    KidTheme.RAINBOW_BRIGHT -> RainbowSurfaceVariant
}

fun getKidThemeColorScheme(theme: KidTheme): androidx.compose.material3.ColorScheme {
    val primary = getPrimaryColor(theme)
    val primaryDark = getPrimaryDarkColor(theme)
    val secondary = getSecondaryColor(theme)
    val surface = getSurfaceColor(theme)
    val surfaceVariant = getSurfaceVariantColor(theme)

    return androidx.compose.material3.lightColorScheme(
        primary = primary,
        onPrimary = Color.White,
        primaryContainer = surfaceVariant,
        onPrimaryContainer = primaryDark,
        secondary = secondary,
        onSecondary = Color.White,
        secondaryContainer = surface,
        onSecondaryContainer = primaryDark,
        tertiary = AnchorWarning,
        onTertiary = Color(0xFF2B1E07),
        background = surface,
        onBackground = AnchorOnSurface,
        surface = surface,
        onSurface = AnchorOnSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = AnchorOnSurfaceVariant,
        outline = AnchorOutline,
        error = AnchorError,
        onError = AnchorOnError
    )
}
