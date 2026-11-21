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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anchorguard.client.ui.screens.ScreenScaffold
import com.anchorguard.client.ui.theme.KidTheme
import com.anchorguard.client.ui.theme.getKidThemeColorScheme

/**
 * Kid-friendly device pairing screen.
 * Enter the pairing code to link this child device to the parent account.
 */
@Composable
fun KidDevicePairingScreen(
    kidTheme: KidTheme,
    onPaired: () -> Unit,
    onSkip: () -> Unit,
    onBack: () -> Unit
) {
    val colorScheme = getKidThemeColorScheme(kidTheme)
    var pairingCode by remember { mutableStateOf("") }

    ScreenScaffold(
        title = "Link devices",
        subtitle = "Enter the pairing code from the parent device."
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Pairing code",
            style = MaterialTheme.typography.titleLarge,
            color = colorScheme.onSurface,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = pairingCode,
            onValueChange = { pairingCode = it },
            label = { Text(text = "Code") },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { if (pairingCode.length >= 4) onPaired() },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
            enabled = pairingCode.length >= 4,
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.primary,
                contentColor = colorScheme.onPrimary
            )
        ) {
            Text(text = "Pair device")
        }
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(
            onClick = onSkip,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(text = "Skip for now", style = MaterialTheme.typography.labelMedium)
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
