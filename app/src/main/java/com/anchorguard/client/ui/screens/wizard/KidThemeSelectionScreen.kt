package com.anchorguard.client.ui.screens.wizard

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anchorguard.client.ui.components.ThemeSelector
import com.anchorguard.client.ui.screens.ScreenScaffold
import com.anchorguard.client.ui.theme.KidTheme
import com.anchorguard.client.ui.theme.getKidThemeColorScheme

/**
 * Kid-friendly theme selection screen.
 * Allows kids to pick their favorite theme color scheme.
 */
@Composable
fun KidThemeSelectionScreen(
    kidTheme: KidTheme,
    onThemeChanged: (KidTheme) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val colorScheme = getKidThemeColorScheme(kidTheme)

    ScreenScaffold(
        title = "Pick your theme",
        subtitle = "Choose your favorite colors for AnchorGuard!"
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Which colors do you like best?",
            style = MaterialTheme.typography.titleLarge,
            color = colorScheme.onSurface,
            modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(24.dp))

        ThemeSelector(
            modifier = Modifier.fillMaxWidth(),
            onThemeSelected = { theme ->
                onThemeChanged(theme)
            }
        )
        Spacer(modifier = Modifier.height(32.dp))

        androidx.compose.material3.Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = colorScheme.primary,
                contentColor = colorScheme.onPrimary
            )
        ) {
            Text(text = "Continue")
        }
        Spacer(modifier = Modifier.height(12.dp))
        androidx.compose.material3.TextButton(
            onClick = onBack,
            modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally)
        ) {
            Text(text = "Back")
        }
    }
}
