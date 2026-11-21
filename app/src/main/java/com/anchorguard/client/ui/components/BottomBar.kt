package com.anchorguard.client.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anchorguard.client.ui.theme.KidTheme
import com.anchorguard.client.ui.theme.getKidThemeColorScheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning

/**
 * Bottom bar for Child Home launcher mode.
 * Shows SOS button, time remaining, and optional Parent Mode toggle.
 */
@Composable
fun BottomBar(
    totalTimeRemainingMillis: Long = 0,
    kidTheme: KidTheme = KidTheme.OCEAN_BLUE,
    onSosClick: () -> Unit,
    onParentModeClick: () -> Unit,
    showParentModeToggle: Boolean = false
) {
    val colorScheme = getKidThemeColorScheme(kidTheme)

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onSosClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            ),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = "SOS",
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SOS — Emergency Alert",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onError
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Time Remaining: ${formatTimeRemaining(totalTimeRemainingMillis)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (showParentModeToggle) {
                TextButton(
                    onClick = onParentModeClick,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(text = "Parent Mode", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

private fun formatTimeRemaining(millis: Long): String {
    val minutes = millis / 60000
    val hours = minutes / 60
    val remainingMinutes = minutes % 60
    return if (hours > 0) "${hours}h ${remainingMinutes}m" else "${remainingMinutes}m"
}
