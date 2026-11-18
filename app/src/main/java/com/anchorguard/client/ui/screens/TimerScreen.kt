package com.anchorguard.client.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anchorguard.client.ui.components.AnchorInfoCard
import com.anchorguard.client.ui.components.AnchorSecondaryButton

@Composable
fun TimerScreen() {
    ScreenScaffold(
        title = "Timer",
        subtitle = "Temporary lock and screen-time presets."
    ) {
        AnchorInfoCard(title = "Presets", body = "5, 15, 30 minutes and 1 hour quick presets placeholder.")
        Spacer(Modifier.height(12.dp))
        AnchorInfoCard(title = "Custom duration", body = "Custom minute picker placeholder.")
        Spacer(Modifier.height(20.dp))
        AnchorSecondaryButton(text = "Start temporary lock (placeholder)", onClick = {})
    }
}
