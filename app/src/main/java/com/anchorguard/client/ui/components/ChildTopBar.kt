package com.anchorguard.client.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anchorguard.client.ui.theme.KidTheme
import com.anchorguard.client.ui.theme.getKidThemeColorScheme

/**
 * Top bar for Child Home launcher mode.
 * Shows child avatar/name and PIN-protected Settings/Exit actions.
 */
@Composable
fun ChildTopBar(
    childName: String,
    kidTheme: KidTheme = KidTheme.OCEAN_BLUE,
    onSettingsClick: () -> Unit,
    onExitClick: () -> Unit
) {
    val colorScheme = getKidThemeColorScheme(kidTheme)

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "Child avatar",
                tint = colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = childName,
                style = MaterialTheme.typography.titleLarge,
                color = colorScheme.onSurface
            )
        }
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = colorScheme.onSurface
                )
            }
            IconButton(onClick = onExitClick) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Exit Child Mode",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
