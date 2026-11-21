package com.anchorguard.client.ui.screens.wizard

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anchorguard.client.ui.screens.ScreenScaffold
import com.anchorguard.client.ui.theme.KidTheme
import com.anchorguard.client.ui.theme.getKidThemeColorScheme
/**
 * Kid-friendly setup complete screen.
 * Shows success message and transitions to child home.
 */
@Composable
fun KidSetupCompleteScreen(
    kidTheme: KidTheme,
    onFinish: () -> Unit,
    onBack: () -> Unit
) {
    val colorScheme = getKidThemeColorScheme(kidTheme)

    ScreenScaffold(
        title = "All set! 🎉",
        subtitle = "Your device is ready for safe use."
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "🎉",
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "You're all set!",
            style = MaterialTheme.typography.displaySmall,
            color = colorScheme.onSurface,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "AnchorGuard will help you stay safe and focused. Have fun!",
            style = MaterialTheme.typography.bodyLarge,
            color = colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.primary,
                contentColor = colorScheme.onPrimary
            )
        ) {
            Text(text = "Start using device")
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
