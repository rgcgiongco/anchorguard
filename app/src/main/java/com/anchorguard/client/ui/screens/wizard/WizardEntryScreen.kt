package com.anchorguard.client.ui.screens.wizard

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anchorguard.client.ui.components.AnchorInfoCard
import com.anchorguard.client.ui.components.AnchorPrimaryButton
import com.anchorguard.client.ui.screens.ScreenScaffold

/**
 * Unified wizard entry screen that replaces Welcome + Role Selection.
 *
 * Branches into three explicit paths:
 * - Path A: Parent device → sign in / create account → permissions → pairing → safety → dashboard
 * - Path B: Child device → register → permissions → pairing → safety → child home
 * - Path C: Parent adding child → register → permissions → pairing → safety → child home (no auth)
 */
@Composable
fun WizardEntryScreen(
    onParentDevice: () -> Unit,
    onChildDevice: () -> Unit,
    onParentAddingChild: () -> Unit
) {
    ScreenScaffold(
        title = "Set up this device",
        subtitle = "Who is this device for?"
    ) {
        AnchorInfoCard(
            title = "I'm a parent — sign in or create account",
            body = "Manage family settings, review devices, and control pairing."
        )
        Spacer(Modifier.height(12.dp))
        AnchorPrimaryButton(text = "Set up as parent", onClick = onParentDevice)

        Spacer(Modifier.height(24.dp))

        AnchorInfoCard(
            title = "This is a child's device",
            body = "Apply allowed apps, schedules, and screen-time rules on this device."
        )
        Spacer(Modifier.height(12.dp))
        AnchorPrimaryButton(text = "Set up as child", onClick = onChildDevice)

        Spacer(Modifier.height(24.dp))

        AnchorInfoCard(
            title = "I'm a parent adding a new child device",
            body = "Register a child device to your account without signing in on this device."
        )
        Spacer(Modifier.height(12.dp))
        AnchorPrimaryButton(text = "Add child device", onClick = onParentAddingChild)
    }
}
