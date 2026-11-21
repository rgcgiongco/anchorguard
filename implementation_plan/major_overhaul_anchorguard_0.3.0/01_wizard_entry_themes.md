# Part 1: Android App - Wizard Entry Screen & Themes

## Version: 0.3.0
## Duration: Week 1-2

---

## 1.1 WizardEntryScreen Revamp (`WizardEntryScreen.kt`)

### Current State
- 4 options: Parent, Child, Parent Adding Child, Shared
- No language selector
- No theme selector

### Target State
- 3 options: **Parent**, **Child**, **Shared** (remove "Parent Adding Child")
- **Language Selector** dropdown (top-right or bottom)
- **Theme Selector** with 5 kid-friendly themes

### Changes to `WizardEntryScreen.kt`

```kotlin
// REMOVE lines 66-75: "I'm a parent adding a new child device" card + button

// ADD: Language Selector Component
// ADD: Theme Selector Component
// MODIFY: Store preferences in SecureSessionStore
```

### New Components

**`ui/components/LanguageSelector.kt`**
- Dropdown with: English, Spanish, French, German, Chinese, Japanese, Korean, Arabic, Hindi, Portuguese
- Persists to `SecureSessionStore.setSelectedLanguage()`
- Updates app locale via `Configuration`

**`ui/components/ThemeSelector.kt`**
- Horizontal scrollable cards showing theme previews
- 5 themes with kid-friendly designs
- Persists to `SecureSessionStore.setSelectedTheme()`
- Applies immediately via `AnchorGuardTheme`

---

## 1.2 Theme System Implementation

### New File: `ui/theme/KidThemes.kt`

```kotlin
enum class KidTheme(val id: String, val name: String, val description: String) {
    OCEAN_BLUE("ocean_blue", "Ocean Blue", "Cool blues for little explorers"),
    SUNSET_PINK("sunset_pink", "Sunset Pink", "Warm pinks for dreamy adventurers"),
    FOREST_GREEN("forest_green", "Forest Green", "Natural greens for nature lovers"),
    SPACE_PURPLE("space_purple", "Space Purple", "Cosmic purples for star gazers"),
    RAINBOW_BRIGHT("rainbow_bright", "Rainbow Bright", "Colorful fun for everyone")
}
```

### Modified: `ui/theme/Color.kt` - Add 5 Theme Color Schemes

```kotlin
// Theme 1: Ocean Blue (Boy-friendly)
val OceanPrimary = Color(0xFF006994)
val OceanPrimaryDark = Color(0xFF004D6B)
val OceanSecondary = Color(0xFF00A8CC)
val OceanSurface = Color(0xFFE0F2F7)
val OceanSurfaceVariant = Color(0xFFB3E0EB)

// Theme 2: Sunset Pink (Girl-friendly)
val SunsetPrimary = Color(0xFFE85D75)
val SunsetPrimaryDark = Color(0xFFC0395B)
val SunsetSecondary = Color(0xFFFF8FA3)
val SunsetSurface = Color(0xFFFFEBEE)
val SunsetSurfaceVariant = Color(0xFFFFCDD2)

// Theme 3: Forest Green (Neutral)
val ForestPrimary = Color(0xFF2E7D32)
val ForestPrimaryDark = Color(0xFF1B5E20)
val ForestSecondary = Color(0xFF4CAF50)
val ForestSurface = Color(0xFFE8F5E9)
val ForestSurfaceVariant = Color(0xFFC8E6C9)

// Theme 4: Space Purple (Unisex)
val SpacePrimary = Color(0xFF6A1B9A)
val SpacePrimaryDark = Color(0xFF4A148C)
val SpaceSecondary = Color(0xFF9C27B0)
val SpaceSurface = Color(0xFFF3E5F5)
val SpaceSurfaceVariant = Color(0xFFE1BEE7)

// Theme 5: Rainbow Bright (Unisex)
val RainbowPrimary = Color(0xFFFF6B6B)
val RainbowPrimaryDark = Color(0xFFEE5A5A)
val RainbowSecondary = Color(0xFF4ECDC4)
val RainbowSurface = Color(0xFFFFF3E0)
val RainbowSurfaceVariant = Color(0xFFFFE0B2)
```

### Modified: `ui/theme/Theme.kt` - Dynamic Theme Support

```kotlin
@Composable
fun AnchorGuardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    kidTheme: KidTheme = KidTheme.OCEAN_BLUE, // NEW PARAMETER
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> getKidThemeColorScheme(kidTheme) // NEW FUNCTION
    }
    // ... rest unchanged
}

private fun getKidThemeColorScheme(theme: KidTheme): ColorScheme {
    return when (theme) {
        KidTheme.OCEAN_BLUE -> OceanColorScheme
        KidTheme.SUNSET_PINK -> SunsetColorScheme
        KidTheme.FOREST_GREEN -> ForestColorScheme
        KidTheme.SPACE_PURPLE -> SpaceColorScheme
        KidTheme.RAINBOW_BRIGHT -> RainbowColorScheme
    }
}
```

### Modified: `auth/security/SecureSessionStore.kt`

```kotlin
// ADD THESE METHODS:
fun setSelectedTheme(themeId: String) {
    raw().edit().putString("selected_theme", themeId).apply()
}

fun getSelectedTheme(): String {
    return raw().getString("selected_theme", KidTheme.OCEAN_BLUE.id)
}

fun setSelectedLanguage(languageCode: String) {
    raw().edit().putString("selected_language", languageCode).apply()
}

fun getSelectedLanguage(): String {
    return raw().getString("selected_language", "en")
}
```

---

## 1.3 Files to Create/Modify

### New Files
```
ui/theme/KidThemes.kt
ui/components/LanguageSelector.kt
ui/components/ThemeSelector.kt
```

### Modified Files
```
ui/screens/wizard/WizardEntryScreen.kt
ui/theme/Color.kt
ui/theme/Theme.kt
auth/security/SecureSessionStore.kt
nav/Routes.kt (if new routes needed)
```

---

## 1.4 Codegraph Checkpoints

### At Start
- `codegraph_explore` query: "WizardEntryScreen SecureSessionStore Theme Color"
- Review: `WizardEntryScreen.kt`, `Theme.kt`, `Color.kt`, `SecureSessionStore.kt`

### At End
- `codegraph_explore` query: "KidTheme LanguageSelector ThemeSelector"
- Verify: Theme switching works, language persists, navigation unchanged

---

## 1.5 Testing

### Unit Tests
- `KidTheme` enum values and properties
- `SecureSessionStore` theme/language persistence
- `getKidThemeColorScheme` returns correct scheme

### Integration Tests
- Theme selector changes app colors immediately
- Language selector updates app locale
- Preferences persist across app restarts
- WizardEntryScreen shows 3 options + selectors

### Device Testing
- Phone: selectors fit in layout
- Tablet: selectors appropriately sized
- All 5 themes render correctly
- RTL language support (Arabic)

---

## 1.6 Dependencies

- None (foundation module)
- Must complete before Part 2 (parent flow uses themes)