package com.anchorguard.client.ui.screens.wizard

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.anchorguard.client.auth.security.SecureSessionStore
import com.anchorguard.client.ui.components.AnchorInfoCard
import com.anchorguard.client.ui.components.AnchorPrimaryButton
import com.anchorguard.client.ui.screens.ScreenScaffold

/**
 * Unified wizard entry screen that replaces Welcome + Role Selection.
 *
 * Branches into four explicit paths:
 * - Path A: Parent device → sign in / create account → permissions → pairing → safety → dashboard
 * - Path B: Child device → register → permissions → pairing → safety → child home
 * - Path C: Parent adding child → register → permissions → pairing → safety → child home (no auth)
 * - Path D: Shared device (Parent + Child) → permissions → pairing → safety → dashboard
 */
@Composable
fun WizardEntryScreen(
    onParentDevice: () -> Unit,
    onChildDevice: () -> Unit,
    onParentAddingChild: () -> Unit,
    onBothDevices: () -> Unit
) {
    val context = LocalContext.current
    val store = remember { SecureSessionStore(context) }

    ScreenScaffold(
        title = "Set up this device",
        subtitle = "Who is this device for?"
    ) {
        AnchorInfoCard(
            title = "I'm a parent — sign in or create account",
            body = "Manage family settings, review devices, and control pairing."
        )
        Spacer(Modifier.height(12.dp))
        AnchorPrimaryButton(text = "Set up as parent", onClick = {
            store.setWizardRole("parent")
            store.setSetupMode("parent")
            onParentDevice()
        })

        Spacer(Modifier.height(24.dp))

        AnchorInfoCard(
            title = "This is a child's device",
            body = "Apply allowed apps, schedules, and screen-time rules on this device."
        )
        Spacer(Modifier.height(12.dp))
        AnchorPrimaryButton(text = "Set up as child", onClick = {
            store.setWizardRole("child")
            store.setSetupMode("child_device")
            onChildDevice()
        })

        Spacer(Modifier.height(24.dp))

        AnchorInfoCard(
            title = "I'm a parent adding a new child device",
            body = "Register a child device to your account without signing in on this device."
        )
        Spacer(Modifier.height(12.dp))
        AnchorPrimaryButton(text = "Add child device", onClick = {
            store.setWizardRole("child")
            store.setSetupMode("parent_adding_child")
            onParentAddingChild()
        })

        Spacer(Modifier.height(24.dp))

        AnchorInfoCard(
            title = "This device is shared — Parent + Child",
            body = "Parent has full access; Child Mode restricts apps and schedules. Switch between modes with Parent PIN."
        )
        Spacer(Modifier.height(12.dp))
        AnchorPrimaryButton(text = "Set up as shared", onClick = {
            store.setDeviceRole("both")
            store.setSetupMode("shared")
            store.setActiveMode("parent")
            onBothDevices()
        })
    }
}