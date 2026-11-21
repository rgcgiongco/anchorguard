package com.anchorguard.client.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.anchorguard.client.ui.theme.KidTheme
import com.anchorguard.client.ui.theme.getKidThemeColorScheme

data class AllowedApp(
    val packageName: String,
    val appName: String,
    val iconResId: Int = 0
)

/**
 * Single app card in the Child Home launcher grid.
 * Shows app icon, name, and time remaining.
 */
@Composable
fun AppGridCard(
    app: AllowedApp,
    timeRemainingMillis: Long = 0,
    kidTheme: KidTheme = KidTheme.OCEAN_BLUE,
    onClick: () -> Unit
) {
    val colorScheme = getKidThemeColorScheme(kidTheme)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surfaceContainer
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App icon placeholder (colored circle)
            androidx.compose.foundation.Canvas(
                modifier = Modifier.size(48.dp)
            ) {
                drawCircle(
                    color = colorScheme.primary,
                    radius = 24f
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = app.appName,
                style = MaterialTheme.typography.labelLarge,
                color = colorScheme.onSurface,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatTimeRemaining(timeRemainingMillis),
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatTimeRemaining(millis: Long): String {
    val minutes = millis / 60000
    val hours = minutes / 60
    val remainingMinutes = minutes % 60
    return if (hours > 0) "${hours}h ${remainingMinutes}m" else "${remainingMinutes}m"
}
