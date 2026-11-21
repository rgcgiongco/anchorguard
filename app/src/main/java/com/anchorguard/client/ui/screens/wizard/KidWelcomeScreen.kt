package com.anchorguard.client.ui.screens.wizard

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anchorguard.client.ui.components.ThemeSelector
import com.anchorguard.client.ui.screens.ScreenScaffold
import com.anchorguard.client.ui.theme.KidTheme
import com.anchorguard.client.ui.theme.getKidThemeColorScheme

/**
 * Kid-friendly welcome screen with theme preview.
 * First step of the child setup flow.
 */
@Composable
fun KidWelcomeScreen(
    kidTheme: KidTheme = KidTheme.OCEAN_BLUE,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val colorScheme = getKidThemeColorScheme(kidTheme)

    ScreenScaffold(
        title = "Welcome to AnchorGuard",
        subtitle = "Let's set up this device for safe and fun use!"
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Hi there! 👋",
            style = MaterialTheme.typography.displaySmall,
            color = colorScheme.primary,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "This device will have special rules to keep you safe while you play and learn.",
            style = MaterialTheme.typography.bodyLarge,
            color = colorScheme.onSurface,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(32.dp))

        ThemeSelector(
            modifier = Modifier.fillMaxWidth(),
            onThemeSelected = { /* theme already set via parameter */ }
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.primary,
                contentColor = colorScheme.onPrimary
            )
        ) {
            Text(text = "Let's get started!")
        }
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(text = "Back")
        }
    }
}
