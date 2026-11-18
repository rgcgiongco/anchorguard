package com.anchorguard.client.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anchorguard.client.ui.components.AnchorInfoCard
import com.anchorguard.client.ui.components.AnchorPrimaryButton
import com.anchorguard.client.ui.components.AnchorSecondaryButton

@Composable
fun AuthWelcomeScreen(
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onBack: () -> Unit
) {
    ScreenScaffold(
        title = "Parent account",
        subtitle = "Sign in to manage devices, pairing, and protection settings."
    ) {
        AnchorInfoCard(
            title = "AnchorGuard account",
            body = "Use your parent email and password to link and manage child devices."
        )
        Spacer(Modifier.height(20.dp))
        AnchorPrimaryButton(text = "Sign in", onClick = onLogin)
        Spacer(Modifier.height(8.dp))
        AnchorSecondaryButton(text = "Create account", onClick = onRegister)
        Spacer(Modifier.height(8.dp))
        AnchorSecondaryButton(text = "Back", onClick = onBack)
    }
}
