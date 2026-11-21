package com.anchorguard.client.ui.screens.wizard

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
 * Kid-friendly permissions screen.
 * Explains why AnchorGuard needs permissions in kid-friendly language.
 */
@Composable
fun KidPermissionsScreen(
    kidTheme: KidTheme,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val colorScheme = getKidThemeColorScheme(kidTheme)

    ScreenScaffold(
        title = "Safety rules",
        subtitle = "AnchorGuard needs a few permissions to keep you safe."
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Here's what AnchorGuard does:",
            style = MaterialTheme.typography.titleLarge,
            color = colorScheme.onSurface,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(20.dp))

        PermissionItem(
            title = "Block distracting apps",
            description = "Helps you focus on homework and bedtime.",
            colorScheme = colorScheme,
            icon = "🚫"
        )
        Spacer(modifier = Modifier.height(16.dp))
        PermissionItem(
            title = "Set screen time limits",
            description = "Reminds you when it's time to take a break.",
            colorScheme = colorScheme,
            icon = "⏰"
        )
        Spacer(modifier = Modifier.height(16.dp))
        PermissionItem(
            title = "Keep your device safe",
            description = "Protects your phone from unwanted changes.",
            colorScheme = colorScheme,
            icon = "🔒"
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
            Text(text = "I understand")
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

@Composable
private fun PermissionItem(
    title: String,
    description: String,
    colorScheme: androidx.compose.material3.ColorScheme,
    icon: String
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, style = MaterialTheme.typography.displaySmall)
        Spacer(modifier = Modifier.width(16.dp))
        androidx.compose.foundation.layout.Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}
