package com.anchorguard.client.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anchorguard.client.ui.theme.KidTheme
import com.anchorguard.client.ui.theme.getKidThemeColorScheme

/**
 * Grid of allowed apps for Child Home launcher mode.
 * Uses rows of 4 apps on phone.
 */
@Composable
fun AllowedAppsGrid(
    apps: List<AllowedApp>,
    timeRemainingMap: Map<String, Long> = emptyMap(),
    kidTheme: KidTheme = KidTheme.OCEAN_BLUE,
    onAppClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        apps.chunked(4).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { app ->
                    AppGridCard(
                        app = app,
                        timeRemainingMillis = timeRemainingMap[app.packageName] ?: 0L,
                        kidTheme = kidTheme,
                        onClick = { onAppClick(app.packageName) }
                    )
                }
            }
        }
    }
}
