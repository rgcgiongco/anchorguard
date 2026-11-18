package com.anchorguard.client.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anchorguard.client.ui.components.AnchorInfoCard
import com.anchorguard.client.ui.components.AnchorPrimaryButton

@Composable
fun WelcomeScreen(onContinue: () -> Unit) {
    ScreenScaffold(
        title = "Welcome",
        subtitle = "Set up AnchorGuard to manage apps, screen time, and device access."
    ) {
        AnchorInfoCard(
            title = "What you can do",
            body = "Create a parent gate, choose this device's role, and pair with your family account. Enforcement features are configured in later setup steps."
        )
        Spacer(Modifier.height(20.dp))
        AnchorPrimaryButton(text = "Get started", onClick = onContinue)
    }
}
