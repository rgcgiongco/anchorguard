package com.anchorguard.client.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.anchorguard.client.auth.security.SecureSessionStore
import com.anchorguard.client.ui.theme.KidTheme
import com.anchorguard.client.ui.theme.getKidThemeColorScheme

/**
 * Theme selector component with horizontal scrollable cards showing theme previews.
 * Persists selection to SecureSessionStore and applies theme immediately.
 */
@Composable
fun ThemeSelector(
    onThemeSelected: ((theme: KidTheme) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val store = remember { SecureSessionStore(context) }
    var selectedTheme by remember { mutableStateOf(KidTheme.valueOf(store.getSelectedTheme().uppercase())) }

    val themes = KidTheme.values().toList()

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        themes.forEach { theme ->
            ThemeCard(
                theme = theme,
                selected = selectedTheme == theme,
                onClick = {
                    selectedTheme = theme
                    store.setSelectedTheme(theme.id)
                    onThemeSelected?.invoke(theme)
                }
            )
        }
    }
}

@Composable
private fun ThemeCard(
    theme: KidTheme,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = getKidThemeColorScheme(theme)

    Card(
        modifier = Modifier
            .width(80.dp)
            .height(100.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surfaceContainer
        ),
        shape = MaterialTheme.shapes.large,
        elevation = if (selected) CardDefaults.cardElevation(defaultElevation = 4.dp) else CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Theme preview color block
            Surface(
                color = colorScheme.primary,
                modifier = Modifier.size(48.dp)
            ) { }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = theme.displayName,
                style = MaterialTheme.typography.labelMedium,
                color = colorScheme.onSurface,
                maxLines = 1
            )
            if (selected) {
                Text(
                    text = "✓",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.primary
                )
            }
        }
    }
}
