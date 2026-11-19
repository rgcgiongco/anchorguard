package com.anchorguard.client.ui.screens.wizard

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anchorguard.client.ui.components.AnchorInfoCard
import com.anchorguard.client.ui.components.AnchorPrimaryButton
import com.anchorguard.client.ui.screens.ScreenScaffold

@Composable
fun WizardWelcomeScreen(onGetStarted: () -> Unit) {
    ScreenScaffold(
        title = "Welcome",
        subtitle = "Protect your family's digital world"
    ) {
        AnchorInfoCard(
            title = "AnchorGuard",
            body = "Set up this device for family safety in a few guided steps."
        )
        Spacer(Modifier.height(20.dp))
        AnchorPrimaryButton(text = "Get Started", onClick = onGetStarted)
    }
}
