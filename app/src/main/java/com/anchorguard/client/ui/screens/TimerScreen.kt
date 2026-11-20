package com.anchorguard.client.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.anchorguard.client.R
import com.anchorguard.client.data.local.UserRepository
import com.anchorguard.client.model.SubscriptionTier
import com.anchorguard.client.ui.components.AnchorInfoCard
import com.anchorguard.client.ui.components.AnchorSecondaryButton

@Composable
fun TimerScreen() {
    val context = LocalContext.current
    val userRepo = remember { UserRepository(context) }
    val userTier = userRepo.getUserTier()

    ScreenScaffold(
        title = "Timer",
        subtitle = "Temporary lock and screen-time presets."
    ) {
        AnchorInfoCard(title = "Presets", body = "5, 15, 30 minutes and 1 hour quick presets placeholder.")
        Spacer(Modifier.height(12.dp))
        AnchorInfoCard(title = "Custom duration", body = "Custom minute picker placeholder.")
        Spacer(Modifier.height(20.dp))
        AnchorSecondaryButton(text = "Start temporary lock (placeholder)", onClick = {})
        Spacer(Modifier.height(20.dp))
        if (userTier.tier == SubscriptionTier.PAID) {
            AnchorInfoCard(title = "Schedule & Automation", body = "Recurring weekly schedule and automated lock rules.")
            Spacer(Modifier.height(12.dp))
            AnchorSecondaryButton(text = "Configure Schedule", onClick = {})
        } else {
            Text(
                text = context.getString(R.string.free_tier_schedules_disabled),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(8.dp))
            AnchorSecondaryButton(text = "Upgrade for Schedule & Automation", onClick = {})
        }
    }
}
